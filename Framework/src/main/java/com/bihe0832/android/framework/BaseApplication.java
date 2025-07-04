package com.bihe0832.android.framework;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import com.bihe0832.android.lib.language.MultiLanguageHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bumptech.glide.Glide;
import java.util.List;

public abstract class BaseApplication extends Application {

    protected abstract boolean isDebug();

    protected abstract boolean isOfficial();

    protected abstract boolean skipPrivacy();

    protected abstract String versionTag();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Application", "base BaseApplication onCreate start");
        ZixieCoreInit.INSTANCE.initCore(this, isDebug(), isOfficial(), skipPrivacy(), versionTag(),
                supportMultiLanguage());
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo it : runningApps) {
            if (it.pid == android.os.Process.myPid() && it.processName != null && it.processName.contains(
                    getPackageName())) {
                ZLog.e("Application initCore process: name:" + it.processName + " and id:" + it.pid);
                if (it.processName.equalsIgnoreCase(getPackageName())) {
                    ZixieContext.INSTANCE.showDebugEditionToast();
                } else {
                    ZixieContext.INSTANCE.showDebug("独立进程初始化：" + it.processName);
                    ZLog.e("Application process: name:" + it.processName + " and id:" + it.pid);
                }
            }
        }
        Log.d("Application", "base BaseApplication onCreate end");
    }

    protected boolean supportMultiLanguage() {
        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.d("Application", "base BaseApplication attachBaseContext");
        if (supportMultiLanguage()) {
            super.attachBaseContext(MultiLanguageHelper.INSTANCE.modifyContextLanguageConfig(base));
        } else {
            super.attachBaseContext(base);
        }
        MultiDex.install(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (supportMultiLanguage()) {
            MultiLanguageHelper.INSTANCE.modifyContextLanguageConfig(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ZLog.e("Application", "Application onLowMemory");
        ZixieContext.INSTANCE.showDebug("onLowMemory");
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ZixieContext.INSTANCE.showDebug("onTrimMemory");
        ZLog.e("Application", "Application onTrimMemory");
        if (level > TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }
}
