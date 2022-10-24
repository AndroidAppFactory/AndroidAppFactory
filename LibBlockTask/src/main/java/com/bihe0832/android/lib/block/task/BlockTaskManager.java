package com.bihe0832.android.lib.block.task;

import android.os.AsyncTask;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.IdGenerator;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/22.
 * Description: Description
 */
public class BlockTaskManager {
    public static final String TAG = "BlockTaskManager";

    private IdGenerator mIdGenerator = new IdGenerator(0);
    //阻塞队列
    private final PriorityBlockingQueue<BlockTask> mTaskQueue = new PriorityBlockingQueue<>();
    private boolean isRunning = false;

    public BlockTaskManager() {
        start();
    }

    //开始遍历任务队列
    private void start() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRunning) {
                        ZLog.d(TAG, "Do task by loop");
                        //死循环
                        BlockTask iTask = mTaskQueue.take();
                        if (iTask != null) {
                            iTask.startTask();
                            iTask.blockTask();
                            iTask.finishTask();
                        }

//                        if(mTaskQueue.size() == 0){
//                            isRunning = false;
//                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public <T extends BlockTask> void remove(T task) {
        if (mTaskQueue.contains(task)) {
            ZLog.d(TAG, "\n" + "task has been finished. remove it from task queue");
            mTaskQueue.remove(task);
        }
    }

    public void add(BlockTask task) {
        long time = System.currentTimeMillis();
        ZLog.d(TAG, "Add    task: " + time + " - " + task);
        if (!isRunning) {
            isRunning = true;
            start();
        }
        //按照优先级插入队列 依次播放
        if (!mTaskQueue.contains(task)) {
            task.setSequence(mIdGenerator.generate());
            mTaskQueue.add(task);
        }
    }
}
