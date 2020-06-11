package com.bihe0832.android.test;

import android.util.Log;

import com.bihe0832.android.lib.timer.BaseTask;
import com.bihe0832.android.lib.timer.TaskManager;


/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
public class TestTask extends BaseTask {


    @Override
    public String getTaskName() {
        return "TestTask";
    }

    @Override
    public int getMyInterval() {
        return 4;
    }

    @Override
    protected int getNextEarlyRunTime() {
        return 6;
    }

    @Override
    public void run() {
        Log.d("TestTask","TestTask");
        TaskManager.getInstance().letTaskRunEarly("TestTask");
    }
}
