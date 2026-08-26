package dev.jaowzin.carromloader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayDeque;

/**
 * Lightweight clean-room frame detector for the CTF board.
 *
 * It intentionally uses only coarse color/shape cues:
 *  - the wooden board is the largest contiguous warm/tan region;
 *  - the striker is the bright, roughly circular component in the lower board zone.
 *
 * This is not tied to a specific skin texture and does not read game memory.
 */
final class BoardVision {
    static final class State {
        final RectF board;
        final PointF striker;
        final float strikerRadius;
        final float confidence;
        final long capturedAtMs;

        State(RectF board, PointF striker, float strikerRadius, float confidence, long capturedAtMs) {
            this.board = board;
            this.striker = striker;
            this.strikerRadius = strikerRadius;
            this.confidence = confidence;
            this.capturedAtMs = capturedAtMs;
        }

        boolean usable() {
            return board != null && striker != null && confidence >= 0.55f;
        }
    }

    private BoardVision() {}

    static State analyze(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() < 80 || bitmap.getHeight() < 80) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int scanStep = Math.max(2, Math.min(width, height) / 260);

        Span rows = findBoardRows(bitmap, scanStep);
        if (rows == null) return null;

        Span cols = findBoardColumns(bitmap, rows, scanStep);
        if (cols == null) return null;

        RectF outer = new RectF(cols.start, rows.start, cols.end, rows.end);
        float minSide = Math.min(outer.width(), outer.height());
        if (minSide < Math.min(width, height) * 0.35f) return null;

        // The warm-color span includes the decorative/orange rail. Physics should use
        // the inner cushion surface, so move inward by a small fraction of board size.
        float cushionInset = minSide * 0.045f;
        RectF playable = new RectF(
                outer.left + cushionInset,
                outer.top + cushionInset,
                outer.right - cushionInset,
                outer.bottom - cushionInset
        );

        CircleCandidate striker = findStriker(bitmap, outer, scanStep);
        if (striker == null) {
            return new State(playable, null, minSide * 0.034f, 0.35f, System.currentTimeMillis());
        }

