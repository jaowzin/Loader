package dev.jaowzin.carromloader.carrom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure geometry used by the future line renderer. It knows nothing about Android,
 * hooking or the Carrom process: given an origin, direction and calibrated table
 * rectangle, it returns reflected trajectory segments.
 */
public final class TrajectorySolver {
    private static final float EPSILON = 0.0001f;

    public static final class Segment {
        public final float x1;
        public final float y1;
        public final float x2;
        public final float y2;

        Segment(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static final class Result {
        public final List<Segment> segments;
        public final float finalDx;
        public final float finalDy;

        Result(List<Segment> segments, float finalDx, float finalDy) {
            this.segments = Collections.unmodifiableList(segments);
            this.finalDx = finalDx;
            this.finalDy = finalDy;
        }
    }

    private TrajectorySolver() {}

    public static Result solve(float originX, float originY, float directionX, float directionY,
                               float left, float top, float right, float bottom, int maxBounces) {
        float length = (float) Math.hypot(directionX, directionY);
        if (length < EPSILON) return new Result(new ArrayList<>(), 0f, 0f);
        float dx = directionX / length;
        float dy = directionY / length;
        float x = originX;
        float y = originY;
        List<Segment> segments = new ArrayList<>();

        int steps = Math.max(0, maxBounces) + 1;
        for (int i = 0; i < steps; i++) {
            Hit hit = nextHit(x, y, dx, dy, left, top, right, bottom);
            if (hit == null) break;
            segments.add(new Segment(x, y, hit.x, hit.y));
            x = hit.x;
            y = hit.y;
            if (hit.vertical) dx = -dx;
            if (hit.horizontal) dy = -dy;
            x += dx * EPSILON * 10f;
            y += dy * EPSILON * 10f;
        }
        return new Result(segments, dx, dy);
    }

    private static Hit nextHit(float x, float y, float dx, float dy,
                               float left, float top, float right, float bottom) {
        float bestT = Float.POSITIVE_INFINITY;
        float hitX = 0f;
        float hitY = 0f;
        boolean vertical = false;
        boolean horizontal = false;

        if (Math.abs(dx) > EPSILON) {
            float wallX = dx > 0 ? right : left;
            float t = (wallX - x) / dx;
            if (t > EPSILON) {
                float candidateY = y + dy * t;
                if (candidateY >= top - EPSILON && candidateY <= bottom + EPSILON) {
                    bestT = t;
                    hitX = wallX;
                    hitY = candidateY;
                    vertical = true;
                }
            }
        }

        if (Math.abs(dy) > EPSILON) {
            float wallY = dy > 0 ? bottom : top;
            float t = (wallY - y) / dy;
            if (t > EPSILON) {
                float candidateX = x + dx * t;
                if (candidateX >= left - EPSILON && candidateX <= right + EPSILON) {
                    if (Math.abs(t - bestT) <= EPSILON) {
                        hitX = candidateX;
                        hitY = wallY;
                        horizontal = true;
                    } else if (t < bestT) {
                        bestT = t;
                        hitX = candidateX;
                        hitY = wallY;
                        vertical = false;
                        horizontal = true;
                    }
                }
            }
        }

        if (!Float.isFinite(bestT)) return null;
        return new Hit(hitX, hitY, vertical, horizontal);
    }

    private static final class Hit {
        final float x;
        final float y;
        final boolean vertical;
        final boolean horizontal;

        Hit(float x, float y, boolean vertical, boolean horizontal) {
            this.x = x;
            this.y = y;
            this.vertical = vertical;
            this.horizontal = horizontal;
        }
    }
}
