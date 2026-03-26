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
import com.bihe0832.android.base.compose.debug.AAFDebugModuleView
import com.bihe0832.android.base.compose.debug.DebugTempView
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.common.DebugComposeMainActivityWithDrawer
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeView
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.utils.BadgeView
import com.bihe0832.android.common.qrcode.QrcodeUtils
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
open class TestMainActivityWithDrawer : DebugComposeMainActivityWithDrawer() {

    val TAB_FOR_DEV_COMMON: String = "通用调试"
    val TAB_FOR_DEV_MODULE: String = "模块调试"
    val TAB_FOR_DEBUG: String = "临时测试"
    val TAB_FOR_DEV: String = "开发测试"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLogTips.initModule(this, true, Gravity.LEFT or Gravity.TOP)
        CommonDBManager.init(this)
        // 消息拍脸
        AAFMessageManager.getMessageLiveData().observe(this) { noticeList ->
            noticeList?.distinctBy { it.messageID }
                ?.filter {
                    !AAFMessageManager.mAutoShowMessageList.contains(it.messageID)
                            && AAFMessageManager.canShowFace(it, false)
                }?.forEach {
                    AAFMessageManager.mAutoShowMessageList.add(it.messageID)
                    AAFMessageManager.showMessage(this, it, true)
                }
        }
    }

    override fun getTitleActionContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // 观察消息未读数 LiveData（通过 DisposableEffect + observe 避免依赖 runtime-livedata）
                var unreadCount by remember { mutableIntStateOf(AAFMessageManager.getUnreadNum()) }
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.Observer<Any?> { _ ->
                        unreadCount = AAFMessageManager.getUnreadNum()
                    }
                    AAFMessageManager.getMessageLiveData().observe(lifecycleOwner, observer)
                    onDispose {
                        AAFMessageManager.getMessageLiveData().removeObserver(observer)
                    }
                }

                Row {
                    // 扫描二维码
                    IconButton(onClick = {
                        QrcodeUtils.openQrScan(this@TestMainActivityWithDrawer)
                    }) {
                        Icon(
                            modifier = Modifier.width(24.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.icon_scan),
                            contentDescription = "扫描二维码"
                        )
                    }
                    // 消息中心（带未读红点）
                    IconButton(onClick = {
                        RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
                    }) {
                        Box {
                            Icon(
                                modifier = Modifier.width(24.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.icon_message),
                                contentDescription = "消息中心"
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    BadgeView(
                                        num = unreadCount,
                                        badgeSmallSize = 14.dp,
                                        badgeLargeSize = 16.dp,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun getNavigationIcon(): ImageVector? {
        return null
    }

    override fun getDrawerContentContentRender(): RenderState? {
//        return getAAFNavigationDrawerContentRender()
        return null
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