        float boardScore = clamp01(minSide / (Math.min(width, height) * 0.72f));
        float confidence = clamp01(0.48f * boardScore + 0.52f * striker.score);
        return new State(
                playable,
                new PointF(striker.x, striker.y),
                striker.radius,
                confidence,
                System.currentTimeMillis()
        );
    }

    private static Span findBoardRows(Bitmap bitmap, int step) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int samplesPerRow = Math.max(1, (width + step - 1) / step);

        boolean[] accepted = new boolean[height];
        for (int y = 0; y < height; y += step) {
            int warm = 0;
            for (int x = 0; x < width; x += step) {
                if (isBoardWood(bitmap.getPixel(x, y))) warm++;
            }
            float ratio = warm / (float) samplesPerRow;
            boolean ok = ratio >= 0.39f;
            for (int yy = y; yy < Math.min(height, y + step); yy++) accepted[yy] = ok;
        }
        return largestSpan(accepted, Math.max(24, (int) (height * 0.20f)));
    }

    private static Span findBoardColumns(Bitmap bitmap, Span rows, int step) {
        int width = bitmap.getWidth();
        int yStart = Math.max(0, rows.start);
        int yEnd = Math.min(bitmap.getHeight(), rows.end);
        int sampleRows = Math.max(1, (yEnd - yStart + step - 1) / step);

        boolean[] accepted = new boolean[width];
        for (int x = 0; x < width; x += step) {
            int warm = 0;
            for (int y = yStart; y < yEnd; y += step) {
                if (isBoardWood(bitmap.getPixel(x, y))) warm++;
            }
            float ratio = warm / (float) sampleRows;
            boolean ok = ratio >= 0.34f;
            for (int xx = x; xx < Math.min(width, x + step); xx++) accepted[xx] = ok;
        }
        return largestSpan(accepted, Math.max(24, (int) (width * 0.38f)));
    }

    private static CircleCandidate findStriker(Bitmap bitmap, RectF board, int coarseStep) {
        float boardMin = Math.min(board.width(), board.height());
        int step = Math.max(2, Math.max(coarseStep, Math.round(boardMin / 210f)));

        int left = Math.max(0, Math.round(board.left));
        int right = Math.min(bitmap.getWidth() - 1, Math.round(board.right));
        int top = Math.max(0, Math.round(board.top + board.height() * 0.66f));
        int bottom = Math.min(bitmap.getHeight() - 1, Math.round(board.bottom));
        if (right <= left || bottom <= top) return null;

        int gridW = Math.max(1, (right - left) / step + 1);
        int gridH = Math.max(1, (bottom - top) / step + 1);
        boolean[] bright = new boolean[gridW * gridH];
        boolean[] visited = new boolean[bright.length];

        for (int gy = 0; gy < gridH; gy++) {
            int y = Math.min(bottom, top + gy * step);
            for (int gx = 0; gx < gridW; gx++) {
                int x = Math.min(right, left + gx * step);
                bright[gy * gridW + gx] = isStrikerBright(bitmap.getPixel(x, y));
            }
        }

        float minDiameter = boardMin * 0.045f;
        float maxDiameter = boardMin * 0.115f;
        CircleCandidate best = null;
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int index = 0; index < bright.length; index++) {
            if (!bright[index] || visited[index]) continue;

            queue.clear();
            queue.add(index);
            visited[index] = true;

            int cells = 0;
            int minGX = Integer.MAX_VALUE;
            int minGY = Integer.MAX_VALUE;
            int maxGX = Integer.MIN_VALUE;
            int maxGY = Integer.MIN_VALUE;
            long sumGX = 0;
            long sumGY = 0;

            while (!queue.isEmpty()) {
                int current = queue.removeFirst();
                int gy = current / gridW;
                int gx = current - gy * gridW;
                cells++;
                sumGX += gx;
                sumGY += gy;
                minGX = Math.min(minGX, gx);
                maxGX = Math.max(maxGX, gx);
                minGY = Math.min(minGY, gy);
                maxGY = Math.max(maxGY, gy);

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = gx + dx;
                        int ny = gy + dy;
                        if (nx < 0 || nx >= gridW || ny < 0 || ny >= gridH) continue;
                        int next = ny * gridW + nx;
                        if (bright[next] && !visited[next]) {
                            visited[next] = true;
                            queue.addLast(next);
                        }
                    }
                }
            }

            if (cells < 5) continue;
            float componentW = (maxGX - minGX + 1) * step;
            float componentH = (maxGY - minGY + 1) * step;
            float diameter = Math.max(componentW, componentH);
            if (diameter < minDiameter || diameter > maxDiameter) continue;

            float aspect = componentW / Math.max(1f, componentH);
            if (aspect < 0.62f || aspect > 1.55f) continue;

            float centerGX = sumGX / (float) cells;
            float centerGY = sumGY / (float) cells;
            float cx = left + centerGX * step;
            float cy = top + centerGY * step;
            float yNorm = (cy - board.top) / Math.max(1f, board.height());
            if (yNorm < 0.72f) continue;

            float boxArea = Math.max(1f, componentW * componentH);
            float fill = clamp01((cells * step * step) / boxArea);
            float roundness = 1f - Math.min(1f, Math.abs(1f - aspect));
            float sizeFit = 1f - Math.min(1f,
                    Math.abs(diameter - boardMin * 0.072f) / (boardMin * 0.072f));
            float lowerBias = clamp01((yNorm - 0.72f) / 0.24f);
            float score = clamp01(0.34f * fill + 0.26f * roundness + 0.24f * sizeFit + 0.16f * lowerBias);

            CircleCandidate candidate = new CircleCandidate(cx, cy, diameter * 0.5f, score);
            if (best == null || candidate.score > best.score) best = candidate;
        }
        return best;
    }

    private static boolean isBoardWood(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return r > 100
                && g > 58
                && b > 26
                && r - g > 13
                && g - b > 4
                && r - b > 30;
    }

    private static boolean isStrikerBright(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        return r >= 178 && g >= 168 && b >= 145 && max - min <= 78;
    }

    private static Span largestSpan(boolean[] values, int minimumLength) {
        int bestStart = -1;
        int bestEnd = -1;
        int start = -1;
        for (int i = 0; i <= values.length; i++) {
            boolean value = i < values.length && values[i];
            if (value && start < 0) {
                start = i;
            } else if (!value && start >= 0) {
                int end = i;
                if (end - start >= minimumLength && end - start > bestEnd - bestStart) {
                    bestStart = start;
                    bestEnd = end;
                }
                start = -1;
            }
        }
        return bestStart >= 0 ? new Span(bestStart, bestEnd) : null;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static final class Span {
        final int start;
        final int end;

        Span(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class CircleCandidate {
        final float x;
        final float y;
        final float radius;
        final float score;

        CircleCandidate(float x, float y, float radius, float score) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.score = score;
        }
    }
}
