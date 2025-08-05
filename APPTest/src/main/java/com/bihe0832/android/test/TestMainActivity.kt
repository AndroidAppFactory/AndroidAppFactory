package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.base.compose.debug.DebugComposeModuleView
import com.bihe0832.android.base.debug.AAFDebugModule
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.common.DebugComposeMainActivity
import com.bihe0832.android.common.compose.debug.module.AAFDebugCommonModuleView
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
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
    val TAB_FOR_COMPOSE: String = "Compose"
    val TAB_FOR_DEV: String = "开发测试"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLogTips.initModule(this, true, Gravity.LEFT or Gravity.TOP)
        CommonDBManager.init(this)
        ApplicationObserver.addDestoryListener(object : ApplicationObserver.APPDestroyListener {

            override fun onAllActivityDestroyed() {
                ZLog.d("onAllActivityDestroyed")
                R.mipmap.default_head_icon
            }
        })
    }

    override fun onResume() {
        super.onResume()
        DebugWidget.showAddDebugWidgetTips(this)
    }

    override fun getTabs(): List<String> {
        return mutableListOf(
            TAB_FOR_DEV_COMMON,
            TAB_FOR_DEV_MODULE,
            TAB_FOR_COMPOSE,
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
                AAFDebugCommonModuleView {
                    DebugUtilsV2.startActivityWithException(
                        context,
                        AAFDebugLogListActivity::class.java
                    )
                }
            }

            TAB_FOR_DEV_MODULE -> {
                AAFDebugModule()
            }

            TAB_FOR_COMPOSE -> {
                DebugComposeModuleView()
            }

            TAB_FOR_DEV -> {
                AAFRouterView()
            }

            else -> {
                DebugComposeModuleView()
            }
        }
    }


}
