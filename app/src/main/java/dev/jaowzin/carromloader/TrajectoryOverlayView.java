package dev.jaowzin.carromloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * Native-only aim renderer.
 *
 * This view intentionally fails closed: if the Carrom native bridge has not
 * produced a fresh striker position and angle, nothing is drawn. There is no
 * screenshot/vision fallback and no touch-position fallback.
 */
final class TrajectoryOverlayView extends View {
    private static final int CYAN = Color.rgb(91, 245, 218);
    private static final int ICE = Color.rgb(218, 255, 249);
    private static final int BLUE = Color.rgb(75, 214, 255);

    private final Paint glow = stroke(Color.argb(42, 91, 245, 218), 6.4f);
    private final Paint rail = stroke(Color.argb(78, 255, 255, 255), 3.2f);
    private final Paint core = stroke(CYAN, 1.8f);
    private final Paint startDot = fill(Color.WHITE);
    private final Paint impactFill = fill(Color.argb(215, 218, 255, 249));
    private final Paint impactRing = stroke(Color.argb(155, 91, 245, 218), 1.2f);
    private final Paint ghostFill = fill(Color.argb(34, 91, 245, 218));
    private final Paint ghostRing = stroke(Color.argb(205, 155, 255, 236), 1.5f);
    private final Paint ghostCore = fill(Color.argb(205, 230, 255, 251));
    private final ShotPredictor predictor = new ShotPredictor();
    private final boolean bankPreview;

    private float startX;
    private float startY;
    private float currentX;
    private float currentY;
    private boolean active;
    private int clearGeneration;

    TrajectoryOverlayView(Context context, boolean bankPreview) {
        super(context);
        this.bankPreview = bankPreview;
        setClickable(false);
        setFocusable(false);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        glow.setStrokeCap(Paint.Cap.ROUND);
        glow.setStrokeJoin(Paint.Join.ROUND);
        glow.setShadowLayer(dp(5), 0f, 0f, Color.argb(90, 91, 245, 218));
        rail.setStrokeCap(Paint.Cap.ROUND);
        rail.setStrokeJoin(Paint.Join.ROUND);
        core.setStrokeCap(Paint.Cap.ROUND);
        core.setStrokeJoin(Paint.Join.ROUND);
        startDot.setShadowLayer(dp(4), 0f, 0f, Color.argb(155, 91, 245, 218));
        impactFill.setShadowLayer(dp(4), 0f, 0f, Color.argb(145, 91, 245, 218));
        ghostFill.setShadowLayer(dp(7), 0f, 0f, Color.argb(75, 91, 245, 218));
        ghostRing.setPathEffect(new DashPathEffect(new float[]{dp(5), dp(3)}, 0f));
    }

    void startVision(Window ignoredWindow) {
        NativeAimBridge.ensureStarted();
    }

    void stopVision() {
        active = false;
        clearGeneration++;
        invalidate();
    }

    void observeTouch(MotionEvent event) {
        if (event == null) return;
        NativeAimBridge.ensureStarted();

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            clearGeneration++;
            startX = event.getX();
            startY = event.getY();
            currentX = startX;
            currentY = startY;
            active = true;
            invalidate();
        } else if (action == MotionEvent.ACTION_MOVE) {
            currentX = event.getX();
            currentY = event.getY();
            active = true;
            invalidate();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            currentX = event.getX();
            currentY = event.getY();
            active = true;
            invalidate();
            final int generation = ++clearGeneration;
            postDelayed(() -> {
                if (generation == clearGeneration) {
                    active = false;
                    invalidate();
                }
            }, 900L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active || getWidth() <= 0 || getHeight() <= 0) return;

        NativeAimBridge.State nativeState = NativeAimBridge.read();
        if (nativeState == null
                || !nativeState.hooked()
                || !nativeState.positionFresh()
                || !nativeState.angleFresh()) {
            return;
        }

        float touchAimX = startX - currentX;
        float touchAimY = startY - currentY;
        float drag = (float) Math.hypot(touchAimX, touchAimY);
        if (drag < dp(10)) return;

        RectF board = estimateBoardRect();
        PointF origin = mapNativeWorldToBoard(board, nativeState);
        if (origin == null) return;

        PointF direction = nativeDirection(nativeState.angle, touchAimX, touchAimY);
        if (!Float.isFinite(direction.x) || !Float.isFinite(direction.y)) return;

        float discRadius = board.width() * 0.0325f;
        ShotPredictor.Prediction prediction = predictor.predict(
                board,
                discRadius,
                origin.x,
                origin.y,
                direction.x,
                direction.y,
                drag,
                bankPreview ? 3 : 0
        );
        if (prediction.segments.isEmpty()) return;

        ShotPredictor.Segment first = prediction.segments.get(0);
        canvas.drawCircle(first.from.x, first.from.y, dp(2.6f), startDot);
        for (int i = 0; i < prediction.segments.size(); i++) {
            ShotPredictor.Segment segment = prediction.segments.get(i);
            drawSegment(canvas, segment, i);
            if (segment.cushionHit && i < prediction.segments.size() - 1) {
                drawImpact(canvas, segment.to.x, segment.to.y, i);
            }
        }
        if (prediction.complete) {
            drawGhostStop(canvas, prediction.stop.x, prediction.stop.y, prediction.discRadiusPx);
        } else {
            drawCutoff(canvas, prediction.stop.x, prediction.stop.y);
        }
    }

