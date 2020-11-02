package com.bihe0832.android.app.network.listener

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager


/**
 * Author: waynescliu
 * Date: 2020/8/12
 * Description:监听网络切换广播
 */
object NetworkChangeObserver {
    private const val TAG = "NetworkChangeObserver:::"
    private var networkChangeListeners: MutableList<NetworkChangeListener> = ArrayList<NetworkChangeListener>()

    fun addListener(networkChangeListener: NetworkChangeListener) {
        ZLog.d("$TAG addListener networkChangeListener:$networkChangeListener")
        networkChangeListeners.add(networkChangeListener)
    }

    fun removeListener(networkChangeListener: NetworkChangeListener) {
        ZLog.d("$TAG removeListener networkChangeListener:$networkChangeListener")
        networkChangeListeners.remove(networkChangeListener)
    }

    fun postNetworkChangeEvent(networkChangeEvent: NetworkChangeEvent) {
        ThreadManager.getInstance().runOnUIThread(Runnable {
            networkChangeListeners.forEach() {
                ZLog.d("$TAG postNetworkChangeEvent networkChangeListener:$it,networkChangeEvent:$networkChangeEvent")
                it.onNetworkChange(networkChangeEvent)
            }
        })
    }
}