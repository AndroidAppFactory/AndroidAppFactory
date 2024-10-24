package com.bihe0832.android.lib.install.splitapk;

import static androidx.core.app.ShareCompat.getCallingActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;


import android.content.pm.PackageManager;
import com.bihe0832.android.lib.log.ZLog;

import java.util.ArrayList;
import java.util.List;

public class SplitApksInstallBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SplitApksInstallBroadcastReceiver:::";
    private List<EventObserver> mObservers = new ArrayList<>();

    public String getIntentFilterFlag(Context context){
        return context.getPackageName() + ".action.RootlessSaiPiBroadcastReceiver.ACTION_DELIVER_PI_EVENT";
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // check if the originating Activity is from trusted package

        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                ZLog.d(TAG + "Requesting user confirmation for installation");
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // 检查Intent的目标组件是否在你的应用的控制范围内
                ComponentName componentName = confirmationIntent.getComponent();
                if (componentName != null) {
                    String packageName = componentName.getPackageName();
                    if (!context.getPackageName().equals(packageName)) {
                        ZLog.e(TAG + "Invalid target package: " + packageName);
                        return;
                    }
                }

                // 使用PackageManager检查Intent是否会被你的应用处理
                PackageManager packageManager = context.getPackageManager();
                if (confirmationIntent.resolveActivity(packageManager) == null) {
                    ZLog.e(TAG + "No activity found to handle the intent");
                    return;
                }

                try {
                    context.startActivity(confirmationIntent);
                } catch (Exception e) {
                    ZLog.e(TAG + "startActivity failed:" + e.getMessage());
                }
                dispatchOnConfirmationPending();
                break;
            case PackageInstaller.STATUS_SUCCESS:
                ZLog.d(TAG + "Installation succeed");
                dispatchOnInstallationSucceeded();
                break;
            default:
                ZLog.d(TAG + "Installation failed");
                dispatchOnInstallationFailed();
                break;
        }
    }

    private void dispatchOnConfirmationPending() {
        for (EventObserver observer : mObservers)
            observer.onConfirmationPending();
    }

    private void dispatchOnInstallationSucceeded() {
        for (EventObserver observer : mObservers)
            observer.onInstallationSucceeded();
    }

    private void dispatchOnInstallationFailed() {
        for (EventObserver observer : mObservers)
            observer.onInstallationFailed();
    }

    public void addEventObserver(EventObserver observer) {
        mObservers.add(observer);
    }

    public interface EventObserver {
        void onConfirmationPending();
        void onInstallationSucceeded();
        void onInstallationFailed();
    }
}
