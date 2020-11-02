package com.bihe0832.android.app;

import android.app.ActivityManager;
import android.content.Context;

import com.bihe0832.android.framework.ui.BaseApplication;
import com.bihe0832.android.lib.log.ZLog;

import java.util.List;

public class Application extends BaseApplication {

    static {
        try {
            // Load so
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZLog.d("Application", "Application onCreate start");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo it : runningApps) {
            if (it.pid == android.os.Process.myPid() && it.processName != null && it.processName.contains(getPackageName())) {
                ZLog.e("Application initCore process: name:" + it.processName + " and id:" + it.pid);
                final String processName = it.processName;
                if (processName.equalsIgnoreCase(getPackageName())) {
                    AppFactoryInit.INSTANCE.initCore(getApplicationContext());
                    AppFactoryInit.INSTANCE.initExtra(getApplicationContext(), processName);
                }
            }
        }
        ZLog.d("Application", "MnaApplication onCreate end");
    }

}
