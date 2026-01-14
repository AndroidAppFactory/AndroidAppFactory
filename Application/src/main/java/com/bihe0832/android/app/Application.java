package com.bihe0832.android.app;

import com.bihe0832.android.framework.BaseApplication;
import com.bihe0832.android.lib.log.ZLog;

/**
 * AAF 框架应用入口类
 *
 * 继承自 BaseApplication，提供应用的基础配置和初始化入口。
 * 包含版本控制标识、调试模式开关等关键配置项。
 *
 * 注意事项：
 * - IS_TEST_VERSION 和 IS_OFFICIAL_VERSION 由自动构建系统修改，请勿手动修改
 * - VERSION_TAG 用于版本追踪，由自动构建系统维护
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
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


    /**
     * 判断是否为调试版本
     *
     * @return 如果是测试版本返回 true，否则返回 false
     */
    @Override
    protected boolean isDebug() {
        return IS_TEST_VERSION;
    }

    /**
     * 判断是否为正式发布版本
     *
     * @return 如果是正式版本返回 true，否则返回 false
     */
    @Override
    protected boolean isOfficial() {
        return IS_OFFICIAL_VERSION;
    }

    /**
     * 获取版本标签
     *
     * @return 当前版本的标签字符串
     */
    @Override
    protected String versionTag() {
        return VERSION_TAG;
    }

    /**
     * 判断是否跳过隐私协议检查
     *
     * 非正式版本默认跳过隐私协议检查，便于开发调试
     *
     * @return 如果跳过隐私检查返回 true，否则返回 false
     */
    @Override
    protected boolean skipPrivacy() {
        return !isOfficial();
    }

    /**
     * 应用创建时的初始化入口
     *
     * 执行 AAF 框架的完整初始化流程，包括核心模块和扩展模块的初始化
     */
    @Override
    public void onCreate() {
        ZLog.d("Application", "Application onCreate start");
        super.onCreate();
        AppFactoryInit.INSTANCE.initAll(this);
        ZLog.d("Application", "Application onCreate end");
    }
}
