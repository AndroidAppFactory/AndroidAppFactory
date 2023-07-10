package com.bihe0832.android.common.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import com.bihe0832.android.framework.ZixieContext.isDebug
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.DtTypeInfo
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.network.wifi.WifiUtil

object NetworkChangeManager {

    private const val TAG = "NetworkChangeManager"

    private var canGetNetType = false
    private var canGetBSSID = false
    private var canGetSSID = false
    private var canGetCELLID = false

    private var cachedNetType = NetworkUtil.NETWORK_CLASS_UNKNOWN
    private var cachedDtTypeInfo: DtTypeInfo = DtTypeInfo()

    private var cachedWifiBssId: String = WifiUtil.INVALID_BSSID
    private var cachedWifiSsid: String = WifiUtil.DEFAULT_SSID
    private var cachedCellID: String? = "-1_-1_-1_-1"

    private var mNetReceiver: BroadcastReceiver? = null
    private val networkChangeListeners = ArrayList<NetworkChangeListener>()

    fun getLastNetType(): Int {
        return cachedNetType
    }

    fun getLastNetTypeName(): String {
        return NetworkUtil.getNetworkName(getLastNetType())
    }

    fun getLastDtTypeInfo(): DtTypeInfo {
        return cachedDtTypeInfo
    }

    fun getLastWifiBssId(): String {
        return cachedWifiBssId
    }

    fun getLastWifiSSID(): String {
        return cachedWifiSsid
    }

    fun getRealNetTypeName(context: Context): String {
        return NetworkUtil.getNetworkName(NetworkUtil.getNetworkState(context))
    }

    fun getRealNetType(context: Context): Int {
        return NetworkUtil.getNetworkState(context)
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

    fun init(context: Context, getNetType: Boolean, getSSID: Boolean = false, getBssID: Boolean = false, curCellId: Boolean = false) {
        DeviceInfoManager.getInstance().init(context.applicationContext)
        WifiManagerWrapper.init(context, isDebug(), notifyRSSI = false, canScanWifi = false)
        canGetNetType = getNetType
        canGetBSSID = getBssID
        canGetSSID = getSSID
        canGetCELLID = curCellId
        refreshInfo(context)
        listenNetChange(context)
    }

    fun refreshInfo(context: Context?) {
        WifiManagerWrapper.refreshWifiInfo()
        if (canGetNetType) {
            cachedNetType = NetworkUtil.getNetworkState(context)
            cachedDtTypeInfo = DtTypeInfo.getDtTypeInfo(context)
        }

        if (NetworkUtil.isWifiNet(cachedNetType)) {
            if (canGetBSSID) {
                cachedWifiBssId = WifiManagerWrapper.getBSSID()
            }

            if (canGetSSID) {
                cachedWifiSsid = WifiManagerWrapper.getSSID()
            }
        } else if (NetworkUtil.isMobileNet(cachedNetType) && canGetCELLID) {
            cachedCellID = MobileUtil.getPhoneCellInfo(context)
        }
    }

    private fun listenNetChange(ctx: Context) {
        mNetReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                innerNetworkChanged(context.applicationContext, intent)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        if (canGetSSID || canGetBSSID) {
            intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        ctx.registerReceiver(mNetReceiver, intentFilter)

        WifiManagerWrapper.setWifiChangedListener(object : WifiManagerWrapper.OnWifiChangerListener {
            override fun onStateChanged(context: Context?, state: Int) {
                innerNetworkChanged(context, null)
            }

            override fun onWifiInfoChanged(context: Context?) {
                innerNetworkChanged(context, null)
            }

            override fun onScanUpdate(context: Context?, wifiList: List<ScanResult?>?) {

            }

            override fun onConnectUpdate(context: Context?, wifiConfigurationList: List<WifiConfiguration?>?) {

            }

        })
    }

    private fun innerNetworkChanged(context: Context?, intent: Intent?) {
        ZLog.d(TAG, "change")
        var oldNetType = cachedNetType
        var oldDtType = cachedDtTypeInfo
        refreshInfo(context)
        ZLog.d(TAG, "network change >> oldNetType: $oldNetType, cachedNetType: $cachedNetType, oldDtType $oldDtType, cachedDtType $cachedDtTypeInfo")
        ZLog.w(TAG, "network change >> netType correctAfter: $cachedNetType ,intent.getAction():" + intent?.action)
        if (intent?.action?.equals(LocationManager.PROVIDERS_CHANGED_ACTION) == true || oldNetType != cachedNetType || oldDtType != cachedDtTypeInfo) {
            postNetworkChangeEvent(oldNetType, cachedNetType, intent)
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
}