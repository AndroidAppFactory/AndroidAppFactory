package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.app.ui.AAFDefaultTitleActions
import com.bihe0832.android.app.ui.navigation.getAAFNavigationDrawerContentRender
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.base.compose.debug.AAFDebugModuleView
import com.bihe0832.android.base.compose.debug.DebugTempView
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.common.DebugComposeMainActivity
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeView
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.utils.BadgeView
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.aaf.res.R
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager
import com.bihe0832.android.test.module.AAFDebugLogListActivity
import com.bihe0832.android.test.module.AAFRouterView
import com.bihe0832.android.test.widget.DebugWidget

@APPMain
@Module(RouterConstants.MODULE_NAME_DEBUG)
open class TestMainActivity : DebugComposeMainActivity() {

    val TAB_FOR_DEV_COMMON: String = "通用调试"
    val TAB_FOR_DEV_MODULE: String = "模块调试"
    val TAB_FOR_DEBUG: String = "临时测试"
    val TAB_FOR_DEV: String = "开发测试"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLogTips.initModule(this, true, Gravity.LEFT or Gravity.TOP)
        CommonDBManager.init(this)
        // 消息拍脸
        AAFMessageManager.observeAndShowFace(this)
        // 版本更新检查
        UpdateManager.checkUpdateAndShowDialog(this, false, ZixieContext.isOfficial())

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
        return null
    }

    override fun getDrawerContentContentRender(): RenderState? {
        return getAAFNavigationDrawerContentRender()
//        return null
    }

    override fun onResume() {
        super.onResume()
        DebugWidget.showAddDebugWidgetTips(this)
    }

    override fun getTabs(): List<String> {
        return mutableListOf(
            TAB_FOR_DEV_COMMON,
            TAB_FOR_DEV_MODULE,
            TAB_FOR_DEBUG,
            TAB_FOR_DEV
        )
    }

    override fun getDefault(): String {
        return TAB_FOR_DEV_COMMON
    }

    @Composable
    override fun GetPageView(page: Int, tab: String) {
        when (tab) {
            TAB_FOR_DEV_COMMON -> {
                val context = LocalContext.current
                DebugCommonComposeView {
                    DebugUtilsV2.startActivityWithException(
                        context,
                        AAFDebugLogListActivity::class.java
                    )
                }
            }

            TAB_FOR_DEV_MODULE -> {
                AAFDebugModuleView()
            }

            TAB_FOR_DEBUG -> {
                DebugContent {
                    DebugComposeItem("路由测试", "AAFRouterView") { AAFRouterView() }
                    DebugComposeItem("临时测试(Temp)", "DebugTempView") { DebugTempView() }
                }
            }

            TAB_FOR_DEV -> {
                AAFDebugOldModule()
            }

            else -> {
                AAFDebugModuleView()
            }
        }
    }


}
