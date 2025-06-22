package com.android.systemui.animation;

import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.WindowManager;
import android.app.ActivityManager;
import android.window.SurfaceSyncGroup;
import android.window.OnBackInvokedDispatcher.Priority;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;

public class JavaClassToKotlin {
    public static final int TRANSIT_CLOSE = WindowManager.TRANSIT_CLOSE;
    public static final int TRANSIT_OPEN = WindowManager.TRANSIT_OPEN;
    public static final int TRANSIT_TO_BACK = WindowManager.TRANSIT_TO_BACK;
    public static final int TRANSIT_TO_FRONT = WindowManager.TRANSIT_TO_FRONT;
    public static final int START_TASK_TO_FRONT = ActivityManager.START_TASK_TO_FRONT;
    public static final int START_SUCCESS = ActivityManager.START_SUCCESS;
    public static final int START_DELIVERED_TO_TOP = ActivityManager.START_DELIVERED_TO_TOP;
    public static final int TYPE_NAVIGATION_BAR = WindowManager.LayoutParams.TYPE_NAVIGATION_BAR;
    public static final long getInstanceFrameTime = Choreographer.getInstance.frameTime();
    public static final long lastFrameTimeNanos = Choreographer.getInstance.lastFrameTimeNanos();
    public static final long NANOS_PER_MS = TimeUtils.NANOS_PER_MS;
    public void addSyncCompleteCallback(Executor executor, Runnable runnable) {
        SurfaceSyncGroup.addSyncCompleteCallback(executor, runnable);
    }
    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {
    }

    public static class WindowManagerProxy {
        
        public WindowManagerProxy(WindowManager WindowManager) {;
        }
    }
}

