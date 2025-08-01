package com.bihe0832.android.common.compose.debug

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.ui.EmptyView
import com.bihe0832.android.lib.log.ZLog

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */


typealias ComposeViewProvider = @Composable () -> Unit

object DebugComposeItemManager {

    private val registry = mutableMapOf<String, ComposeViewProvider>()

    fun register(key: String, composable: @Composable () -> Unit) {
        if (registry.containsKey(key)) {
            ZLog.e(
                "\n\nDebugComposeItemManager : the same key :$key  has add before!!!\n\n"
            )
        }
        registry[key] = composable
    }

    @Composable
    fun GetDebugComposeItem(key: String) {
        val composable = registry[key]
        if (composable != null) {
            composable.invoke()
        } else {
            EmptyView(
                "没有匹配到对应的Compose组件\n\n $key", MaterialTheme.colorScheme.error
            ) {}
        }
    }


}