package dev.jaowzin.carromloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * Native-only aim renderer.
 *
 * The guide is rendered as a pair of parallel rails around the predicted disc path,
 * similar to the extended-guideline style used by carrom/pool trainers. Native state
 * remains the source of truth; stale samples fail closed and are not painted.
 */
final class TrajectoryOverlayView extends View {
    private static final int CYAN = Color.rgb(35, 244, 235);
    private static final int ICE = Color.rgb(235, 255, 254);

    private final Paint halo = stroke(Color.argb(55, 35, 244, 235), 6.5f);
    private final Paint edge = stroke(Color.argb(235, 35, 244, 235), 2.0f);
    private final Paint centerAxis = stroke(Color.argb(180, 245, 255, 255), 1.25f);
    private final Paint strikerRing = stroke(Color.argb(205, 35, 244, 235), 2.0f);
    private final Paint strikerCore = fill(Color.argb(220, 245, 255, 255));
    private final Paint impactFill = fill(Color.argb(225, 235, 255, 254));
    private final Paint impactRing = stroke(Color.argb(205, 35, 244, 235), 1.7f);
    private final Paint ghostFill = fill(Color.argb(28, 35, 244, 235));
    private final Paint ghostRing = stroke(Color.argb(220, 35, 244, 235), 1.8f);
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

