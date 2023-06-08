package com.bihe0832.android.base.debug.lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bihe0832.android.common.lock.screen.permission.LockScreenPermission;
import com.bihe0832.android.common.lock.screen.service.LockScreenService;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/5.
 * Description: Description
 */
public class DebugLockService extends LockScreenService {

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
        wakeUpAndUnlock(context);
        super.handleReceiverIntent(context, intent);
    }
}
