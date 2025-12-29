package com.bihe0832.android.lib.lock.screen.service;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bihe0832.android.lib.config.Config;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.notification.NotifyManager;
import com.bihe0832.android.lib.utils.intent.PendingIntentUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;

public abstract class LockScreenService extends Service {

    public static final String TAG = "LockScreenService";

    public static final int NOTICE_ID = 99999;
    public static final String NOTICE_CHANNEL_NAME = "正在运行中";
    public static final String NOTICE_CHANNEL_ID = "ForegroundService";

    protected abstract Class<? extends Activity> getLockScreenActivity();

    private ScreenBroadcastReceiver screenBroadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        ZLog.d(LockScreenService.TAG, "onCreate被调用，启动前台service");
        initForegroundNotify();
        initReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZLog.d(LockScreenService.TAG, "onDestroy，前台service被杀死");
        unregisterReceiver(screenBroadcastReceiver);
        NotifyManager.INSTANCE.cancelNotify(this, NOTICE_ID);
        // 重启自己
        Intent intent = new Intent(getApplicationContext(), getClass());
        startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ZLog.d(LockScreenService.TAG, "onStartCommand 被调用");
        init();
        // 如果Service被终止，当资源允许情况下，重启service
        return START_STICKY;
    }

    private void initReceiver() {
        if (screenBroadcastReceiver == null) {
            screenBroadcastReceiver = new ScreenBroadcastReceiver();
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenBroadcastReceiver, filter);
        }
    }

    private void initForegroundNotify() {
        String channelId = NOTICE_CHANNEL_ID;
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
            NotifyManager.INSTANCE.createNotificationChannel(this, NOTICE_CHANNEL_NAME, channelId);
        }
        //如果API大于18，需要弹出一个可见通知
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true).setSmallIcon(com.bihe0832.android.lib.aaf.res.R.mipmap.icon).setContentTitle(NOTICE_CHANNEL_NAME).build();
            startForeground(NOTICE_ID, notification);
        } else {
            startForeground(NOTICE_ID, new Notification());
        }

        Intent intent = new Intent(this, CancelNoticeService.class);
        startService(intent);
    }

    public class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceiverIntent(context, intent);
        }
    }

    protected void handleReceiverIntent(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            startLockScreeActivity(context);
        }
    }

    protected void startLockScreeActivity(Context context) {
        ZLog.d(LockScreenService.TAG, "startLockScreeActivity");
        Intent sendIntent = new Intent(context, getLockScreenActivity());
        sendIntent.setPackage(context.getPackageName());
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        try {
            PendingIntent pendingIntent = PendingIntentUtils.getBroadcastPendingIntent(context,0, sendIntent);
            context.startActivity(sendIntent);
            pendingIntent.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //亮屏
    public void wakeUpScreen(Context context) {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    //屏锁管理器，禁用系统锁屏
    public static void disableSystemLockScreen(Context context) {
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                KeyguardManager keyGuardService = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock keyGuardLock = keyGuardService.newKeyguardLock("");
                keyGuardLock.disableKeyguard();
            } catch (Exception e) {
                ZLog.e(LockScreenService.TAG, "disableSystemLockScreen exception, cause: " + e.getCause() + ", message: " + e.getMessage());
            }
        }
    }
}
