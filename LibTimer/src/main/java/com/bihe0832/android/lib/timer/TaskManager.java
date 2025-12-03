package com.bihe0832.android.lib.timer;


import android.text.TextUtils;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.Synchronized;

/**
 * 任务管理器 - 提供定时任务的调度和管理能力
 * <p>
 * 功能说明：
 * 1. 支持添加、删除、查询定时任务
 * 2. 使用单线程调度器每 500ms 检查一次任务是否到期
 * 3. 到期任务在独立线程中执行，避免相互阻塞
 * 4. 自动停止调度器当没有任务时，节省资源
 * <p>
 * 优化说明：
 * - 减少匿名内部类创建，降低 GC 压力
 * - 优化任务移除逻辑，使用 iterator.remove()
 * - 优化日志输出，减少字符串拼接开销
 * <p>
 * Created by zixie
 * Modified by AI Assistant on 2025/12/03 - 优化资源消耗
 */
public class TaskManager {

    //定时任务检查的时间粒度
    private static final String LOG_TAG = "TaskManager";
    protected static final int PERIOD = 500;
    private boolean started = false;
    private ConcurrentHashMap<String, BaseTask> mTaskList = new ConcurrentHashMap<String, BaseTask>();
    ScheduledExecutorService scheduledExecutorService = null;

    private static volatile TaskManager instance;

    public static TaskManager getInstance() {
        if (instance == null) {
            synchronized (TaskManager.class) {
                if (instance == null) {
                    instance = new TaskManager();
                }
            }
        }
        return instance;
    }

    private TaskManager() {
        super();
    }

    private void startTimer() {
        stopTimer();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        if (scheduledExecutorService != null) {
            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    runAll();
                }
            }, 1, PERIOD, TimeUnit.MILLISECONDS);
            started = true;
        }
    }

    private void stopTimer() {
        started = false;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    private void runAll() {
        try {
            if (mTaskList.isEmpty()) {
                ZLog.d(LOG_TAG, "TaskDispatcher stopTimer");
                stopTimer();
            } else {
                ZLog.d(LOG_TAG, "TaskDispatcher running, task count: " + mTaskList.size());
                Iterator<Entry<String, BaseTask>> iter = mTaskList.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, BaseTask> entry = iter.next();
                    final BaseTask task = entry.getValue();

                    // 优化：使用 iterator.remove() 而不是 map.remove()
                    if (task.isDeleted()) {
                        iter.remove();
                        ZLog.d(LOG_TAG, "Task removed: " + task.getTaskName());
                        continue;
                    }

                    // 检查任务是否到期
                    if (task.getNotifiedTimes() > task.getMyInterval() - 1) {
                        task.resetNotifiedTimes();
                        // 检查任务是否正在执行中
                        if (task.isRunning()) {
                            // 任务正在执行，跳过本次执行

                            ZLog.w(LOG_TAG, "Task skipped (still running): " + task.getTaskName());

                            task.skip();
                        } else {

                            ZLog.d(LOG_TAG, "Task executing: " + task.getTaskName());

                            // 优化：直接传递 task，减少匿名内部类创建
                            // 每个任务在独立线程中执行，避免相互阻塞
                            ThreadManager.getInstance().start(task);
                        }
                    }
                    task.increaseNotifiedTimes();
                }
            }
        } catch (Exception e) {
            ZLog.e(LOG_TAG, "runAll error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Synchronized
    public int addTask(BaseTask task) {
        if (TextUtils.isEmpty(task.getTaskName())) {
            ZLog.e(LOG_TAG, "add task failed: bad task name");
            return -3;
        }
        if (task.getMyInterval() < 1) {
            ZLog.e(LOG_TAG, "add task failed: bad interval for task " + task.getTaskName());
            return -1;
        }

        ZLog.d(LOG_TAG, "add task: " + task.getTaskName() + ", interval: " + task.getMyInterval());

        int result = -1;
        if (mTaskList.containsKey(task.getTaskName())) {
            if (mTaskList.get(task.getTaskName()).isDeleted()) {
                mTaskList.get(task.getTaskName()).setDeleted(false);
                return 1;
            } else {
                return -2;
            }
        } else {
            synchronized (mTaskList) {
                mTaskList.put(task.getTaskName(), task);
            }
            result = 0;
        }

        if (!started) {
            startTimer();
        }
        return result;
    }

    /**
     * 根据定时任务名称获取对应的定时任务
     *
     * @param taskName 任务名称
     * @return 返回对应的定时任务
     */
    public BaseTask getTaskByName(String taskName) {
        return mTaskList.get(taskName);
    }

    /**
     * 根据定时任务名称删除对应的任务
     *
     * @param taskName 任务名称
     */
    public void removeTask(String taskName) {
        ZLog.d(LOG_TAG, "remove task:" + taskName);
        if (!TextUtils.isEmpty(taskName)) {
            if (mTaskList.containsKey(taskName)) {
                mTaskList.get(taskName).setDeleted(true);
                mTaskList.remove(taskName);
            }
        }
    }

    public void letTaskRunEarly(String taskName) {
        ZLog.d(LOG_TAG, "letTaskRunEarly task:" + taskName);
        BaseTask task = getTaskByName(taskName);
        if (null != task) {
            task.letTaskRunEarly();
        }
    }
}
