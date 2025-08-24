package com.bihe0832.android.base.compose.debug.shake

import android.app.Service
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.device.shake.ShakeManager
import com.bihe0832.android.lib.device.shake.ShakeManagerImpl

@Preview
@Composable
fun DebugShakeView() {
    var lastTime = 0L
    var lastTimeState = rememberUpdatedState(lastTime)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ShakeManager.setOnShakeListener(object : ShakeManagerImpl.OnShakeListener {
            override fun onShake() {
                if (System.currentTimeMillis() - lastTimeState.value > 1000L) {
                    lastTime = System.currentTimeMillis()
                    ZixieContext.showDebug("Shake 1")
                    (context.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(
                            longArrayOf(50, 200, 100, 300), -1
                    )
                }
            }
        })

    }
    DisposableEffect(Unit) {

        onDispose {
            println("Composable is disposed")
        }
    }
    DebugContent {
        DebugTips(text = "摇一摇实时数值")
        DebugShakeValueView(speed = 1234.0, onStart = {
                                    ShakeManager.start()
        }, onStop = {

        })
        DebugTips(text = "摇一摇相关调试，设置后达到阈值会触发震动")
        DebugItem("摇一摇很灵敏") { ShakeManager.setSpeed(10) }
        DebugItem("摇一摇很迟钝") { ShakeManager.setSpeed(100) }
        DebugItem("摇一摇比较容易") { ShakeManager.setSpeed(30) }


    }
}