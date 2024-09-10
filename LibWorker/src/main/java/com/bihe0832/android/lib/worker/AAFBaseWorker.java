package com.bihe0832.android.lib.worker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.bihe0832.android.lib.log.ZLog;

public abstract class AAFBaseWorker extends Worker {

    public AAFBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //模拟耗时/网络请求操作
        try {
            ZLog.w(AAFWorkerManager.TAG, "start doWork:" + getClass().getName());
            doAction(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }

    /**
     * 刷新widget
     */
    protected abstract void doAction(Context context);
}
