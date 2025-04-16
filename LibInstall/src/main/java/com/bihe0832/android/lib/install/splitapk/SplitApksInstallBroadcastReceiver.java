package com.bihe0832.android.lib.install.splitapk;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;

public class SplitApksInstallBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SplitApksInstallBroadcastReceiver:::";
    private EventObserver mObservers = null;

    public String getIntentFilterFlag(Context context) {
        return context.getPackageName() + ".action.ROOTLESS_SAIP_BROADCAST_RECEIVER.ACTION_DELIVER_PI_EVENT";
    }

    // 方法1: 检查调用者是否持有自定义权限
    private boolean checkCallerPermission(Context context) {
        return context.checkCallingOrSelfPermission(
                context.getPackageName() + ".install.permission.SEND_SPLIT_APKS_INSTALL")
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        String pacakgeName = "";
        String sessionID = "";
        try {
            sessionID = String.valueOf(intent.getExtras().getInt(PackageInstaller.EXTRA_SESSION_ID, 0));
            pacakgeName = intent.getExtras().getString(PackageInstaller.EXTRA_PACKAGE_NAME, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(sessionID)) {
            mObservers.onInstallationFailed(sessionID, pacakgeName);
            return;
        }
        // 验证调用者权限
        if (!checkCallerPermission(context)) {
            ZLog.e(TAG, "Permission verification failed");
            mObservers.onInstallationFailed(sessionID, pacakgeName);
            return;
        }

        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                ZLog.d(TAG, "Requesting user confirmation for installation");
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);

                // 检查Intent的目标组件是否在你的应用的控制范围内
                ComponentName componentName = confirmationIntent.getComponent();
                if (componentName != null) {
                    String packageName = componentName.getPackageName();
                    if (!context.getPackageName().equals(packageName)) {
                        ZLog.e(TAG, "Invalid target package: " + packageName);
                        mObservers.onInstallationFailed(sessionID, pacakgeName);
                        return;
                    }
                }

                // 或者验证调用者包名是否在白名单中
                if (!context.getPackageName().equals(confirmationIntent.getPackage())
                        && !"com.android.packageinstaller".equals(confirmationIntent.getPackage())) {
                    ZLog.e(TAG, "Invalid target package: " + confirmationIntent.getPackage());
                    mObservers.onInstallationFailed(sessionID, pacakgeName);
                    return;
                }

                // 清除任何可能被滥用的flags和extras
                confirmationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // 使用PackageManager检查Intent是否会被你的应用处理
                PackageManager packageManager = context.getPackageManager();
                if (confirmationIntent.resolveActivity(packageManager) == null) {
                    ZLog.e(TAG, "No activity found to handle the intent");
                    return;
                }

                try {
                    context.startActivity(confirmationIntent);
                } catch (Exception e) {
                    ZLog.e(TAG, "startActivity failed:" + e.getMessage());
                }
                mObservers.onConfirmationPending(sessionID, pacakgeName);
                break;
            case PackageInstaller.STATUS_SUCCESS:
                ZLog.d(TAG, "Installation succeed");
                mObservers.onInstallationSucceeded(sessionID, pacakgeName);
                break;
            default:
                ZLog.d(TAG, "Installation failed");
                mObservers.onInstallationFailed(sessionID, pacakgeName);
                break;
        }
    }

    public void setEventObserver(EventObserver observer) {
        mObservers = observer;
    }

    public interface EventObserver {

        void onConfirmationPending(String sessionId, String packageName);

        void onInstallationSucceeded(String sessionId, String packageName);

        void onInstallationFailed(String sessionId, String packageName);
    }
}
