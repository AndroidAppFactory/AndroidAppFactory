package com.bihe0832.android.lib.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程管理器 - 提供统一的线程调度和管理能力
 * 
 * 功能说明：
 * 1. 提供不同优先级的 HandlerThread（主线程、普通、高优先级、低优先级）
 * 2. 提供有界线程池用于执行临时任务，避免线程爆炸
 * 3. 线程池配置：核心线程5个，最大20个，队列容量100，空闲超时60秒
 * 
 * Created by zixie on 16/6/2.
 * Modified by AI Assistant on 2025/12/03 - 优化线程池为有界线程池
 */
public class ThreadManager {

    public static final int LOOPER_TYPE_ANDROID_MAIN = 0;
    public static final int LOOPER_TYPE_NORMAL = 1;
    public static final int LOOPER_TYPE_HIGHER = 2;
    public static final int LOOPER_TYPE_LOWER = 3;

    private HandlerThread mNormalHandlerThread = null;
    private HandlerThread mHigherHandlerThread = null;
    private HandlerThread mLowerHandlerThread = null;

    // 正常优先级线程，可用于处理数据存储，业务逻辑等，
    private final static String THREAD_NAME_NORMAL = "THREAD_NORMAL";
    // 高优先级线程，可以用于分发网络请求等（网络请求的处理建议利用临时线程池）
    private final static String THREAD_NAME_HIGHER = "THREAD_HIGHER";
    // 低优先级线程，可以用于处理一些临时逻辑、延迟逻辑、数据上报等，底层会转发到线程池
    private final static String THREAD_NAME_LOWER = "THREAD_LOWER";
    private Handler mPostDelayTempHandler;

    // 临时线程池，用于通过Runnable处理其余临时逻辑，线程池内所有线程都为低优先级线程
    private ExecutorService executor;
    private final static String EXECUTOR_NAME_TEMP = "TEMP_THREADS";

    private static volatile ThreadManager instance;

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    private ThreadManager() {
    }

    public Looper getLooper(int type) {
        if (type == LOOPER_TYPE_ANDROID_MAIN) {
            return Looper.getMainLooper();
        } else if (type == LOOPER_TYPE_LOWER) {
            if (null == mLowerHandlerThread) {
                mLowerHandlerThread = new HandlerThread(THREAD_NAME_LOWER, Thread.MIN_PRIORITY);
                mLowerHandlerThread.start();
            }
            return mLowerHandlerThread.getLooper();
        } else if (type == LOOPER_TYPE_HIGHER) {
            if (null == mHigherHandlerThread) {
                mHigherHandlerThread = new HandlerThread(THREAD_NAME_HIGHER, Thread.MAX_PRIORITY);
                mHigherHandlerThread.start();
            }
            return mHigherHandlerThread.getLooper();
        } else {
            if (null == mNormalHandlerThread) {
                mNormalHandlerThread = new HandlerThread(THREAD_NAME_NORMAL, Thread.NORM_PRIORITY);
                mNormalHandlerThread.start();
            }
            return mNormalHandlerThread.getLooper();
        }
    }

    public void start(Runnable runnable) {
        if (null == executor) {
            try {
                // 使用有界线程池：核心线程5个，最大20个，队列100个任务，空闲超时60秒
                // 这样可以避免线程爆炸，同时保证足够的并发能力
                executor = new ThreadPoolExecutor(
                    5,                              // 核心线程数
                    30,                             // 最大线程数
                    60L,                            // 空闲线程存活时间
                    TimeUnit.SECONDS,               // 时间单位
                    new LinkedBlockingQueue<Runnable>(100),  // 任务队列，容量100
                    new CommonThreadFactory()       // 线程工厂
                );
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        try {
            executor.submit(runnable);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void start(final Runnable runnable, long delayMillis) {
        if (null == mPostDelayTempHandler) {
            mPostDelayTempHandler = new Handler(getLooper(LOOPER_TYPE_HIGHER));
        }
        mPostDelayTempHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start(runnable);
            }
        }, delayMillis);
    }

    public void start(final Runnable runnable, int seconds) {
        start(runnable, seconds * 1000L);
    }

    private class CommonThreadFactory implements ThreadFactory {
        private final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public CommonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = EXECUTOR_NAME_TEMP + "-POOL-" + poolNumber.getAndIncrement();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.MIN_PRIORITY) {
                t.setPriority(Thread.MIN_PRIORITY);
            }
            return t;
        }
    }

    public void runOnUIThread(Runnable runnable) {
        try {
            new Handler(Looper.getMainLooper()).post(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
