package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.base.compose.debug.AAFDebugModuleView
import com.bihe0832.android.base.debug.AAFDebugModule
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.common.DebugComposeMainActivity
import com.bihe0832.android.common.compose.debug.common.DebugComposeMainWithDrawerActivity
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeView
import com.bihe0832.android.common.compose.state.RenderState
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
    }

    @Composable
    override fun getNavigationIcon(): ImageVector? {
        return null
    }
    
    fun getDrawerContentContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            getTitleName(),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        HorizontalDivider()

                        Text(
                            "Section 1",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text("Item 1") },
                            selected = false,
                            onClick = { /* Handle click */ }
                        )
                        NavigationDrawerItem(
                            label = { Text("Item 2") },
                            selected = true,
                            onClick = { /* Handle click */ }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "Section 2",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text("Settings") },
                            selected = false,
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            badge = { Text("20") }, // Placeholder
                            onClick = { }
                        )
                        NavigationDrawerItem(
                            label = { Text("Help and feedback") },
                            selected = false,
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null
                                )
                            },
                            onClick = { /* Handle click */ },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

        }
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
                DebugCommonComposeView {
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
                AAFDebugModuleView()
            }

            TAB_FOR_DEV -> {
                AAFRouterView()
            }

            else -> {
                AAFDebugModuleView()
            }
        }
    }


}
