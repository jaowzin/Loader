package dev.jaowzin.carromloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

final class TrajectoryOverlayView extends View {
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint point = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        glow.setColor(Color.argb(95, 91, 245, 201));
        glow.setStrokeWidth(dp(8));
        glow.setStrokeCap(Paint.Cap.ROUND);
        glow.setStyle(Paint.Style.STROKE);
        glow.setShadowLayer(dp(8), 0, 0, Color.argb(150, 91, 245, 201));

        line.setColor(Color.rgb(133, 255, 219));
        line.setStrokeWidth(dp(2.2f));
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStyle(Paint.Style.STROKE);

        point.setColor(Color.WHITE);
        point.setStyle(Paint.Style.FILL);
        point.setShadowLayer(dp(5), 0, 0, Color.argb(200, 91, 245, 201));
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
            }, 650L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active || getWidth() <= 0 || getHeight() <= 0) return;

        float dx = startX - currentX;
        float dy = startY - currentY;
        float length = (float) Math.hypot(dx, dy);
        if (length < dp(12)) return;
        dx /= length;
        dy /= length;

        float margin = dp(18);
        float minX = margin;
        float minY = margin;
        float maxX = getWidth() - margin;
        float maxY = getHeight() - margin;
        float x = clamp(startX, minX, maxX);
        float y = clamp(startY, minY, maxY);

        canvas.drawCircle(x, y, dp(4), point);

        int reflections = bankPreview ? 2 : 0;
        for (int segment = 0; segment <= reflections; segment++) {
            float tx = Float.POSITIVE_INFINITY;
            float ty = Float.POSITIVE_INFINITY;

            if (dx > 0.0001f) tx = (maxX - x) / dx;
            else if (dx < -0.0001f) tx = (minX - x) / dx;

            if (dy > 0.0001f) ty = (maxY - y) / dy;
            else if (dy < -0.0001f) ty = (minY - y) / dy;

            float t = Math.min(tx, ty);
            if (!Float.isFinite(t) || t <= 0f) break;

            float endX = x + dx * t;
            float endY = y + dy * t;
            canvas.drawLine(x, y, endX, endY, glow);
            canvas.drawLine(x, y, endX, endY, line);

            boolean hitX = Math.abs(tx - t) < 0.5f;
            boolean hitY = Math.abs(ty - t) < 0.5f;
            if (hitX) dx = -dx;
            if (hitY) dy = -dy;

            x = clamp(endX, minX, maxX);
            y = clamp(endY, minY, maxY);
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
