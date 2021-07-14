package com.bihe0832.android.app;

import android.app.ActivityManager;
import android.content.Context;
import com.bihe0832.android.framework.router.RouterInterrupt;
import com.bihe0832.android.framework.ui.BaseApplication;
import com.bihe0832.android.lib.log.ZLog;
import java.util.List;

public class Application extends BaseApplication {

    /**
     * 是否为调试版本，请勿手动修改，自动构建会自动修改。如有问题，请联系hardy
     * 当IS_TEST_VERSION为true时，表示当前是开发版本
     */
    private static final boolean IS_TEST_VERSION = true;

    /**
     * 是否为正式发布版本，请勿手动修改，自动构建会自动修改。如有问题，请联系hardy
     * 当 IS_OFFICIAL_VERSION == true && IS_TEST_VERSION == false 时 不提示 [ showOfficial ] 其余都提示
     */
    private static final boolean IS_OFFICIAL_VERSION = false;

    //版本对应TAG，请勿手动修改，自动构建会自动修改。如有问题，请联系hardy
    private static final String VERSION_TAG = "Tag_ZIXIE_1.0.0_1";

    static {
        try {
            // Load so
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }


    @Override
    protected boolean isDebug() {
        return IS_TEST_VERSION;
    }

    @Override
    protected boolean isOfficial() {
        return IS_OFFICIAL_VERSION;
    }

    @Override
    protected String versionTag() {
        return VERSION_TAG;
    }

    @Override
    protected boolean skipPrivacy() {
        return !isOfficial();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZLog.d("Application", "Application onCreate start");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo it : runningApps) {
            if (it.pid == android.os.Process.myPid() && it.processName != null && it.processName
                    .contains(getPackageName())) {
                ZLog.e("Application initCore process: name:" + it.processName + " and id:" + it.pid);
                final String processName = it.processName;
                if (processName.equalsIgnoreCase(getPackageName())) {
                    AppFactoryInit.INSTANCE.initCore(getApplicationContext());
                    if(RouterInterrupt.INSTANCE.hasAgreedPrivacy()){
                        AppFactoryInit.INSTANCE.initExtra(getApplicationContext());
                    }
                }
            }
        }
        ZLog.d("Application", "MnaApplication onCreate end");
    }

}