    private PointF mapNativeWorldToBoard(RectF board, NativeAimBridge.State state) {
        double x = state.worldX;
        double y = state.worldY;
        double max = Math.max(Math.abs(x), Math.abs(y));

        double halfExtent;
        if (max <= 0.60) {
            halfExtent = 0.37;
        } else if (max <= 17.5) {
            halfExtent = 14.57;
        } else if (max <= 45.0) {
            halfExtent = 37.0;
        } else {
            return null;
        }

        float sx = (float) (board.centerX() + (x / halfExtent) * board.width() * 0.5);
        float sy = (float) (board.centerY() - (y / halfExtent) * board.height() * 0.5);
        float pad = board.width() * 0.04f;
        if (sx < board.left - pad || sx > board.right + pad
                || sy < board.top - pad || sy > board.bottom + pad) {
            return null;
        }
        return new PointF(sx, sy);
    }

    private PointF nativeDirection(double angle, float touchX, float touchY) {
        float touchLen = (float) Math.hypot(touchX, touchY);
        if (touchLen < 0.001f || !Double.isFinite(angle)) {
            return new PointF(Float.NaN, Float.NaN);
        }

        float tx = touchX / touchLen;
        float ty = touchY / touchLen;
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float[][] candidates = {
                {c, s}, {c, -s}, {-c, s}, {-c, -s},
                {s, c}, {s, -c}, {-s, c}, {-s, -c}
        };

        float bestDot = -Float.MAX_VALUE;
        float bestX = Float.NaN;
        float bestY = Float.NaN;
        for (float[] candidate : candidates) {
            float dot = candidate[0] * tx + candidate[1] * ty;
            if (dot > bestDot) {
                bestDot = dot;
                bestX = candidate[0];
                bestY = candidate[1];
            }
        }
        return new PointF(bestX, bestY);
    }

    private void drawSegment(Canvas canvas, ShotPredictor.Segment segment, int index) {
        int fade = Math.max(85, 240 - index * 44);
        glow.setAlpha(Math.max(14, 42 - index * 8));
        rail.setAlpha(Math.max(22, 78 - index * 12));
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, glow);
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, rail);
        core.setShader(new LinearGradient(
                segment.from.x,
                segment.from.y,
                segment.to.x,
                segment.to.y,
                withAlpha(index == 0 ? ICE : CYAN, fade),
                withAlpha(index == 0 ? CYAN : BLUE, Math.max(70, fade - 35)),
                Shader.TileMode.CLAMP
        ));
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, core);
        core.setShader(null);
    }

    private void drawImpact(Canvas canvas, float x, float y, int index) {
        impactFill.setAlpha(Math.max(115, 210 - index * 35));
        impactRing.setAlpha(Math.max(85, 160 - index * 25));
        canvas.drawCircle(x, y, dp(2.2f), impactFill);
        canvas.drawCircle(x, y, dp(5.2f), impactRing);
    }

    private void drawGhostStop(Canvas canvas, float x, float y, float radius) {
        canvas.drawCircle(x, y, radius, ghostFill);
        canvas.drawCircle(x, y, radius, ghostRing);
        canvas.drawCircle(x, y, Math.max(dp(2f), radius * 0.15f), ghostCore);
    }

    private void drawCutoff(Canvas canvas, float x, float y) {
        impactRing.setAlpha(130);
        canvas.drawCircle(x, y, dp(5.7f), impactRing);
        impactFill.setAlpha(160);
        canvas.drawCircle(x, y, dp(1.9f), impactFill);
    }

    private RectF estimateBoardRect() {
        float w = getWidth();
        float h = getHeight();
        if (h > w * 1.28f) {
            float side = w * 0.955f;
            float left = (w - side) * 0.5f;
            float top = h * 0.245f;
            if (top + side > h - dp(20)) {
                top = Math.max(dp(20), h - side - dp(20));
            }
            return new RectF(left, top, left + side, top + side);
        }
        float margin = Math.max(dp(16), Math.min(w, h) * 0.035f);
        return new RectF(margin, margin, w - margin, h - margin);
    }

    private Paint stroke(int color, float widthDp) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(widthDp));
        paint.setColor(color);
        return paint;
    }

    private Paint fill(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        return paint;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
