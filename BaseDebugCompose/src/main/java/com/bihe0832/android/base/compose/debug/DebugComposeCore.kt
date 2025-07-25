package com.bihe0832.android.base.compose.debug

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.DebugComposeItemManager
import com.bihe0832.android.common.compose.debug.DebugComposeManager
import com.bihe0832.android.common.compose.debug.DebugViewKey
import com.bihe0832.android.common.compose.ui.EmptyView

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/25.
 * Description: Description
 *
 */

object DebugComposeCore {
    const val DebugCommonCompose = "Common"
    fun init() {
        DebugComposeItemManager.setDebugComposeManagerImpl(object : DebugComposeManager {
            @Composable
            override fun getDebugComposeItem(key: DebugViewKey) {
                GetDebugItem(key)
            }
        })
    }

    @Composable
    fun GetDebugItem(item: DebugViewKey) {
        val result = if (item.viewKey.startsWith(DebugCommonCompose)) {
            GetCommonDebug(item.viewKey)
        } else {
            false
        }

        if (!result) {
            EmptyView(
                "没有匹配到对应的Compose组件\n\n ${item.viewKey}", MaterialTheme.colorScheme.error
            ) {

            }
        }
    }
}