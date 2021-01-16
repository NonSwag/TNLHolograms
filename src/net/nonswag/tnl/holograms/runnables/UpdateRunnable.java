package net.nonswag.tnl.holograms.runnables;

import net.nonswag.tnl.holograms.Holograms;
import net.nonswag.tnl.holograms.api.Hologram;
import net.nonswag.tnl.listener.NMSMain;

import javax.annotation.Nonnull;

public class UpdateRunnable {

    @Nonnull private static final Thread thread;

    static {
        thread = new Thread(() -> {
            try {
                do {
                    for (Hologram hologram : Holograms.cachedValues()) {
                        hologram.updateAll();
                    }
                    Thread.sleep(Holograms.getUpdateTime());
                } while (true);
            } catch (Throwable t) {
                if (!(t instanceof InterruptedException)) {
                    NMSMain.stacktrace(t);
                }
            }
        });
        thread.setDaemon(true);
    }

    public static void start() {
        if (!isRunning()) {
            getThread().start();
        }
    }

    public static void stop() {
        if (isRunning()) {
            getThread().interrupt();
        }
    }

    public static void restart() {
        if (isRunning()) {
            stop();
            start();
        }
    }

    public static boolean isRunning() {
        return getThread() != null && getThread().isAlive() && !getThread().isInterrupted();
    }

    public static Thread getThread() {
        return thread;
    }
}
