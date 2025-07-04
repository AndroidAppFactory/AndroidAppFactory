package com.bihe0832.android.app;

import com.bihe0832.android.framework.BaseApplication;
import com.bihe0832.android.lib.log.ZLog;

public class Application extends BaseApplication {

    /**
     * 是否为调试版本，请勿手动修改，自动构建会自动修改。如有问题，请联系zixie
     * 当IS_TEST_VERSION为true时，表示当前是开发版本
     */
    private static final boolean IS_TEST_VERSION = true;

    /**
     * 是否为正式发布版本，请勿手动修改，自动构建会自动修改。如有问题，请联系zixie
     * 当 IS_OFFICIAL_VERSION == true && IS_TEST_VERSION == false 时 不提示 [ showOfficial ] 其余都提示
     */
    private static final boolean IS_OFFICIAL_VERSION = false;

    //版本对应TAG，请勿手动修改，自动构建会自动修改。如有问题，请联系zixie
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
        ZLog.d("Application", "Application onCreate start");
        super.onCreate();
        AppFactoryInit.INSTANCE.initAll(this);
        ZLog.d("Application", "Application onCreate end");
    }
}
