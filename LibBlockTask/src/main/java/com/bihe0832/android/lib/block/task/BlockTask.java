package com.bihe0832.android.lib.block.task;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/10/22.
 * Description: Description
 */
public interface BlockTask extends Comparable<BlockTask> {

    String TAG = "BlockTask";

    // 获取任务名称
    String getTaskName();

    //执行具体任务的方法
    void startTask();

    //任务执行完成后的回调方法
    void finishTask();

    //任务执行完成后的回调方法
    void skipTask();

    //设置任务优先级
    void setPriority(int mTaskPriority);

    //获取任务优先级
    int getPriority();

    //当优先级相同 按照插入顺序 先入先出 该方法用来标记插入顺序
    void setSequence(long mSequence);

    //获取入队次序
    long getSequence();

    //每个任务的状态，就是标记完成和未完成
    boolean getStatus();

    //阻塞任务执行，该方法用于任务执行时间不确定的情况
    void blockTask() throws Exception;

    //解除阻塞任务，该方法用于任务执行时间不确定的情况
    void unLockBlock();
}

