package com.bihe0832.android.base.compose.debug.shake

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.device.shake.ShakeManager
import com.bihe0832.android.lib.device.shake.ShakeManagerImpl

@SuppressLint("AutoboxingStateCreation")
@Preview
@Composable
fun DebugShakeView() {

    val context = LocalContext.current
    // 1. 使用 mutableStateOf 管理状态
    var lastSpeed by remember { mutableStateOf("") }
    var max by remember { mutableStateOf(0) }
    var lastTime by remember { mutableLongStateOf(0L) }

    val mShakeManagerImpl = remember {
        object : ShakeManagerImpl() {
            override fun onShake(speed: Double, time: Int) {
                super.onShake(speed, time)
                lastSpeed = "实时数值：" + speed.toInt() / 100 + "，对应速度：" + speed.toInt()
                if (speed > max) {
                    max = speed.toInt()
                }
            }

            override fun stop() {
                super.stop()
                lastSpeed = "本轮摇动最大数值：" + max / 100 + "，对应速度：" + max
            }

            override fun start(context: Context) {
                super.start(context)
                max = 0
            }
        }
    }
    DisposableEffect(Unit) {
        // 初始化
        ShakeManager.setOnShakeListener(object : ShakeManagerImpl.OnShakeListener {
            override fun onShake() {
                if (System.currentTimeMillis() - lastTime > 1000L) {
                    lastTime = System.currentTimeMillis()
                    ZixieContext.showDebug("Shake 1")
                    (context.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(
                        longArrayOf(50, 200, 100, 300), -1
                    )
                }
            }
        })

        // 清理
        onDispose {
            ShakeManager.stop()
            ShakeManager.setOnShakeListener(null)
        }
    }

    DebugContent {
        DebugTips(text = "摇一摇实时数值")
        DebugShakeValueView(speed = lastSpeed, onStart = {
            mShakeManagerImpl.start(context)
            ShakeManager.start(context)
        }, onStop = {
            mShakeManagerImpl.stop()
            ShakeManager.stop()
        })
        DebugTips(text = "摇一摇相关调试，设置后达到阈值会触发震动")
        DebugItem("摇一摇很灵敏(10)") { ShakeManager.setSpeed(10) }
        DebugItem("摇一摇很迟钝(100)") { ShakeManager.setSpeed(100) }
        DebugItem("摇一摇比较容易(30)") { ShakeManager.setSpeed(30) }


    }
}