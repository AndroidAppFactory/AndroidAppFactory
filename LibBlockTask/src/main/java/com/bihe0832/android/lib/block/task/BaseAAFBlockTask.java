package com.bihe0832.android.lib.block.task;

import com.bihe0832.android.lib.log.ZLog;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/22.
 * Description: Description
 */
public abstract class BaseAAFBlockTask implements BlockTask {

    private int mTaskPriority = 0; //默认优先级
    private long mSequence;// 入队次序
    private String mTaskName = "";// 入队次序
    private Boolean mTaskIsRunning = false;
    private final PriorityBlockingQueue<Integer> blockQueue;

    public abstract void doTask();

    //构造函数
    public BaseAAFBlockTask(String name) {
        mTaskName = name;
        blockQueue = new PriorityBlockingQueue<>();
    }

    @Override
    public final void startTask() {
        ZLog.w(BlockTask.TAG, "Do     Task: " + this);
        mTaskIsRunning = true;
        doTask();
    }

    //任务执行完成，改变标记位，将任务在队列中移除，并且把记录清除
    @Override
    public void finishTask() {
        this.mTaskIsRunning = false;
    }

    //设置任务优先级实现
    @Override
    public void setPriority(int mTaskPriority) {
        this.mTaskPriority = mTaskPriority;
    }

    //获取任务优先级
    @Override
    public int getPriority() {
        return mTaskPriority;
    }
    //获取任务执行时间

    //设置任务次序
    @Override
    public void setSequence(long mSequence) {
        this.mSequence = mSequence;
    }

    //获取任务次序
    @Override
    public long getSequence() {
        return mSequence;
    }

    // 获取任务状态
    @Override
    public boolean getStatus() {
        return mTaskIsRunning;
    }

    //阻塞任务执行
    @Override
    public void blockTask() throws Exception {
        blockQueue.take();
    }

    public String getTaskName() {
        return mTaskName;
    }

    @Override
    public void skipTask() {
        ZLog.w(BlockTask.TAG, "Skip Task: " + this);
    }

    //解除阻塞
    @Override
    final public void unLockBlock() {
        blockQueue.add(1);
    }

    public Boolean isFirstInFirstOut() {
        return true;
    }

    @Override
    public int compareTo(BlockTask another) {
        final int me = this.getPriority();
        final int it = another.getPriority();
        long result;
        if (isFirstInFirstOut()) {
            result = (me == it ? getSequence() - another.getSequence() : it - me);
        } else {
            result = (me == it ? another.getSequence() - getSequence() : it - me);
        }

        return (int) result;
    }

    @Override
    public String toString() {
        return "AAFBlockTask-> TaskPriority : " + mTaskPriority + ",sequence : " + mSequence + ",name : " + mTaskName;
    }
}
