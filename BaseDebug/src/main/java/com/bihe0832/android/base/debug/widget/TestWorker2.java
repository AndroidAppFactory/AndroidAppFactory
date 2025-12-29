package com.bihe0832.android.base.debug.widget;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import com.bihe0832.android.app.router.RouterConstants;
import com.bihe0832.android.app.router.RouterHelper;
import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.framework.debug.ShowDebugClick;
import com.bihe0832.android.lib.media.image.bitmap.BitmapTransUtils;
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil;
import com.bihe0832.android.lib.notification.NotifyManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.bihe0832.android.lib.utils.time.DateUtil;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;
import com.bihe0832.android.lib.worker.AAFWorkerManager;


public class TestWorker2 extends BaseWidgetWorker {

    public TestWorker2(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    protected void updateWidget(Context context) {
        AAFWorkerManager.INSTANCE.startForegroundService(context, DebugForegroundService.class.getName(), false);

        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String data = DateUtil.getCurrentDateEN() + "\n" + ShowDebugClick.getBasicDebugInfo(context);
                //只能通过远程对象来设置appwidget中的控件状态
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_2);

                PendingIntent pendingIntent = NotifyManager.INSTANCE.getPendingIntent(context,
                        RouterHelper.INSTANCE.getFinalURL(RouterConstants.MODULE_NAME_BASE_ABOUT));
                //为刷新按钮绑定一个事件便于发送广播
                remoteViews.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
                //通过远程对象修改textview
                remoteViews.setTextViewText(R.id.widget_text, data);
                remoteViews.setOnClickPendingIntent(R.id.widget_text, NotifyManager.INSTANCE.getPendingIntent(context,
                        RouterHelper.INSTANCE.getFinalURL(RouterConstants.MODULE_NAME_BASE_ABOUT)));

                Bitmap bitmap = BitmapUtil.getRemoteBitmap("https://cdn.bihe0832.com/images/head.jpg",
                        DisplayUtil.dip2px(context, 40f), DisplayUtil.dip2px(context, 40f));
                if (null == bitmap) {
                    try {
                        bitmap = BitmapFactory.decodeResource(context.getResources(), com.bihe0832.android.lib.aaf.res.R.mipmap.icon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                remoteViews.setImageViewBitmap(R.id.widget_icon,
                        BitmapTransUtils.getBitmapWithRound(bitmap, bitmap.getWidth() * 0.15f));
                //获得所有本程序创建的appwidget
                ComponentName componentName = new ComponentName(context, TestWidgetProvider2.class);
                //更新appwidget
                updateWidget(context, componentName, remoteViews);
            }
        });
    }
}
