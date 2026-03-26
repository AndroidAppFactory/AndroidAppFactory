package com.bihe0832.android.app.about

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.about.compose.AboutComposeActivity
import com.bihe0832.android.common.about.compose.wrapper.SettingsItemFactory
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.lib.router.annotation.Module

/**
 * AAF 关于页面（Compose 版本）
 *
 * 继承 AboutComposeActivity，使用 Compose 实现关于页面。
 * 通过路由 {@link RouterConstants#MODULE_NAME_BASE_ABOUT} 访问
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
@Module(RouterConstants.MODULE_NAME_BASE_ABOUT)
open class AboutActivity : AboutComposeActivity() {

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

    /**
     * 提供设置项列表内容
     *
     * 展示版本更新、版本列表、反馈入口等设置项
     */
    @Composable
    override fun getSettingsContent(context: Context, updateData: UpdateDataFromCloud?) {
        // 检查更新
        SettingsItemFactory.Update(cloud = updateData) { ctx ->
            (ctx as? android.app.Activity)?.let {
                UpdateManager.checkUpdateAndShowDialog(
                    it,
                    checkUpdateByUser = true,
                    showIfNeedUpdate = true,
                )
            }
        }

        // 版本列表
        SettingsItemFactory.VersionList()

        // 意见反馈
        SettingsItemFactory.FeedbackURL()
    }

    override fun onDebugAction() {
        ZixieContext.showWaiting()
    }
}