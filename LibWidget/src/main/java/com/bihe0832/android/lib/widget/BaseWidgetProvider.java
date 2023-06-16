package com.bihe0832.android.lib.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;

/**
 * 所有 AppWidgetProvider 的基类，包含了对添加、移除、更新、通用广播等逻辑
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {

    //系统更新广播
    public static final String REFRESH_ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String REFRESH_ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";

    //自定义的刷新广播
    public static final String REFRESH_ACTION = "android.appwidget.action.REFRESH";
    public static final String REFRESH_INTENT_KEY_UPDATE_ALL = "update_all";

    public abstract Class<? extends BaseWidgetWorker> getWidgetWorkerClass();

    protected abstract boolean canAutoUpdateByOthers();

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ZLog.d(WidgetUpdateManager.TAG, "onReceive start:" + getClass().getName());
        if (TextUtils.equals(intent.getAction(), REFRESH_ACTION)) {
            boolean updateAll = intent.getBooleanExtra(REFRESH_INTENT_KEY_UPDATE_ALL, true);
            //执行一次任务
            WidgetUpdateManager.INSTANCE.updateWidget(context, getWidgetWorkerClass(), canAutoUpdateByOthers(), updateAll);
        } else if (TextUtils.equals(intent.getAction(), REFRESH_ACTION_APPWIDGET_UPDATE) || TextUtils.equals(intent.getAction(), REFRESH_ACTION_SCREEN_ON)) {
            WidgetUpdateManager.INSTANCE.updateWidget(context, getWidgetWorkerClass(), canAutoUpdateByOthers(), false);
        }
        ZLog.d(WidgetUpdateManager.TAG, "onReceive end:" + getClass().getName());
    }

    /**
     * 每次窗口小部件更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ZLog.d(WidgetUpdateManager.TAG, "onUpdate:" + getClass().getName());
        ZLog.d(WidgetUpdateManager.TAG, "onUpdate worker:" + getWidgetWorkerClass().getName());
        WidgetUpdateManager.INSTANCE.updateWidget(context, getWidgetWorkerClass(), canAutoUpdateByOthers(), true);
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        ZLog.d(WidgetUpdateManager.TAG, "onDeleted:" + getClass().getName());
        ZLog.d(WidgetUpdateManager.TAG, "onDeleted worker:" + getWidgetWorkerClass().getName());
        WidgetUpdateManager.INSTANCE.updateAllWidgets(context);
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法，可添加多次但只第一次调用
     */
    @Override
    public void onEnabled(Context context) {
        //AppWidget的实例第一次被创建时调用
        super.onEnabled(context);
        ZLog.d(WidgetUpdateManager.TAG, "onEnabled:" + getClass().getName());
        ZLog.d(WidgetUpdateManager.TAG, "onEnabled worker:" + getWidgetWorkerClass().getName());
        WidgetUpdateManager.INSTANCE.enableWidget(context, getWidgetWorkerClass(), canAutoUpdateByOthers());
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法，注意是最后一个
     */
    @Override
    public void onDisabled(Context context) {
        //删除一个AppWidget时调用
        super.onDisabled(context);
        ZLog.d(WidgetUpdateManager.TAG, "onDisabled:" + getClass().getName());
        ZLog.d(WidgetUpdateManager.TAG, "onDisabled worker:" + getWidgetWorkerClass().getName());
        WidgetUpdateManager.INSTANCE.disableWidget(context, getWidgetWorkerClass());
    }
}
