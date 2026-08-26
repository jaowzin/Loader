package dev.jaowzin.carromloader;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small clean-room 2D predictor used by the visual guide.
 *
 * Coordinates are pixels, while the speed/friction model is evaluated in meters so
 * predictions stay reasonably similar across screen sizes. Shot power is supplied as
 * the game's normalized 0..1 value captured from ControlsLogicLocal.
 */
final class ShotPredictor {
    private static final float BOARD_WIDTH_METERS = 0.74f;
    private static final float FRICTION_ACCEL_MPS2 = 1.55f;
    private static final float CUSHION_RESTITUTION = 0.78f;
    private static final float MIN_SPEED_MPS = 0.07f;
    private static final int HARD_BOUNCE_LIMIT = 5;

    static final class Segment {
        final PointF from;
        final PointF to;
        final boolean cushionHit;
        final int index;

        Segment(float x1, float y1, float x2, float y2, boolean cushionHit, int index) {
            this.from = new PointF(x1, y1);
            this.to = new PointF(x2, y2);
            this.cushionHit = cushionHit;
            this.index = index;
        }
    }

    static final class Prediction {
        final List<Segment> segments;
        final PointF stop;
        final float discRadiusPx;
        final float power;
        final float initialSpeedMps;
        final boolean complete;

        Prediction(List<Segment> segments, PointF stop, float discRadiusPx,
                   float power, float initialSpeedMps, boolean complete) {
            this.segments = Collections.unmodifiableList(segments);
            this.stop = stop;
            this.discRadiusPx = discRadiusPx;
            this.power = power;
            this.initialSpeedMps = initialSpeedMps;
            this.complete = complete;
        }
    }

    Prediction predict(RectF board, float discRadiusPx,
                       float startX, float startY,
                       float directionX, float directionY,
                       float normalizedPower, int visibleBounces) {
        ArrayList<Segment> segments = new ArrayList<>();
        if (board == null || board.width() < 1f || board.height() < 1f) {
            return new Prediction(segments, new PointF(startX, startY), discRadiusPx, 0f, 0f, false);
        }

        float magnitude = (float) Math.hypot(directionX, directionY);
        if (magnitude < 0.001f) {
            return new Prediction(segments, new PointF(startX, startY), discRadiusPx, 0f, 0f, false);
        }
        float dx = directionX / magnitude;
        float dy = directionY / magnitude;

        RectF playable = new RectF(
                board.left + discRadiusPx,
                board.top + discRadiusPx,
                board.right - discRadiusPx,
                board.bottom - discRadiusPx
        );
        if (playable.width() <= 1f || playable.height() <= 1f) {
            return new Prediction(segments, new PointF(startX, startY), discRadiusPx, 0f, 0f, false);
        }

        float x = clamp(startX, playable.left, playable.right);
        float y = clamp(startY, playable.top, playable.bottom);
        float power = clamp(normalizedPower, 0f, 1f);
        if (power <= 0.005f) {
            return new Prediction(segments, new PointF(x, y), discRadiusPx, power, 0f, true);
        }

        // Temporary local calibration of native 0..1 power to physical speed. The
        // important change here is that line length now follows the game's own power
        // state rather than Android touch distance.
        float initialSpeed = 0.45f + 3.45f * (float) Math.pow(power, 0.78f);
        float speed = initialSpeed;
        float metersPerPixel = BOARD_WIDTH_METERS / board.width();

        int bounceLimit = Math.min(Math.max(0, visibleBounces), HARD_BOUNCE_LIMIT);
        int bounces = 0;
        boolean complete = false;
        PointF stop = new PointF(x, y);

        for (int segmentIndex = 0; segmentIndex <= HARD_BOUNCE_LIMIT; segmentIndex++) {
            if (speed <= MIN_SPEED_MPS) {
                complete = true;
                stop.set(x, y);
                break;
            }

            float tx = Float.POSITIVE_INFINITY;
            float ty = Float.POSITIVE_INFINITY;
            if (dx > 0.00001f) tx = (playable.right - x) / dx;
            else if (dx < -0.00001f) tx = (playable.left - x) / dx;
            if (dy > 0.00001f) ty = (playable.bottom - y) / dy;
            else if (dy < -0.00001f) ty = (playable.top - y) / dy;

            float wallDistancePx = Math.min(tx, ty);
            if (!Float.isFinite(wallDistancePx) || wallDistancePx <= 0f) {
                stop.set(x, y);
                break;
            }

            float stopDistanceMeters = Math.max(0f,
                    (speed * speed - MIN_SPEED_MPS * MIN_SPEED_MPS)
                            / (2f * FRICTION_ACCEL_MPS2));
            float stopDistancePx = stopDistanceMeters / metersPerPixel;

            if (stopDistancePx <= wallDistancePx) {
                float endX = x + dx * stopDistancePx;
                float endY = y + dy * stopDistancePx;
                segments.add(new Segment(x, y, endX, endY, false, segmentIndex));
                stop.set(endX, endY);
                complete = true;
                break;
            }

            float endX = x + dx * wallDistancePx;
            float endY = y + dy * wallDistancePx;
            segments.add(new Segment(x, y, endX, endY, true, segmentIndex));
            stop.set(endX, endY);

            float travelledMeters = wallDistancePx * metersPerPixel;
            float impactSquared = Math.max(0f,
                    speed * speed - 2f * FRICTION_ACCEL_MPS2 * travelledMeters);
            float impactSpeed = (float) Math.sqrt(impactSquared);

            if (bounces >= bounceLimit) {
                complete = false;
                break;
            }

            boolean hitX = Math.abs(tx - wallDistancePx) < 0.75f;
            boolean hitY = Math.abs(ty - wallDistancePx) < 0.75f;
            if (hitX) dx = -dx;
            if (hitY) dy = -dy;

            speed = impactSpeed * CUSHION_RESTITUTION;
            bounces++;

            x = clamp(endX + dx * 0.35f, playable.left, playable.right);
            y = clamp(endY + dy * 0.35f, playable.top, playable.bottom);
        }

        return new Prediction(segments, stop, discRadiusPx, power, initialSpeed, complete);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
