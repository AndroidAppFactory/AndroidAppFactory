package com.bihe0832.android.common.compose.debug.common

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.common.compose.ui.activity.CommonActivityToolbarViewWithDrawer

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: 在原生页面加载Compose
 *
 */

abstract class DebugComposeMainWithDrawerActivity : DebugComposeMainActivity() {
    
    abstract fun getDrawerContentContentRender(): RenderState

    override fun getToolBarRender(contentRender: RenderState): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                handleEffect(LocalContext.current)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                CommonActivityToolbarViewWithDrawer(
                    icon = ImageVector.vectorResource(ResR.drawable.icon_menu),
                    drawerState = drawerState,
                    drawerContent = { getDrawerContentContentRender().Content() },
                    title = getTitleName(),
                    isCenter = isCenter(),
                    content = {
                        contentRender.Content()
                    })
            }
        }
    }

}


