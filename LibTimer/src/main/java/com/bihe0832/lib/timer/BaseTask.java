package com.bihe0832.lib.timer;

/**
 * 所有定时任务都要继承自此类 getNotifiedTimes决定每个任务的间隔次数(总时间由间隔次数*定时器最小时间粒度)
 */
public abstract class BaseTask {

    protected int notifiedTimes = runAfterAdd() ? getMyInterval() : 0;

    // 返回各自需要的执行间隔, 如果此函数返回n, 则此任务每n*500毫秒会被运行
    public abstract int getMyInterval();

    //强制修改task的调用周期，使下一次调用提前 n*500 毫秒，如果提前超过间隔，将在下一个500毫秒立即执行
    protected abstract int getNextEarlyRunTime();

    // 定时任务到期需要执行的操作
    public abstract void run();

    //定时任务的名称
    public abstract String getTaskName();

    protected final int getNotifiedTimes() {
        return notifiedTimes;
    }

    /**
     * 每次定时器到时间以后会尝试调用一下所有任务, 任务自身记录是否需要运行, 每次被调用要增加一次notifiedTimes
     */
    protected final void increaseNotifiedTimes() {
        notifiedTimes++;
    }

    /**
     * 每次任务实际执行过后需要将被通知的次数置0
     */
    protected final void resetNotifiedTimes() {
        notifiedTimes = 0;
    }


    /**
     * 任务在添加以后是否立即运行
     */
    protected boolean runAfterAdd() {
        return true;
    }

    protected void letTaskRunEarly() {
        this.notifiedTimes = this.notifiedTimes + getNextEarlyRunTime();
    }
}
