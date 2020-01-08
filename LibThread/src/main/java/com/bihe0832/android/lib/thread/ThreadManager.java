package com.bihe0832.android.lib.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zixie on 16/6/2.
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
    // 低优先级线程，可以用于处理一些临时逻辑、延迟逻辑、数据上报等
    private final static String THREAD_NAME_LOWER = "THREAD_LOWER";
    private Handler mPostDelayTempHandler;

    // 临时线程池，用于通过Runnable处理其余临时逻辑，线程池内所有线程都为低优先级线程
    private ExecutorService executor;
    private static final int MAX_RUNNING_THREAD = 5;
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

    private ThreadManager(){
    }

    public Looper getLooper(int type) {
        if (type == LOOPER_TYPE_ANDROID_MAIN) {
            return Looper.getMainLooper();
        } else if (type == LOOPER_TYPE_LOWER) {
            if(null == mLowerHandlerThread){
                mLowerHandlerThread = new HandlerThread(THREAD_NAME_LOWER, Thread.MIN_PRIORITY);
                mLowerHandlerThread.start();
            }
            return mLowerHandlerThread.getLooper();
        } else if (type == LOOPER_TYPE_HIGHER) {
            if(null == mHigherHandlerThread){
                mHigherHandlerThread = new HandlerThread(THREAD_NAME_HIGHER, Thread.MAX_PRIORITY);
                mHigherHandlerThread.start();
            }
            return mHigherHandlerThread.getLooper();
        }  else {
            if(null == mNormalHandlerThread){
                mNormalHandlerThread = new HandlerThread(THREAD_NAME_NORMAL, Thread.NORM_PRIORITY);
                mNormalHandlerThread.start();
            }
            return mNormalHandlerThread.getLooper();
        }
    }

    public void start(Runnable runnable){
        if(null == executor){
            try {
                executor = Executors.newFixedThreadPool(MAX_RUNNING_THREAD, new CommonThreadFactory());
            } catch (Throwable t) {
                executor = Executors.newCachedThreadPool(new CommonThreadFactory());
            }
        }
        try {
            executor.submit(runnable);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void start(Runnable runnable, int seconds){
        if(null == mPostDelayTempHandler){
            mPostDelayTempHandler = new Handler(getLooper(LOOPER_TYPE_HIGHER));
        }
        mPostDelayTempHandler.postDelayed(runnable, seconds * 1000);
    }

    public void start(Runnable runnable, long delayMillis){
        if(null == mPostDelayTempHandler){
            mPostDelayTempHandler = new Handler(getLooper(LOOPER_TYPE_HIGHER));
        }
        mPostDelayTempHandler.postDelayed(runnable, delayMillis);
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

    public void runOnUIThread(Runnable runnable){
        try{
            new Handler(Looper.getMainLooper()).post(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
