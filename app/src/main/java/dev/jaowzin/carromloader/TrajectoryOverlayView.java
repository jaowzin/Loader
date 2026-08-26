package dev.jaowzin.carromloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class TrajectoryOverlayView extends View {
    private static final String TAG = "carrom_line";
    private static final int CYAN = Color.rgb(91, 245, 218);
    private static final int ICE = Color.rgb(218, 255, 249);
    private static final int BLUE = Color.rgb(75, 214, 255);
    private static final long FRAME_INTERVAL_MS = 520L;
    private static final long VISION_MAX_AGE_MS = 3500L;

    private final Paint glow = stroke(Color.argb(52, 91, 245, 218), 8.5f);
    private final Paint rail = stroke(Color.argb(90, 255, 255, 255), 4.2f);
    private final Paint core = stroke(CYAN, 2.15f);
    private final Paint startDot = fill(Color.WHITE);
    private final Paint impactFill = fill(Color.argb(225, 218, 255, 249));
    private final Paint impactRing = stroke(Color.argb(170, 91, 245, 218), 1.4f);
    private final Paint ghostFill = fill(Color.argb(43, 91, 245, 218));
    private final Paint ghostRing = stroke(Color.argb(220, 155, 255, 236), 1.7f);
    private final Paint ghostCore = fill(Color.argb(215, 230, 255, 251));
    private final ShotPredictor predictor = new ShotPredictor();
    private final boolean bankPreview;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService visionWorker = Executors.newSingleThreadExecutor();

    private Window visionWindow;
    private BoardVision.State visionState;
    private boolean visionRunning;
    private boolean captureInFlight;
    private float startX;
    private float startY;
    private float currentX;
    private float currentY;
    private boolean active;
    private int clearGeneration;
    private final Runnable captureLoop = this::captureFrame;

    TrajectoryOverlayView(Context context, boolean bankPreview) {
        super(context);
        this.bankPreview = bankPreview;
        setClickable(false);
        setFocusable(false);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        glow.setStrokeCap(Paint.Cap.ROUND);
        glow.setStrokeJoin(Paint.Join.ROUND);
        glow.setShadowLayer(dp(7), 0f, 0f, Color.argb(115, 91, 245, 218));
        rail.setStrokeCap(Paint.Cap.ROUND);
        rail.setStrokeJoin(Paint.Join.ROUND);
        core.setStrokeCap(Paint.Cap.ROUND);
        core.setStrokeJoin(Paint.Join.ROUND);
        startDot.setShadowLayer(dp(5), 0f, 0f, Color.argb(190, 91, 245, 218));
        impactFill.setShadowLayer(dp(5), 0f, 0f, Color.argb(180, 91, 245, 218));
        ghostFill.setShadowLayer(dp(9), 0f, 0f, Color.argb(105, 91, 245, 218));
        ghostRing.setPathEffect(new DashPathEffect(new float[]{dp(6), dp(3)}, 0f));
    }

    void startVision(Window window) {
        if (window == null) return;
        visionWindow = window;
        NativeAimBridge.ensureStarted();
        if (visionRunning) return;
        visionRunning = true;
        mainHandler.removeCallbacks(captureLoop);
        mainHandler.postDelayed(captureLoop, 180L);
    }

    void stopVision() {
        visionRunning = false;
        captureInFlight = false;
        visionWindow = null;
        mainHandler.removeCallbacks(captureLoop);
        visionWorker.shutdownNow();
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
            }, 1100L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!active || getWidth() <= 0 || getHeight() <= 0) return;

        float touchAimX = startX - currentX;
        float touchAimY = startY - currentY;
        float drag = (float) Math.hypot(touchAimX, touchAimY);
        if (drag < dp(10)) return;

        BoardVision.State detected = recentVision();
        RectF board = detected != null && detected.board != null
                ? new RectF(detected.board)
                : estimateBoardRect();

        NativeAimBridge.State nativeState = NativeAimBridge.read();
        PointF nativeOrigin = mapNativeWorldToBoard(board, nativeState);

        float originX;
        float originY;
        if (nativeOrigin != null) {
            originX = nativeOrigin.x;
            originY = nativeOrigin.y;
        } else if (detected != null && detected.usable()) {
            originX = detected.striker.x;
            originY = detected.striker.y;
        } else {
            originX = startX;
            originY = startY;
        }

        float aimX = touchAimX;
        float aimY = touchAimY;
        if (nativeState != null && nativeState.angleFresh()) {
            PointF direction = nativeDirection(nativeState.angle, touchAimX, touchAimY);
            aimX = direction.x;
            aimY = direction.y;
        }

        float discRadius = detected != null && detected.usable()
                ? detected.strikerRadius
                : board.width() * 0.0325f;

        ShotPredictor.Prediction prediction = predictor.predict(
                board,
                discRadius,
                originX,
                originY,
                aimX,
                aimY,
                drag,
                bankPreview ? 3 : 0
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
            drawCutoff(canvas, prediction.stop.x, prediction.stop.y);
        }
    }

    /**
     * Carrom 19.3.0 stores the physics position as MCPoint. The game also contains
     * convertPointToInches:/convertInchesToPoint:, so the first native calibration
     * accepts the two plausible units used by this engine: meters or inches.
     */
    private PointF mapNativeWorldToBoard(RectF board, NativeAimBridge.State state) {
        if (state == null || !state.positionFresh()) return null;
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
        float pad = board.width() * 0.08f;
        if (sx < board.left - pad || sx > board.right + pad ||
                sy < board.top - pad || sy > board.bottom + pad) {
            return null;
        }
        return new PointF(sx, sy);
    }

    private PointF nativeDirection(double angle, float touchX, float touchY) {
        float touchLen = (float) Math.hypot(touchX, touchY);
        if (touchLen < 0.001f || !Double.isFinite(angle)) return new PointF(touchX, touchY);
        float tx = touchX / touchLen;
        float ty = touchY / touchLen;
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float[][] candidates = {
                {c, s}, {c, -s}, {-c, s}, {-c, -s},
                {s, c}, {s, -c}, {-s, c}, {-s, -c}
        };
        float bestDot = -Float.MAX_VALUE;
        float bestX = tx;
        float bestY = ty;
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

    private void captureFrame() {
        if (!visionRunning || captureInFlight || visionWindow == null) return;
        if (active) {
            mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
            return;
        }

        View decor = visionWindow.getDecorView();
        int width = decor.getWidth();
        int height = decor.getHeight();
        if (width < 80 || height < 80 || !decor.isAttachedToWindow()) {
            mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (Throwable error) {
            mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS * 2);
            return;
        }

        captureInFlight = true;
        try {
            PixelCopy.request(visionWindow, bitmap, result -> {
                if (!visionRunning || result != PixelCopy.SUCCESS) {
                    captureInFlight = false;
                    bitmap.recycle();
                    if (visionRunning) mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
                    return;
                }
                try {
                    visionWorker.execute(() -> {
                        BoardVision.State state = null;
                        try {
                            state = BoardVision.analyze(bitmap);
                        } catch (Throwable error) {
                            Log.w(TAG, "frame analysis failed", error);
                        } finally {
                            bitmap.recycle();
                        }
                        BoardVision.State finalState = state;
                        mainHandler.post(() -> {
                            captureInFlight = false;
                            if (!visionRunning) return;
                            if (finalState != null) visionState = finalState;
                            mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
                        });
                    });
                } catch (Throwable error) {
                    captureInFlight = false;
                    bitmap.recycle();
                    mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
                }
            }, mainHandler);
        } catch (Throwable error) {
            captureInFlight = false;
            bitmap.recycle();
            mainHandler.postDelayed(captureLoop, FRAME_INTERVAL_MS);
        }
    }

    private BoardVision.State recentVision() {
        BoardVision.State state = visionState;
        if (state == null) return null;
        if (System.currentTimeMillis() - state.capturedAtMs > VISION_MAX_AGE_MS) return null;
        return state;
    }

    private void drawSegment(Canvas canvas, ShotPredictor.Segment segment, int index) {
        int fade = Math.max(95, 255 - index * 46);
        glow.setAlpha(Math.max(18, 55 - index * 9));
        rail.setAlpha(Math.max(28, 92 - index * 13));
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, glow);
        canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, rail);
        core.setShader(new LinearGradient(
                segment.from.x, segment.from.y,
                segment.to.x, segment.to.y,
                withAlpha(index == 0 ? ICE : CYAN, fade),
                withAlpha(index == 0 ? CYAN : BLUE, Math.max(80, fade - 35)),
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

    private Paint stroke(int color, float widthDp) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(widthDp));
        return setColor(paint, color);
    }

    private Paint fill(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        return setColor(paint, color);
    }

    private Paint setColor(Paint paint, int color) {
        paint.setColor(color);
        return paint;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)),
                Color.red(color), Color.green(color), Color.blue(color));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
