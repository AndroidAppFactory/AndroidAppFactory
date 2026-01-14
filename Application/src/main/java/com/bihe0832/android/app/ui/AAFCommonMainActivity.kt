package com.bihe0832.android.app.ui

import android.os.Bundle
import com.bihe0832.android.app.message.checkMsgAndShowFace
import com.bihe0832.android.common.navigation.drawer.R as NavigationDrawerR
import com.bihe0832.android.app.ui.navigation.AAFNavigationDrawerFragment
import com.bihe0832.android.app.ui.navigation.addRedDotAction
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.main.CommonActivityWithNavigationDrawer
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment
import com.bihe0832.android.framework.ZixieContext
import java.util.Locale

/**
 * AAF 通用主页 Activity
 *
 * 带侧边栏导航的主页基类，集成了：
 * - 侧边栏导航
 * - 消息拍脸展示
 * - 版本更新检查
 * - 二维码扫描入口
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
open class AAFCommonMainActivity : CommonActivityWithNavigationDrawer() {

    /** 侧边栏导航 Fragment */
    private var mAAFNavigationDrawerFragment = createAAFNavigationDrawerFragment()

    /**
     * 创建侧边栏导航 Fragment
     *
     * 子类可重写此方法提供自定义的导航 Fragment
     *
     * @return AAFNavigationDrawerFragment 实例
     */
    open fun createAAFNavigationDrawerFragment(): AAFNavigationDrawerFragment {
        return AAFNavigationDrawerFragment()
    }

    /**
     * 获取侧边栏导航 Fragment
     *
     * @return 当前的导航 Fragment
     */
    override fun getNavigationDrawerFragment(): NavigationDrawerFragment? {
        return mAAFNavigationDrawerFragment
    }

    /**
     * 页面创建时初始化
     *
     * 设置红点、消息拍脸、版本更新检查和扫码入口
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addRedDotAction(findViewById(NavigationDrawerR.id.title_icon_unread))
        checkMsgAndShowFace()
        UpdateManager.checkUpdateAndShowDialog(this, false, ZixieContext.isOfficial())
        updateTitle(titleName)
        showQrcodeScan(needSound = true, needVibrate = true, onlyQRCode = false)
    }

    /**
     * 语言切换回调
     *
     * 重新创建导航 Fragment 以应用新语言
     *
     * @param lastLocale 切换前的语言
     * @param toLanguageTag 切换后的语言
     */
    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        mAAFNavigationDrawerFragment = createAAFNavigationDrawerFragment()
        super.onLocaleChanged(lastLocale, toLanguageTag)
    }


}
