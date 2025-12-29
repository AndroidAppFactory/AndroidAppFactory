package com.bihe0832.android.lib.lock.screen.service;


import static android.app.Notification.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;


import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.os.BuildUtils;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/6/7.
 * Description: 移除前台Service通知栏标志，这个Service选择性使用
 */

public class CancelNoticeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //如果API大于18，需要弹出一个可见通知
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LockScreenService.NOTICE_CHANNEL_ID);
            Notification notification = builder.setOngoing(true).setSmallIcon(com.bihe0832.android.lib.aaf.res.R.mipmap.icon).setPriority(PRIORITY_MIN).setCategory(Notification.CATEGORY_SERVICE).build();
            startForeground(LockScreenService.NOTICE_ID, notification);
        } else {
            startForeground(LockScreenService.NOTICE_ID, new Notification());
        }

        ThreadManager.getInstance().start(() -> {
            ThreadManager.getInstance().runOnUIThread(() -> {
                ZLog.d(LockScreenService.TAG, "onStartCommand cancle");
                // 移除DaemonService弹出的通知
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(LockScreenService.NOTICE_ID);
                try {
                    if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
                        mNotificationManager.deleteNotificationChannel(LockScreenService.NOTICE_CHANNEL_ID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 取消CancelNoticeService的前台
                stopForeground(true);
                // 任务完成，终止自己
                stopSelf();
            });
        }, 1);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}