package dev.jaowzin.carromloader.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public final class GuideOverlayView extends View {
    private final Paint primary = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bounce = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint anchor = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float originX = Float.NaN;
    private float originY = Float.NaN;
    private float angleDegrees = -90f;
    private float totalLength = 1200f;

    public GuideOverlayView(Context context) {
        super(context);
        primary.setColor(0xE6FFFFFF);
        primary.setStrokeWidth(5f);
        bounce.setColor(0xD9FFD54F);
        bounce.setStrokeWidth(4f);
        anchor.setColor(0xF2FF5252);
        anchor.setStyle(Paint.Style.FILL);
        setBackgroundColor(0x00000000);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (Float.isNaN(originX) || Float.isNaN(originY)) {
            originX = w * 0.5f;
            originY = h * 0.72f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Float.isNaN(originX) || Float.isNaN(originY)) return;

        float radians = (float) Math.toRadians(angleDegrees);
        float dx = (float) Math.cos(radians);
        float dy = (float) Math.sin(radians);
        float hit = firstBoundaryDistance(originX, originY, dx, dy, getWidth(), getHeight());
        float firstLength = Math.min(totalLength, hit);

        float x1 = originX + dx * firstLength;
        float y1 = originY + dy * firstLength;
        canvas.drawLine(originX, originY, x1, y1, primary);
        canvas.drawCircle(originX, originY, 11f, anchor);

        float remaining = totalLength - firstLength;
        if (remaining > 1f && hit < Float.POSITIVE_INFINITY) {
            boolean verticalWall = x1 <= 1f || x1 >= getWidth() - 1f;
            boolean horizontalWall = y1 <= 1f || y1 >= getHeight() - 1f;
            if (verticalWall) dx = -dx;
            if (horizontalWall) dy = -dy;

            float hit2 = firstBoundaryDistance(x1, y1, dx, dy, getWidth(), getHeight());
            float secondLength = Math.min(remaining, hit2);
            float x2 = x1 + dx * secondLength;
            float y2 = y1 + dy * secondLength;
            canvas.drawLine(x1, y1, x2, y2, bounce);
        }
    }

    private static float firstBoundaryDistance(float x, float y, float dx, float dy, int width, int height) {
        float best = Float.POSITIVE_INFINITY;
        final float epsilon = 0.0001f;

        if (dx > epsilon) best = Math.min(best, (width - x) / dx);
        else if (dx < -epsilon) best = Math.min(best, (0f - x) / dx);

        if (dy > epsilon) best = Math.min(best, (height - y) / dy);
        else if (dy < -epsilon) best = Math.min(best, (0f - y) / dy);

        return best > 0f ? best : Float.POSITIVE_INFINITY;
    }

    public void rotate(float deltaDegrees) {
        angleDegrees = normalize(angleDegrees + deltaDegrees);
        invalidate();
    }

    public void changeLength(float delta) {
        totalLength = Math.max(150f, Math.min(4000f, totalLength + delta));
        invalidate();
    }

    public void move(float dx, float dy) {
        originX = clamp(originX + dx, 0f, Math.max(0f, getWidth()));
        originY = clamp(originY + dy, 0f, Math.max(0f, getHeight()));
        invalidate();
    }

    public float getAngleDegrees() {
        return angleDegrees;
    }

    public float getTotalLength() {
        return totalLength;
    }

    private static float normalize(float angle) {
        angle %= 360f;
        if (angle < 0f) angle += 360f;
        return angle;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
