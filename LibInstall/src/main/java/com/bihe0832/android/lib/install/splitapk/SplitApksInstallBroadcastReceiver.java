package com.bihe0832.android.lib.install.splitapk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;


import com.bihe0832.android.lib.log.ZLog;

import java.util.ArrayList;
import java.util.List;

public class SplitApksInstallBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SplitApksInstallBroadcastReceiver:::";
    private Context mContext;
    private List<EventObserver> mObservers = new ArrayList<>();

    public SplitApksInstallBroadcastReceiver(Context context) {
        this.mContext = context;
    }

    public String getIntentFilterFlag(){
        return this.mContext.getPackageName() + ".action.RootlessSaiPiBroadcastReceiver.ACTION_DELIVER_PI_EVENT";
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                ZLog.d(TAG + "Requesting user confirmation for installation");
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