        halo.setStrokeCap(Paint.Cap.ROUND);
        halo.setStrokeJoin(Paint.Join.ROUND);
        halo.setShadowLayer(dp(4.5f), 0f, 0f, Color.argb(105, 35, 244, 235));
        edge.setStrokeCap(Paint.Cap.ROUND);
        edge.setStrokeJoin(Paint.Join.ROUND);
        centerAxis.setStrokeCap(Paint.Cap.ROUND);
        centerAxis.setPathEffect(new DashPathEffect(new float[]{dp(8), dp(5)}, 0f));
        strikerRing.setShadowLayer(dp(5), 0f, 0f, Color.argb(110, 35, 244, 235));
        impactFill.setShadowLayer(dp(4), 0f, 0f, Color.argb(145, 35, 244, 235));
        ghostFill.setShadowLayer(dp(7), 0f, 0f, Color.argb(80, 35, 244, 235));
        ghostRing.setPathEffect(new DashPathEffect(new float[]{dp(6), dp(3)}, 0f));
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
            // Keep repainting while aiming so an old guide disappears as soon as a
            // native sample becomes stale instead of visually sticking on screen.
            postInvalidateOnAnimation();
            return;
        }

        float touchAimX = startX - currentX;
        float touchAimY = startY - currentY;
        float drag = (float) Math.hypot(touchAimX, touchAimY);
        if (drag < dp(10)) {
            postInvalidateOnAnimation();
            return;
        }

        RectF board = estimateBoardRect();
        PointF origin = mapNativeWorldToBoard(board, nativeState);
        if (origin == null) {
            postInvalidateOnAnimation();
            return;
        }

        PointF direction = nativeDirection(nativeState.angle, touchAimX, touchAimY);
        if (!Float.isFinite(direction.x) || !Float.isFinite(direction.y)) {
            postInvalidateOnAnimation();
            return;
        }

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
        if (prediction.segments.isEmpty()) {
            postInvalidateOnAnimation();
            return;
        }

        drawStrikerMarker(canvas, origin.x, origin.y, prediction.discRadiusPx);
        for (int i = 0; i < prediction.segments.size(); i++) {
            ShotPredictor.Segment segment = prediction.segments.get(i);
            drawRailSegment(canvas, segment, prediction.discRadiusPx, i);
            if (segment.cushionHit && i < prediction.segments.size() - 1) {
                drawImpact(canvas, segment.to.x, segment.to.y, i);
            }
        }

        if (prediction.complete) {
            drawGhostStop(canvas, prediction.stop.x, prediction.stop.y, prediction.discRadiusPx);
        } else {
            drawCutoff(canvas, prediction.stop.x, prediction.stop.y);
        }

        // Native aim callbacks can continue changing even when the finger is nearly
        // stationary. Redraw every frame while the gesture is active.
        postInvalidateOnAnimation();
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

    /**
     * Carrom's world angle is x-right/y-up. Android's canvas is x-right/y-down,
     * therefore screen direction is (cos(angle), -sin(angle)). The old renderer
     * also tried swapped sin/cos candidates; that could rotate a valid native angle
     * by 90 degrees and produced the horizontal cyan line seen in testing.
     */
    private PointF nativeDirection(double angle, float touchX, float touchY) {
        if (!Double.isFinite(angle)) return new PointF(Float.NaN, Float.NaN);

        float dx = (float) Math.cos(angle);
        float dy = (float) -Math.sin(angle);
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.001f) return new PointF(Float.NaN, Float.NaN);
        dx /= len;
        dy /= len;

        // The native angle describes an axis in some aim states. Use the drag only
        // to choose the forward half of that same axis; never swap x/y components.
        float touchLen = (float) Math.hypot(touchX, touchY);
        if (touchLen >= 0.001f) {
            float tx = touchX / touchLen;
            float ty = touchY / touchLen;
            if (dx * tx + dy * ty < 0f) {
                dx = -dx;
                dy = -dy;
            }
        }
        return new PointF(dx, dy);
    }

    private void drawStrikerMarker(Canvas canvas, float x, float y, float radius) {
        canvas.drawCircle(x, y, radius * 0.93f, ghostFill);
        canvas.drawCircle(x, y, radius * 0.93f, strikerRing);
        canvas.drawCircle(x, y, dp(2.2f), strikerCore);
    }

    private void drawRailSegment(Canvas canvas, ShotPredictor.Segment segment,
                                 float discRadius, int index) {
        float vx = segment.to.x - segment.from.x;
        float vy = segment.to.y - segment.from.y;
        float length = (float) Math.hypot(vx, vy);
        if (length < 0.5f) return;

        float nx = -vy / length;
        float ny = vx / length;
        float halfWidth = Math.max(dp(6f), discRadius * (index == 0 ? 0.82f : 0.62f));
        float fade = Math.max(0.42f, 1f - index * 0.17f);

        float ax1 = segment.from.x + nx * halfWidth;
        float ay1 = segment.from.y + ny * halfWidth;
        float ax2 = segment.to.x + nx * halfWidth;
        float ay2 = segment.to.y + ny * halfWidth;
        float bx1 = segment.from.x - nx * halfWidth;
        float by1 = segment.from.y - ny * halfWidth;
        float bx2 = segment.to.x - nx * halfWidth;
        float by2 = segment.to.y - ny * halfWidth;

        halo.setAlpha(Math.round(55f * fade));
        edge.setAlpha(Math.round(235f * fade));
        centerAxis.setAlpha(Math.round(170f * fade));

        // Wide glow first, then the two crisp rails and a subtle center axis.
        canvas.drawLine(ax1, ay1, ax2, ay2, halo);
        canvas.drawLine(bx1, by1, bx2, by2, halo);
        canvas.drawLine(ax1, ay1, ax2, ay2, edge);
        canvas.drawLine(bx1, by1, bx2, by2, edge);
        canvas.drawLine(segment.from.x, segment.from.y,
                segment.to.x, segment.to.y, centerAxis);
    }

    private void drawImpact(Canvas canvas, float x, float y, int index) {
        impactFill.setAlpha(Math.max(115, 220 - index * 35));
        impactRing.setAlpha(Math.max(90, 205 - index * 28));
        canvas.drawCircle(x, y, dp(2.2f), impactFill);
        canvas.drawCircle(x, y, dp(5.4f), impactRing);
    }

    private void drawGhostStop(Canvas canvas, float x, float y, float radius) {
        canvas.drawCircle(x, y, radius, ghostFill);
        canvas.drawCircle(x, y, radius, ghostRing);
        canvas.drawCircle(x, y, Math.max(dp(2f), radius * 0.13f), impactFill);
    }

    private void drawCutoff(Canvas canvas, float x, float y) {
        impactRing.setAlpha(155);
        canvas.drawCircle(x, y, dp(5.7f), impactRing);
        impactFill.setAlpha(190);
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

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
