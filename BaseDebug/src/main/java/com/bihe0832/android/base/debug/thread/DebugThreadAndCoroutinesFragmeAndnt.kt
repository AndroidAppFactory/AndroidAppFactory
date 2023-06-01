package com.bihe0832.android.base.debug.thread

import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import kotlinx.coroutines.*

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/3/2.
 * Description: Description
 */
class DebugThreadAndCoroutinesFragmeAndnt : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("并发测试", View.OnClickListener { getSpecialData() }))
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


}