package com.bihe0832.android.base.compose.debug.lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.bihe0832.android.lib.lock.screen.permission.LockScreenPermission;
import com.bihe0832.android.lib.lock.screen.service.LockScreenService;
import com.bihe0832.android.lib.log.ZLog;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/6/5.
 * Description: Description
 */
public class DebugLockService extends LockScreenService {

    @Override
    public void onCreate() {
        super.onCreate();
        ZLog.d(LockScreenService.TAG, "onCreate被调用，启动前台service");
    }

    public static void startLockServiceWithPermission(Context context) {
        LockScreenPermission.INSTANCE.startLockServiceWithPermission(context, DebugLockService.class);
    }

    public static void startLockService(Context context) {
        LockScreenPermission.INSTANCE.startLockService(context, DebugLockService.class);
    }

    @Override
    protected Class<? extends Activity> getLockScreenActivity() {
        return DebugLockActivity.class;
    }

    @Override
    protected void handleReceiverIntent(Context context, Intent intent) {
        disableSystemLockScreen(context);
        super.handleReceiverIntent(context, intent);
    }
}
