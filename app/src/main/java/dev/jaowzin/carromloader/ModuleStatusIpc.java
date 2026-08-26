package dev.jaowzin.carromloader;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Process;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Direct same-device IPC between the Loader process and the virtual Carrom process.
 *
 * Uses an abstract Unix-domain socket instead of files, SharedPreferences or Android
 * broadcasts so the virtual runtime cannot redirect the status into the guest's
 * filesystem / component namespace.
 */
final class ModuleStatusIpc {
    private static final String TAG = "CarromStatusIpc";
    private static final String WAITING = "waiting for virtual Carrom";

    private static final AtomicReference<String> LATEST = new AtomicReference<>(WAITING);
    private static final AtomicReference<String> PENDING = new AtomicReference<>();
    private static final AtomicBoolean SERVER_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean SENDER_RUNNING = new AtomicBoolean(false);

    private static final ExecutorService SERVER_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "carrom-status-server");
        thread.setDaemon(true);
        return thread;
    });
    private static final ExecutorService SEND_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "carrom-status-sender");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile Context hostContext;
    private static volatile LocalServerSocket serverSocket;

    private ModuleStatusIpc() {}

    static void startHost(Context context) {
        if (context != null) hostContext = context.getApplicationContext();
        if (!SERVER_STARTED.compareAndSet(false, true)) return;

        SERVER_EXECUTOR.execute(() -> {
            try {
                // LocalServerSocket(String) binds in Linux' abstract socket namespace.
                serverSocket = new LocalServerSocket(socketName());
                Log.i(TAG, "listening on " + socketName());
                while (!Thread.currentThread().isInterrupted()) {
                    LocalSocket client = null;
                    try {
                        client = serverSocket.accept();
                        DataInputStream input = new DataInputStream(client.getInputStream());
                        String value = input.readUTF();
                        if (value != null && !value.trim().isEmpty()) {
                            LATEST.set(value);
                            Context target = hostContext;
                            if (target != null) ModuleStatusStore.write(target, value);
                            Log.i(TAG, "rx " + value.replace('\n', ' '));
                        }
                    } catch (Throwable error) {
                        if (serverSocket != null) Log.w(TAG, "receive failed", error);
                    } finally {
                        closeQuietly(client);
                    }
                }
            } catch (Throwable error) {
                SERVER_STARTED.set(false);
                Log.e(TAG, "server failed", error);
            }
        });
    }

    static void publish(String value) {
        if (value == null || value.trim().isEmpty()) return;
        PENDING.set(value);
        drainSender();
    }

    private static void drainSender() {
        if (!SENDER_RUNNING.compareAndSet(false, true)) return;
        SEND_EXECUTOR.execute(() -> {
            try {
                for (;;) {
                    String value = PENDING.getAndSet(null);
                    if (value == null) break;
                    sendOne(value);
                }
            } finally {
                SENDER_RUNNING.set(false);
                if (PENDING.get() != null) drainSender();
            }
        });
    }

    private static void sendOne(String value) {
        LocalSocket socket = new LocalSocket();
        try {
            socket.connect(new LocalSocketAddress(socketName(), LocalSocketAddress.Namespace.ABSTRACT));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF(value);
            output.flush();
        } catch (Throwable error) {
            // The Loader process may be restarting/background-killed. Status publishing is
            // best-effort and will retry automatically on the next native monitor tick.
            Log.d(TAG, "status socket unavailable: " + error.getClass().getSimpleName());
        } finally {
            closeQuietly(socket);
        }
    }

    static String latest(Context context) {
        String live = LATEST.get();
        if (live != null && !WAITING.equals(live) && !live.trim().isEmpty()) return live;

        String persisted = ModuleStatusStore.read(context);
        if (persisted != null && !persisted.trim().isEmpty()) return persisted;
        return WAITING;
    }

    static void markWaiting(Context context, String message) {
        String value = message == null || message.trim().isEmpty() ? WAITING : message;
        LATEST.set(value);
        if (context != null) ModuleStatusStore.write(context.getApplicationContext(), value);
    }

    private static String socketName() {
        return "carrom_loader_status_" + Process.myUid();
    }

    private static void closeQuietly(LocalSocket socket) {
        if (socket == null) return;
        try {
            socket.close();
        } catch (Throwable ignored) {
        }
    }
}
