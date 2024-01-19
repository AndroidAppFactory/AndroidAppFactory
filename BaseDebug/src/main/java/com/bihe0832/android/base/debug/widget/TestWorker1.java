package com.bihe0832.android.base.debug.widget;

import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.time.DateUtil;
import com.bihe0832.android.lib.widget.WidgetUpdateManager;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;


public class TestWorker1 extends BaseWidgetWorker {

    public TestWorker1(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    protected void updateWidget(Context context) {
        ZLog.d(WidgetUpdateManager.TAG, "updateWidget TestWorker1");
        String data = DateUtil.getCurrentDateEN();
        //只能通过远程对象来设置appwidget中的控件状态
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_1);

        //为刷新按钮绑定一个事件便于发送广播
        remoteViews.setOnClickPendingIntent(R.id.iv_refresh, getWidgetRefreshPendingIntent(context, TestWidgetProvider1.class, true));
        //通过远程对象修改textview
        remoteViews.setTextViewText(R.id.widget_text, data);
//        remoteViews.setOnClickPendingIntent(R.id.widget_text, getWidgetRefreshPendingIntent(context, TestWidgetProvider1.class, false));

        ComponentName componentName = new ComponentName(context, TestWidgetProvider1.class);
        //更新appwidget
        updateWidget(context, componentName, remoteViews);

    }
}
