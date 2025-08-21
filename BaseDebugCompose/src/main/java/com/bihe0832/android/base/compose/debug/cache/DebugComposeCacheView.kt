package com.bihe0832.android.base.compose.debug.cache

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent


@Preview
@Composable
fun DebugComposeCacheViewPreview() {
    DebugComposeCacheView()
}

@Composable
fun DebugComposeCacheView() {
    DebugContent {
        DebugItem("测试数据缓存效果") {
            DebugInfoCacheManager.loggerData()
        }
        DebugItem("测试数据丢弃") {
            for (i in 0..5) {
                DebugInfoCacheManager.addData("TestCache$i", DebugCacheData().apply {
                    this.key = "TestCache$i"
                })
            }
        }
    }
}

