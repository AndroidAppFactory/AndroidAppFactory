package com.bihe0832.android.base.compose.debug.task;

import com.bihe0832.android.lib.block.task.BaseAAFBlockTask;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/10/22.
 * Description: Description
 */
public class LogTask extends BaseAAFBlockTask {

    public LogTask(String name) {
        super(name);
    }

    @Override
    public void doTask() {
        try {
            Thread.sleep(3000L);
            unLockBlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //任务执行完的回调，在这里你可以做些释放资源或者埋点之类的操作
    @Override
    public void finishTask() {
        super.finishTask();

    }
}
