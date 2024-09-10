package com.bihe0832.android.lib.widget.worker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.intent.PendingIntentUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.widget.BaseWidgetProvider;
import com.bihe0832.android.lib.widget.WidgetUpdateManager;
import com.bihe0832.android.lib.worker.AAFBaseWorker;

public abstract class BaseWidgetWorker extends AAFBaseWorker {

    public BaseWidgetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public PendingIntent getWidgetRefreshPendingIntent(Context context, Class<? extends BaseWidgetProvider> classT,
            boolean updateAll) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BaseWidgetProvider.REFRESH_INTENT_KEY_UPDATE_ALL, updateAll);
        return PendingIntentUtils.getBroadcastPendingIntent(context, BaseWidgetProvider.REFRESH_ACTION, classT, bundle);
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

    @Override
    protected void doAction(Context context) {
        updateWidget(getApplicationContext());
    }

    /**
     * 刷新widget
     */
    protected abstract void updateWidget(Context context);
}
