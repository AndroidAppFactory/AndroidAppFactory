package com.bihe0832.android.common.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Handler
import com.bihe0832.android.framework.ZixieContext.isDebug
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper.getBSSID
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper.getSSID
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper.getSignalLevel
import com.bihe0832.android.lib.network.wifi.WifiUtil
import com.bihe0832.android.lib.thread.ThreadManager

object NetworkChangeManager {

    private const val TAG = "NetworkChangeManager"

    private var canGetNetType = false
    private var canGetBSSID = false
    private var canGetSSID = false
    private var canGetCELLID = false

    private var cachedNetType = NetworkUtil.NETWORK_CLASS_UNKNOWN
    private var cachedWifiBssId: String = WifiUtil.INVALID_BSSID
    private var cachedWifiSsid: String = WifiUtil.DEFAULT_SSID
    private var cachedCellID: String? = "-1_-1_-1_-1"

    private var mNetReceiver: BroadcastReceiver? = null
    private var mBgHandler: Handler? = null
    private val networkChangeListeners = ArrayList<NetworkChangeListener>()

    fun init(context: Context, getNetType: Boolean, getSSID: Boolean = false, getBssID: Boolean = false, curCellId: Boolean = false) {
        DeviceInfoManager.getInstance().init(context.applicationContext)
        WifiManagerWrapper.init(context, isDebug())

        canGetNetType = getNetType
        canGetBSSID = getBssID
        canGetSSID = getSSID
        canGetCELLID = curCellId
        mBgHandler = Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL))

        if (canGetNetType) {
            cachedNetType = NetworkUtil.getNetworkState(context)
        }
        if (NetworkUtil.isWifiNet(cachedNetType)) {
            if (canGetBSSID) {
                cachedWifiBssId = getBSSID()
            }

            if (canGetSSID) {
                cachedWifiSsid = getSSID()
            }
        } else if (NetworkUtil.isMobileNet(cachedNetType) && canGetCELLID) {
            cachedCellID = MobileUtil.getPhoneCellInfo(context)
        }
        listenNetChange(context)
    }

    private fun listenNetChange(ctx: Context) {
        mNetReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                change(context.applicationContext, intent)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        ctx.registerReceiver(mNetReceiver, intentFilter)
    }

    private fun change(context: Context, intent: Intent) {
        ZLog.d(TAG, "change")
        if (mBgHandler == null) {
            init(context, canGetNetType, canGetBSSID, canGetCELLID)
            return
        }
        ThreadManager.getInstance().start {
            val curNetType: Int = getNetType(context)
            ZLog.d(TAG, "network change >> netType: $curNetType, sPreNetType: $cachedNetType")
            var curBssid = WifiUtil.INVALID_BSSID
            var curCellId = "-1_-1_-1_-1"
            var curWifiSsid = WifiUtil.DEFAULT_SSID
            var curWifiSignal = 0
            var needPost = false
            ZLog.d(TAG, "network change >> netType correctAfter: $curNetType,intent.getAction():" + intent.action)
            if (NetworkUtil.isWifiNet(curNetType)) {
                if (canGetBSSID) {
                    curBssid = getBSSID()
                }
                if (canGetSSID) {
                    curWifiSsid = getSSID()
                }

                curWifiSignal = getSignalLevel()
                if (curNetType != cachedNetType || cachedWifiBssId != null && cachedWifiBssId != curBssid || cachedWifiSsid != null && cachedWifiSsid != curWifiSsid) {
                    needPost = true
                }
            } else if (NetworkUtil.isMobileNet(curNetType) && canGetCELLID) {
                curCellId = MobileUtil.getPhoneCellInfo(context)
                if (curNetType != cachedNetType || cachedCellID != null && cachedCellID != curCellId) {
                    needPost = true
                }
            } else {
                if (curNetType != cachedNetType) {
                    needPost = true
                }
            }
            if (needPost) {
                ZLog.e(TAG, "notify change: preNetType:$cachedNetType curNetType:$curNetType curWifiSsid:$curWifiSsid preBssId:$cachedWifiBssId bssId:$curBssid curWifiSignal:$curWifiSignal curCellId:$curCellId")
                postNetworkChangeEvent(cachedNetType, curNetType, intent)
                cachedWifiBssId = curBssid
                cachedWifiSsid = curWifiSsid
                cachedCellID = curCellId
                cachedNetType = curNetType
            }
        }
    }

    interface NetworkChangeListener {
        fun onNetworkChange(sPreNetType: Int, curNetType: Int, intent: Intent?)
    }

    fun addListener(networkChangeListener: NetworkChangeListener) {
        if (null != networkChangeListener) {
            networkChangeListeners.add(networkChangeListener)
        }
    }

    fun removeListener(networkChangeListener: NetworkChangeListener) {
        if (null != networkChangeListener) {
            networkChangeListeners.remove(networkChangeListener)
        }
    }

    fun postNetworkChangeEvent(sPreNetType: Int, curNetType: Int, intent: Intent?) {
        for (networkChangeListener in networkChangeListeners) {
            networkChangeListener.onNetworkChange(sPreNetType, curNetType, intent)
        }
    }

    fun getNetType(context: Context): Int {
        return NetworkUtil.getNetworkState(context)
    }

    fun getCachedNetType(): Int {
        return cachedNetType
    }

    fun getNetTypeName(context: Context): String {
        return NetworkUtil.getNetworkName(NetworkUtil.getNetworkState(context))
    }

    fun getCachedNetTypeName(): String {
        return NetworkUtil.getNetworkName(cachedNetType)
    }

    // 主要提供在WiFi下获取移动网络的网络类型，不保证移动网络已连接，仅是信号类型
    fun getMobileNetType(context: Context): Int {
        return NetworkUtil.getMobileNetworkClass(context, null)
    }

    fun isMobileNet(networkType: Int): Boolean {
        return NetworkUtil.isMobileNet(networkType)
    }

    fun isMobileNet(context: Context): Boolean {
        return isMobileNet(NetworkUtil.getNetworkState(context))
    }

    fun isWifiNet(networkType: Int): Boolean {
        return NetworkUtil.isWifiNet(networkType)
    }

    fun isWifiNet(context: Context?): Boolean {
        return isWifiNet(NetworkUtil.getNetworkState(context))
    }

    fun isEtherNet(networkType: Int): Boolean {
        return NetworkUtil.isEtherNet(networkType)
    }

    fun isEtherNet(context: Context): Boolean {
        return isEtherNet(NetworkUtil.getNetworkState(context))
    }

    fun isBluetoothNet(networkType: Int): Boolean {
        return NetworkUtil.isBluetoothNet(networkType)
    }

    fun isBluetoothNet(context: Context): Boolean {
        return NetworkUtil.isBluetoothNet(context)
    }
}