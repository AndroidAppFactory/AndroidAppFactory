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

    public PendingIntent getActivityPendingIntent(Context context, Class<? extends Activity> action,
            Bundle bundle, int requestCode) {
        final Intent activityIntent = new Intent(context, action);
        Bundle finalBundle = new Bundle();
        if (null != bundle) {
            finalBundle.putAll(bundle);
        }
        activityIntent.putExtras(finalBundle);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, requestCode, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PendingIntent getUriPendingIntent(Context context, String uri, int requestCode, boolean needNewTask) {
        final Intent activityIntent = new Intent();
        activityIntent.setData(Uri.parse(uri));
        if (needNewTask) {
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return PendingIntent.getActivity(context, requestCode, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 刷新widget
     */
    protected abstract void doAction(Context context);
}
