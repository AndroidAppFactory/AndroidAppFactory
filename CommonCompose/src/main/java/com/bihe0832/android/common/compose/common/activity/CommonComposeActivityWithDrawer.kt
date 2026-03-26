package com.bihe0832.android.common.compose.common.activity

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonActivityToolbarView
import com.bihe0832.android.common.compose.ui.activity.CommonActivityToolbarViewWithDrawer
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 * 带侧边栏抽屉的通用 Compose Activity 基类
 *
 * 在 CommonComposeActivity 基础上增加了侧边栏导航支持，提供：
 * - 侧边栏抽屉内容（子类必须实现）
 * - 标题栏右侧操作区域（子类可选重写）
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 */
abstract class CommonComposeActivityWithDrawer : CommonComposeActivity() {

    /**
     * 获取侧边栏抽屉内容的 RenderState
     *
     * 子类必须实现此方法提供侧边栏内容
     *
     * @return RenderState 侧边栏内容渲染状态
     */
    abstract fun getDrawerContentContentRender(): RenderState?

    /**
     * 获取标题栏右侧操作区域的 RenderState
     *
     * 子类可重写此方法添加自定义操作按钮（如扫码、消息中心等）
     * 返回 null 表示不需要操作区域
     *
     * @return RenderState? 操作区域渲染状态，null 表示无操作按钮
     */
    open fun getTitleActionContentRender(): RenderState? {
        return null
    }

    override fun getToolBarRender(contentRender: RenderState): RenderState {
        val drawerContent = getDrawerContentContentRender()
        val actionContent = getTitleActionContentRender()

        // 不需要 Drawer 时，退化到父类的简单 Toolbar（action 不影响）
        if (drawerContent == null) {
            return object : RenderState {
                @Composable
                override fun Content() {
                    handleEffect(LocalContext.current)
                    CommonActivityToolbarView(
                        getNavigationIcon(),
                        getTitleName(),
                        isCenter(),
                        actions = { actionContent?.Content() },
                        content = { contentRender.Content() },
                    )
                }
            }
        }

        return object : RenderState {
            @Composable
            override fun Content() {
                handleEffect(LocalContext.current)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                CommonActivityToolbarViewWithDrawer(
                    icon = getNavigationIcon() ?: ImageVector.vectorResource(ResR.drawable.icon_menu),
                    drawerState = drawerState,
                    drawerContent = { drawerContent.Content() },
                    title = getTitleName(),
                    isCenter = isCenter(),
                    actions = {
                        actionContent?.Content()
                    },
                    content = {
                        contentRender.Content()
                    },
                )
            }
        }
    }
}
