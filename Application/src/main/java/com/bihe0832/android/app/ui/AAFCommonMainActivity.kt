package com.bihe0832.android.app.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.bihe0832.android.app.R
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.ui.navigation.getAAFNavigationDrawerContentRender
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivityWithDrawer
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext

/**
 * AAF 通用主页 Activity（Compose 版）
 *
 * 带侧边栏导航的主页基类，集成了：
 * - Compose 侧边栏导航
 * - 消息拍脸展示
 * - 版本更新检查
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
open class AAFCommonMainActivity : CommonComposeActivityWithDrawer() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 消息拍脸
        AAFMessageManager.observeAndShowFace(this)
        // 版本更新检查
        UpdateManager.checkUpdateAndShowDialog(this, false, ZixieContext.isOfficial())
    }

    /**
     * 获取侧边栏内容的 RenderState
     *
     * 子类可重写此方法提供自定义的侧边栏内容
     *
     * @return RenderState 侧边栏内容渲染状态
     */
    override fun getDrawerContentContentRender(): RenderState {
        return getAAFNavigationDrawerContentRender()
    }

    override fun getTitleActionContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                AAFDefaultTitleActions()
            }
        }
    }

    @Composable
    override fun getNavigationIcon(): ImageVector? {
        return ImageVector.vectorResource(R.drawable.icon_menu)
    }
}