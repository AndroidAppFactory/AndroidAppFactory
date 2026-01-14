package com.bihe0832.android.app.about

import android.os.Bundle
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.lib.router.annotation.Module

/**
 * AAF 关于页面
 *
 * 展示应用信息、版本更新、反馈入口等内容
 * 通过路由 {@link RouterConstants#MODULE_NAME_BASE_ABOUT} 访问
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
@Module(RouterConstants.MODULE_NAME_BASE_ABOUT)
open class AboutActivity : com.bihe0832.android.common.about.AboutActivity() {

    /**
     * 获取关于页面 Fragment 的类
     *
     * @return AboutFragment 类
     */
    override fun getAboutItemClass(): Class<out AboutFragment> {
        return AboutFragment::class.java
    }

    /**
     * 创建关于页面 Fragment 实例
     *
     * @return AboutFragment 实例
     */
    override fun getItemFragment(): AboutFragment {
        return AboutFragment()
    }

    /**
     * 页面创建时检查更新
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UpdateManager.checkUpdateAndShowDialog(
            this,
            checkUpdateByUser = false,
            showIfNeedUpdate = true
        )
    }
}