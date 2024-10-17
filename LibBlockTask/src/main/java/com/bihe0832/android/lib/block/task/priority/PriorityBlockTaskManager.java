package com.bihe0832.android.lib.block.task.priority;

import com.bihe0832.android.lib.block.task.BlockTask;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.IdGenerator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.Synchronized;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/10/22.
 *         Description: 具有优先级性质的排序任务
 */
public class PriorityBlockTaskManager {

    //阻塞队列
    protected final PriorityBlockingQueue<BlockTask> mTaskQueue = new PriorityBlockingQueue<>();
    protected IdGenerator mIdGenerator = new IdGenerator(1);
    protected BlockTask currentTask = null;
    private boolean isRealRunning = false;

    private long delayTime = 0L;
    private boolean mUserControledRunning = false;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public PriorityBlockTaskManager() {
    }

    //开始遍历任务队列
    @Synchronized
    private void start() {
        if (!isRealRunning) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    isRealRunning = true;
                    try {
                        while (mUserControledRunning) {
                            //死循环
                            BlockTask iTask;
                            if (delayTime > 0) {
                                iTask = mTaskQueue.poll(delayTime, TimeUnit.MILLISECONDS);
                            } else {
                                iTask = mTaskQueue.take();
                            }
                            if (iTask != null) {
                                iTask.startTask();
                                currentTask = iTask;
                                iTask.blockTask();
                                currentTask = null;
                                iTask.finishTask();
                            } else {
                                taskQueueIsEmptyAfterDelay();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    isRealRunning = false;
                    ZLog.d(BlockTask.TAG, "Block Task Manager stop");
                }
            });
        }
    }

    protected void taskQueueIsEmptyAfterDelay() {
        ZLog.e(BlockTask.TAG, "Block Task Manager, task is empty after:" + delayTime);
    }

    public <T extends BlockTask> void remove(T task) {
        if (mTaskQueue.contains(task)) {
            ZLog.d(BlockTask.TAG, "\n" + "task has been finished. remove it from task queue");
            mTaskQueue.remove(task);
        }
    }

    @Synchronized
    public void add(BlockTask task) {
        long time = System.currentTimeMillis();
        ZLog.d(BlockTask.TAG, "Add task: " + time + " - " + task);
        //按照优先级插入队列 依次播放
        if (!mTaskQueue.contains(task)) {
            if (task.getSequence() < 1) {
                task.setSequence(mIdGenerator.generate());
            }
            mTaskQueue.add(task);
        }
        restart();
    }

    public void setDelayTime(long delayTime) {
        if (delayTime > 0) {
            this.delayTime = delayTime;
        }
    }

    public boolean isRunning() {
        return mUserControledRunning;
    }

    public void setRunning(boolean running) {
        mUserControledRunning = running;
    }

    public void clearAll() {
        mTaskQueue.clear();
        mUserControledRunning = false;
    }

    public void restart() {
        mUserControledRunning = true;
        start();
    }

    public BlockTask getCurrentTask() {
        return currentTask;
    }
}
