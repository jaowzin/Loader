package dev.jaowzin.carromloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

final class TrajectoryOverlayView extends View {
    private static final int CYAN = Color.rgb(91, 245, 218);
    private static final int ICE = Color.rgb(218, 255, 249);
    private static final int BLUE = Color.rgb(75, 214, 255);

    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint core = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint startDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint impactFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint impactRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ghostFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ghostRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ghostCore = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        glow.setStyle(Paint.Style.STROKE);
        glow.setStrokeCap(Paint.Cap.ROUND);
        glow.setStrokeJoin(Paint.Join.ROUND);
        glow.setStrokeWidth(dp(8.5f));
        glow.setColor(Color.argb(52, 91, 245, 218));
        glow.setShadowLayer(dp(7), 0f, 0f, Color.argb(115, 91, 245, 218));

        rail.setStyle(Paint.Style.STROKE);
        rail.setStrokeCap(Paint.Cap.ROUND);
        rail.setStrokeJoin(Paint.Join.ROUND);
        rail.setStrokeWidth(dp(4.2f));
        rail.setColor(Color.argb(90, 255, 255, 255));

        core.setStyle(Paint.Style.STROKE);
        core.setStrokeCap(Paint.Cap.ROUND);
        core.setStrokeJoin(Paint.Join.ROUND);
        core.setStrokeWidth(dp(2.15f));

        startDot.setStyle(Paint.Style.FILL);
        startDot.setColor(Color.WHITE);
        startDot.setShadowLayer(dp(5), 0f, 0f, Color.argb(190, 91, 245, 218));

        impactFill.setStyle(Paint.Style.FILL);
        impactFill.setColor(Color.argb(225, 218, 255, 249));
        impactFill.setShadowLayer(dp(5), 0f, 0f, Color.argb(180, 91, 245, 218));

        impactRing.setStyle(Paint.Style.STROKE);
        impactRing.setStrokeWidth(dp(1.4f));
        impactRing.setColor(Color.argb(170, 91, 245, 218));

        ghostFill.setStyle(Paint.Style.FILL);
        ghostFill.setColor(Color.argb(43, 91, 245, 218));
        ghostFill.setShadowLayer(dp(9), 0f, 0f, Color.argb(105, 91, 245, 218));

        ghostRing.setStyle(Paint.Style.STROKE);
        ghostRing.setStrokeWidth(dp(1.7f));
        ghostRing.setColor(Color.argb(220, 155, 255, 236));
        ghostRing.setPathEffect(new DashPathEffect(new float[]{dp(6), dp(3)}, 0f));

        ghostCore.setStyle(Paint.Style.FILL);
        ghostCore.setColor(Color.argb(215, 230, 255, 251));
    }

    void observeTouch(MotionEvent event) {
        if (event == null) return;
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
            }, 1100L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active || getWidth() <= 0 || getHeight() <= 0) return;

        float aimX = startX - currentX;
        float aimY = startY - currentY;
        float drag = (float) Math.hypot(aimX, aimY);
        if (drag < dp(10)) return;

        RectF board = estimateBoardRect();
        float discRadius = board.width() * 0.0325f;
        int bounces = bankPreview ? 3 : 0;
        ShotPredictor.Prediction prediction = predictor.predict(
                board,
                discRadius,
                startX,
                startY,
                aimX,
                aimY,
                drag,
                bounces
        );
        if (prediction.segments.isEmpty()) return;

        ShotPredictor.Segment first = prediction.segments.get(0);
        canvas.drawCircle(first.from.x, first.from.y, dp(3.1f), startDot);

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
            // Bank preview depth ended at a cushion. A small ring communicates that the
            // visible guide was intentionally truncated rather than claiming a false stop.
            drawCutoff(canvas, prediction.stop.x, prediction.stop.y);
        }
    }

    private void drawSegment(Canvas canvas, ShotPredictor.Segment segment, int index) {
        int fade = Math.max(95, 255 - index * 46);
        glow.setAlpha(Math.max(18, 55 - index * 9));
        rail.setAlpha(Math.max(28, 92 - index * 13));

        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, glow);
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, rail);

        int fromColor = withAlpha(index == 0 ? ICE : CYAN, fade);
        int toColor = withAlpha(index == 0 ? CYAN : BLUE, Math.max(80, fade - 35));
        core.setShader(new LinearGradient(
                segment.from.x, segment.from.y,
                segment.to.x, segment.to.y,
                fromColor, toColor,
                Shader.TileMode.CLAMP
        ));
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, core);
        core.setShader(null);
    }

    private void drawImpact(Canvas canvas, float x, float y, int index) {
        impactFill.setAlpha(Math.max(125, 225 - index * 35));
        impactRing.setAlpha(Math.max(95, 180 - index * 25));
        canvas.drawCircle(x, y, dp(2.6f), impactFill);
        canvas.drawCircle(x, y, dp(6.3f), impactRing);
    }

    private void drawGhostStop(Canvas canvas, float x, float y, float radius) {
        canvas.drawCircle(x, y, radius, ghostFill);
        canvas.drawCircle(x, y, radius, ghostRing);
        canvas.drawCircle(x, y, Math.max(dp(2.2f), radius * 0.17f), ghostCore);
    }

    private void drawCutoff(Canvas canvas, float x, float y) {
        impactRing.setAlpha(150);
        canvas.drawCircle(x, y, dp(7f), impactRing);
        impactFill.setAlpha(180);
        canvas.drawCircle(x, y, dp(2.1f), impactFill);
    }

    /**
     * The CTF layout uses a portrait board occupying almost the full screen width.
     * Keeping this estimation isolated makes it easy to replace with detected board
     * corners once the frame sampler is connected.
     */
    private RectF estimateBoardRect() {
        float w = getWidth();
        float h = getHeight();
        if (h > w * 1.28f) {
            float side = w * 0.955f;
            float left = (w - side) * 0.5f;
            float top = h * 0.245f;
            if (top + side > h - dp(20)) top = Math.max(dp(20), h - side - dp(20));
            return new RectF(left, top, left + side, top + side);
        }
        float margin = Math.max(dp(16), Math.min(w, h) * 0.035f);
        return new RectF(margin, margin, w - margin, h - margin);
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
