package com.bihe0832.android.lib.timer;

/**
 * 定时任务基类 - 所有定时任务都要继承自此类
 * <p>
 * 功能说明：
 * 1. getNotifiedTimes 决定每个任务的间隔次数（总时间 = 间隔次数 × 定时器最小时间粒度 500ms）
 * 2. 实现 Runnable 接口，可以直接传递给线程池执行，减少匿名内部类创建
 * 3. 支持任务的添加、删除、提前执行等操作
 * 4. 防止任务重复执行：如果任务正在执行中，新的执行请求会被跳过
 * <p>
 * 线程安全说明：
 * - 使用 volatile 保证 isRunning 的可见性
 * - 使用 try-finally 确保 isRunning 状态正确释放
 * <p>
 * Created by zixie
 * Modified by AI Assistant on 2025/12/03 - 添加任务执行状态检查，防止重复执行
 */
public abstract class BaseTask implements Runnable {

    //任务计数器记录的轮训次数
    protected int notifiedTimes = runAfterAdd() ? getMyInterval() : 0;

    private boolean isDeleted = false;

    // 任务是否正在执行中（使用 volatile 保证多线程可见性）
    private volatile boolean isRunning = false;

    // 返回各自需要的执行间隔, 如果此函数返回n, 则此任务每n*500毫秒会被运行
    public abstract int getMyInterval();

    //强制修改task的调用周期，使下一次调用提前 n*500 毫秒，如果提前超过间隔，将在下一个500毫秒立即执行
    protected abstract int getNextEarlyRunTime();

    /**
     * Runnable 接口实现 - 由线程池调用
     * 此方法不应被子类重写，子类应实现 doTask() 方法
     */
    @Override
    public final void run() {
        // 检查任务是否正在执行中
        if (isRunning) {
            // 任务正在执行，跳过本次执行，避免重复执行
            return;
        }

        isRunning = true;
        try {
            // 执行具体任务
            doTask();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 确保状态被正确释放
            isRunning = false;
        }
    }

    protected void skip() {
        letTaskRunEarly();
    }

    /**
     * 定时任务到期需要执行的操作
     * 子类必须实现此方法来定义具体的任务逻辑
     */
    protected abstract void doTask();

    //定时任务的名称
    public abstract String getTaskName();

    /**
     * 定时器定时调用所有任务的计数器, 当任务计数器记录的轮训次数与 {@link BaseTask#getMyInterval()} 一致时，任务运行
     */
    protected final int getNotifiedTimes() {
        return notifiedTimes;
    }

    /**
     * 定时器定时调用所有任务的计数器, 每次被调用要增加一次notifiedTimes
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

    protected void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    protected boolean isDeleted() {
        return isDeleted;
    }

    /**
     * 检查任务是否正在执行中
     *
     * @return true 表示任务正在执行，false 表示任务空闲
     */
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String toString() {
        return "BaseTask{" +
                "name=" + getTaskName() +
                ", notifiedTimes=" + notifiedTimes +
                ", isDeleted=" + isDeleted +
                ", isRunning=" + isRunning +
                ", myInterval=" + getMyInterval() +
                ", runAfterAdd=" + runAfterAdd() +
                '}';
    }
}
