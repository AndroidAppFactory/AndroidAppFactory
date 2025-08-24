package com.bihe0832.android.base.compose.debug.task

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import kotlinx.coroutines.*

@Composable
fun DebugThreadAndCoroutinesView() {
    DebugContent {
        DebugItem("并发测试") { getSpecialData() }
        DebugItem("多层调用验证") { testThread() }
        DebugItem("前台服务任务1开启") { start1(it) }
        DebugItem("前台服务任务2开启") { start2(it) }
        DebugItem("前台服务任务1结束") { stop1(it) }
        DebugItem("前台服务任务2结束") { stop2(it) }
    }
}

internal fun getSpecialData() {

    var start = System.currentTimeMillis()
    CoroutineScope(Dispatchers.Default).launch {
        var start = System.currentTimeMillis()
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        val three = async { doSomethingUsefulThree() }
        val four = async { doSomethingUsefulFour() }
        println("The answer is: ${one.await() + two.await() + three.await() + four.await()}")
        println("Completed inner in ${System.currentTimeMillis() - start} ms")
    }
    println("Completed outer in ${System.currentTimeMillis() - start} ms")
}

internal suspend fun doSomethingUsefulOne(): String {
    delay(1000L) // 假设我们在这里做了一些有用的事
    return "A"
}

internal suspend fun doSomethingUsefulTwo(): String {
    delay(1000L) // 假设我们在这里也做了一些有用的事
    return "B"
}


internal suspend fun doSomethingUsefulThree(): String {
    delay(1000L) // 假设我们在这里做了一些有用的事
    return "C"
}

internal suspend fun doSomethingUsefulFour(): String {
    delay(1000L) // 假设我们在这里也做了一些有用的事
    return "D"
}

internal fun testThread() {
    ThreadManager.getInstance().start {
        ZLog.d("testThread: 1")
        ThreadManager.getInstance().start {
            ZLog.d("testThread: 2")
            ThreadManager.getInstance().start {
                ZLog.d("testThread: 3")
                ThreadManager.getInstance().start {
                    ZLog.d("testThread: 4")
                    ThreadManager.getInstance().start {
                        ZLog.d("testThread: 5")
                        ThreadManager.getInstance().start {
                            ZLog.d("testThread: 6")
                            ThreadManager.getInstance().start {
                                ZLog.d("testThread: 7")
                            }
                        }
                    }
                }
            }
        }
    }
}

val scene1 = "scene1"
val scene2 = "scene2"
internal fun start1(context: Context) {
    AAFForegroundServiceManager.sendToForegroundService(context, Intent().apply {
        putExtra("fdsfsd1", "Fsdfsf1")
    }, object : AAFForegroundServiceManager.ForegroundServiceAction {
        override fun getScene(): String {
            return scene1
        }

        override fun getNotifyContent(): String {
            return "预下载"
        }

        override fun onStartCommand(context: Context, intent: Intent, flags: Int, startId: Int) {
        }

    })
}

internal fun start2(context: Context) {
    AAFForegroundServiceManager.sendToForegroundService(context, Intent().apply {
        putExtra("fdsfsd2", "Fsdfsf2")
    }, object : AAFForegroundServiceManager.ForegroundServiceAction {
        override fun getScene(): String {
            return scene2
        }

        override fun getNotifyContent(): String {
            return "预下载2"
        }

        override fun onStartCommand(context: Context, intent: Intent, flags: Int, startId: Int) {
        }

    })
}

internal fun stop1(context: Context) {
    AAFForegroundServiceManager.deleteFromForegroundService(context, scene1)
}

internal fun stop2(context: Context) {
    AAFForegroundServiceManager.deleteFromForegroundService(context, scene2)
}