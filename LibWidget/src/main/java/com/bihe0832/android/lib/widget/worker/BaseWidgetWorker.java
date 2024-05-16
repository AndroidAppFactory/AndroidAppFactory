package com.bihe0832.android.lib.widget.worker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.widget.BaseWidgetProvider;
import com.bihe0832.android.lib.widget.WidgetUpdateManager;

public abstract class BaseWidgetWorker extends Worker {

    public BaseWidgetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //模拟耗时/网络请求操作
        try {
            ZLog.w(WidgetUpdateManager.TAG, "start doWork:" + getClass().getName());
            updateWidget(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.failure();
    }

    public PendingIntent getWidgetRefreshPendingIntent(Context context, Class<? extends BaseWidgetProvider> classT,
            boolean updateAll) {
        return getWidgetPendingIntent(context, BaseWidgetProvider.REFRESH_ACTION, classT, updateAll);
    }

    public PendingIntent getWidgetPendingIntent(Context context, String action,
            Class<? extends BaseWidgetProvider> classT, boolean updateAll) {
        Intent intent = new Intent();
        intent.setClass(context, classT);
        intent.setAction(action);
        intent.putExtra(BaseWidgetProvider.REFRESH_INTENT_KEY_UPDATE_ALL, updateAll);

        //设置pendingIntent
        PendingIntent pendingIntent;
        if (BuildUtils.INSTANCE.getSDK_INT() >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        //Retrieve a PendingIntent that will perform a broadcast
        return pendingIntent;
    }


    protected void updateWidget(Context context, ComponentName componentName, RemoteViews remoteViews) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                //获得appwidget管理实例，用于管理appwidget以便进行更新操作
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                //获得所有本程序创建的appwidget
                try {
                    int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);
                    appWidgetManager.updateAppWidget(widgetIds, remoteViews);
                } catch (Exception e) {
                    appWidgetManager.updateAppWidget(componentName, remoteViews);
                }
            }
        });
    }

    /**
     * 刷新widget
     */
    protected abstract void updateWidget(Context context);
}
