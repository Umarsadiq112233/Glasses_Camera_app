package com.oudmon.ble.base.util;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author gs  Created by hzy on 20200807
 * 处理一些超时任务的线程池,是线程安全的
 */
public class ThreadUtils {
    private static final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final HashMap<TimeTask, ScheduledFuture> mRunnableCache = new HashMap<>();
    private static final Object mLock = new Object();

    /**
     * 发送一个延迟任务.如果队列中已存在则移除之前的任务
     *
     * @param runnable
     * @param delay
     */
    public static void postDelay(TimeTask runnable, long delay) {
        synchronized (mLock) {
            cancel(runnable);
            ScheduledFuture<?> future = mExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
            mRunnableCache.put(runnable, future);
        }
    }


    public static void cancel(TimeTask runnable) {
        cancel(runnable, false);
    }

    public static void cancel(TimeTask runnable, boolean mayInterruptIfRunning) {
        synchronized (mLock) {
            ScheduledFuture future = mRunnableCache.get(runnable);
            if (future != null) {
                future.cancel(mayInterruptIfRunning);
                mRunnableCache.remove(runnable);
            }
        }
    }

    /**
     *  继承此类做超时任务
     */
    public static abstract class TimeTask implements Runnable {

        @Override
        public void run() {
            task();
            mRunnableCache.remove(this);
        }

        protected abstract void task() ;
    }
}
