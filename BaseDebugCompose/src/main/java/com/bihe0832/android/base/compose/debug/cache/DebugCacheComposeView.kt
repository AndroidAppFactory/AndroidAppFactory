package com.bihe0832.android.base.compose.debug.cache

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.config.OnConfigChangedListener
import com.bihe0832.android.lib.log.ZLog


private const val LOG_TAG = "DebugCacheComposeView"

@Preview
@Composable
fun DebugCacheComposeView() {
    val configListener = object : OnConfigChangedListener {
        override fun onValueChanged(key: String?, value: String?) {
            ZLog.d(LOG_TAG, "onNewValue config key: $key value: $value")
        }

        override fun onValueAgain(key: String?, value: String?) {
            ZLog.d(LOG_TAG, "onValueSetted config key: $key value: $value")
        }
    }
    LaunchedEffect(Unit) {
        Config.addOnConfigChangedListener(configListener)
    }
    DisposableEffect(Unit) {
        Config.removeOnConfigChangedListener(configListener)
        onDispose {
            println("Composable is disposed")
        }
    }
    DebugContent {
        DebugItem("配置 Config 管理测试") { testConfig() }

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

private fun testConfig() {
    try {
//            var startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            var duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.writeConfig("test$i", i.toString())
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig write 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
        var key = "aaa"
        ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
        Config.writeConfig(key, true)
        ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
        Config.writeConfig(key, false)
        ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
        Config.writeConfig(key, Config.isSwitchEnabled(key, false))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
