package com.bihe0832.android.base.debug.task

import android.content.Context
import android.content.Intent
import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/3/2.
 * Description: Description
 */
/**
 * @author zixie code@bihe0832.com
 * Created on 2023/3/2.
 * Description: Description
 */
class DebugThreadAndCoroutinesFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("并发测试", View.OnClickListener { getSpecialData() }))
            add(getDebugItem("多层调用验证", View.OnClickListener { testThread() }))
            add(getDebugItem("前台服务任务1开启", View.OnClickListener { start1() }))
            add(getDebugItem("前台服务任务2开启", View.OnClickListener { start2() }))
            add(getDebugItem("前台服务任务1结束", View.OnClickListener { stop1() }))
            add(getDebugItem("前台服务任务2结束", View.OnClickListener { stop2() }))

        }
    }

    fun getSpecialData() {

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

    suspend fun doSomethingUsefulOne(): String {
        delay(1000L) // 假设我们在这里做了一些有用的事
        return "A"
    }

    suspend fun doSomethingUsefulTwo(): String {
        delay(1000L) // 假设我们在这里也做了一些有用的事
        return "B"
    }


    suspend fun doSomethingUsefulThree(): String {
        delay(1000L) // 假设我们在这里做了一些有用的事
        return "C"
    }

    suspend fun doSomethingUsefulFour(): String {
        delay(1000L) // 假设我们在这里也做了一些有用的事
        return "D"
    }

    fun testThread() {
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
    fun start1() {
        AAFForegroundServiceManager.sendToForegroundService(context!!, Intent().apply {
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

    fun start2() {
        AAFForegroundServiceManager.sendToForegroundService(context!!, Intent().apply {
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

    fun stop1() {
        AAFForegroundServiceManager.deleteFromForegroundService(context!!, scene1)
    }

    fun stop2() {
        AAFForegroundServiceManager.deleteFromForegroundService(context!!, scene2)
    }
}