package com.bihe0832.android.lib.network.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.IpUtils
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-05-31.
 * Description: Description
 */

object WifiManagerWrapper {

    val TAG = "WifiManager-> "

    private var mContext: Context? = null

    private var mWifiManager: WifiManager? = null
    private var mConnectivityManager: ConnectivityManager? = null

    private val mWifiReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == WIFI_STATE_CHANGED_ACTION) {
                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                    // 上一次的wifi状态
                    //                    int wifiStatePre = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                    // 新的wifi状态
                    wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_DISABLED,
                    )
                    mWifiChangeListener?.onStateChanged(context, wifiState)
                    // 处理各种wifi状态
                    if (WifiManager.WIFI_STATE_ENABLED == wifiState || WifiManager.WIFI_STATE_ENABLING == wifiState) {
                        startScan()
                    }
                    if (isDebug) ZLog.d(TAG, " WIFI_STATE_CHANGED_ACTION ${getInfoString()}")
                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                } else if (action == WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) {
                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                    if (isDebug) ZLog.d(
                        TAG, " SUPPLICANT_CONNECTION_CHANGE_ACTION ${getInfoString()}"
                    )
                    checkBaseInfoNotify(context)
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        // TODO: 建立新连接
                    } else {
                        // TODO: 现有连接丢失
                    }

                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                } else if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                    checkConfiguredInfoNotify(context)
                    checkBaseInfoNotify(context)
                    if (isDebug) ZLog.d(TAG, " NETWORK_STATE_CHANGED_ACTION ${getInfoString()}")
                    if (isDebug) ZLog.d(TAG, "---------------------- ")
                } else if (action == WifiManager.RSSI_CHANGED_ACTION) {
                    if (isDebug || mNotifyRSSI) {
                        if (isDebug) ZLog.d(TAG, "---------------------- ")
                        updateBaseInfo()
                        if (isDebug) ZLog.d(TAG, " RSSI_CHANGED_ACTION ${getInfoString()}")
                        if (isDebug) ZLog.d(TAG, "---------------------- ")
                    }
                } else if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    if (isDebug || mCanScanWifi) {
                        updateScanListInfo()
                        mWifiChangeListener?.onScanUpdate(context, mWifiList)
                    }
                }
            }
        }
    }

    private var mWifiChangeListener: OnWifiChangerListener? = null

    private var isDebug = false
    private var hasInit = false

    // 是否通知 Wi-Fi 强度
    private var mNotifyRSSI = false

    // 可以扫描周边Wi-Fi
    private var mCanScanWifi = false

    // 可以获取已连接Wi-Fi列表
    private var mCanWifiConfiguration = false

    // 扫描出的网络连接列表
    private var mWifiList: List<ScanResult>? = null

    // 已经连接的网络连接列表
    private var mWifiConfigurationList: List<WifiConfiguration>? = null

    // 当前wifi的状态
    // 获取wifi状态，开启关闭等
    var wifiState = WifiManager.WIFI_STATE_DISABLED
        internal set

    // 当前连接热点的信息
    private var mWifiInfo: WifiInfo? = null
    private var mWiFiNetworkInfo: NetworkInfo? = null

    interface OnWifiChangerListener {
        /**
         * 返回当前手机的Wi-Fi状态
         * @param state 当前Wi-F状态 ，取值参考 [WifiManager.getWifiState].
         */
        fun onStateChanged(context: Context?, state: Int)

        /**
         * 当前连接的Wi-Fi热点的信息发生变化
         * 收到回调以后，可调用 [.getSSID] 等获取最新信息
         */
        fun onWifiInfoChanged(context: Context?)

        /**
         * Wi-Fi扫描时发现了新的Wi-Fi
         * @param wifiList 返回最新的可发现的Wi-Fi列表
         */
        fun onScanUpdate(context: Context?, wifiList: List<ScanResult?>?)

        /**
         * 返回手机已保存的Wi-Fi网络列表
         * @param wifiConfigurationList 返回最新的已保存的Wi-Fi网络列表
         */
        fun onConnectUpdate(context: Context?, wifiConfigurationList: List<WifiConfiguration?>?)
    }

    fun setWifiChangedListener(listener: OnWifiChangerListener?) {
        if (null == listener) {
            mWifiChangeListener = null
        } else {
            mWifiChangeListener = listener
            if (hasInit) {
                refreshWifiInfo()
                listener.onWifiInfoChanged(mContext)
                listener.onScanUpdate(mContext, getScanResultList())
                listener.onConnectUpdate(mContext, getConfigurationList())
            }
        }
    }

    // 得到可以直连的网络
    fun getConfigurationList(): List<WifiConfiguration?> {
        return when (mWifiConfigurationList) {
            null -> ArrayList()
            is List<WifiConfiguration> -> mWifiConfigurationList as List<WifiConfiguration?>
            else -> ArrayList()
        }
    }

    // 得到发现网络列表
    fun getScanResultList(): List<ScanResult> {
        return when (mWifiList) {
            null -> ArrayList()
            is List<ScanResult> -> mWifiList as List<ScanResult>
            else -> ArrayList()
        }
    }

    // Wifi的名称
    fun getSSID(): String {
        if (isDebug) ZLog.d(TAG, "-----------getSSID----------- ")
        mWifiInfo?.let {
            if (TextFactoryUtils.trimMarks(it.ssid)
                    .equals(WifiUtil.DEFAULT_SSID, ignoreCase = true)
            ) {
                getConfiguredByNetworkID(getNetworkId())?.let { wifiConfiguration ->
                    return TextFactoryUtils.trimMarks(wifiConfiguration.SSID)
                }
            } else {
                return TextFactoryUtils.trimMarks(it.ssid)
            }
        }
        return WifiUtil.DEFAULT_SSID
    }

    // 得到接入点的BSSID
    fun getBSSID(): String {
        if (isDebug) ZLog.d(TAG, "-----------getBSSID----------- ")
        mWifiInfo?.let {
            if (it.bssid?.isNotBlank() == true) {
                return it.bssid
            }
        }
        return WifiUtil.INVALID_BSSID
    }

    // Wifi信号强度
    fun getRssi(): Int {
        mWifiInfo?.let {
            return it.rssi
        }
        return -126
    }

    // Wifi信号强度
    fun getSupplicantState(): SupplicantState {
        mWifiInfo?.let {
            return it.supplicantState
        }
        return SupplicantState.DISCONNECTED
    }

    // Wifi信号强度等级
    fun getSignalLevel(): Int {
        return WifiUtil.getWifiSignalLevel(mWifiManager, getRssi(), 5)
    }

    // 连接速度
    fun getLinkSpeed(): Int {
        mWifiInfo?.let {
            return it.linkSpeed
        }
        return -1
    }

    // 连接速度单位
    fun getLinkSpeedUnits(): String {
        return WifiInfo.LINK_SPEED_UNITS
    }

    // 得到IP地址
    fun getIpAddress(): Int {
        mWifiInfo?.let {
            return it.ipAddress
        }
        return 0
    }

    fun getIpString(): String {
        return IpUtils.ipn2s(getIpAddress())
    }

    fun getGatewayIpString(): String {
        return WifiUtil.getGatewayIp(mWifiManager)
    }

    fun getGatewayMac(): String {
        return WifiUtil.getGatewayMac(mWifiManager)
    }

    // 得到连接的ID
    private fun getNetworkId(): Int {
        mWifiInfo?.let {
            return it.networkId
        }
        return -1
    }

    fun getFrequency(): Int {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWifiInfo?.let {
                return it.frequency
            }
        }
        return 0
    }

    // 判断是否是5GWiFi
    fun is5GHzWiFi(): Boolean {
        return FrequencyInfo.is5GHzWiFi(getFrequency())
    }

    // 获取当前连接的wifi的配置信息
    fun getWifiConfiguration(): WifiConfiguration? {
        return getConfiguredByNetworkID(getNetworkId())
    }

    enum class WifiStatus {
        NOT_CONNECTED, CONNECTED_NO_INTERNET, CONNECTED
    }

    fun getWifiStatus(): WifiStatus {
        val connectivityManager =
            mContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return if (mWifiManager?.isWifiEnabled == true) {
            val wifiNetworkInfo = connectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiNetworkInfo?.isConnected == true) {
                if (BuildUtils.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNetwork = connectivityManager.activeNetwork
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (networkCapabilities != null && networkCapabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED
                        )
                    ) {
                        WifiStatus.CONNECTED
                    } else {
                        WifiStatus.CONNECTED_NO_INTERNET
                    }
                } else {
                    if (wifiNetworkInfo.isConnectedOrConnecting) {
                        WifiStatus.CONNECTED
                    } else {
                        WifiStatus.CONNECTED_NO_INTERNET
                    }
                }
            } else {
                WifiStatus.NOT_CONNECTED
            }
        } else {
            WifiStatus.NOT_CONNECTED
        }
    }

    // 得到WifiInfo的所有信息包
    fun getInfoString(): String {
        val buffer = StringBuffer()
        mWifiInfo?.let {
            buffer.append("\n\tWifiInfo-> ").append(it.toString())
        }
        mWiFiNetworkInfo?.let {
            buffer.append("\n\tnetwork-> ").append(it.toString())
        }

        getWifiConfiguration()?.let {
            buffer.append("\n\tWifiConfiguration-> ").append(getWifiConfigurationString(it))
        }
        buffer.append("\n\twifi state-> $wifiState")
            .append(";network state-> ${mWifiManager?.isWifiEnabled}")
        buffer.append("\n\tmWifiList-> " + getScanResultList().size)
            .append(";mWifiConfigurationList-> " + getConfigurationList().size)
        return buffer.toString()
    }

    fun getChannel(bssid: String): Int {
        return WifiChannelInfo.getWiFiChannel(mWifiList, bssid)
    }

    fun getSecurity(): Int {
        return WifiUtil.getSecurity(getWifiConfiguration())
    }

    fun getConfiguredBySSID(ssid: String): WifiConfiguration? {
        if (TextUtils.isEmpty(ssid)) {
            return null
        }
        getConfigurationList()?.let {
            if (it.isNotEmpty()) {
                for (existingConfig in it) {
                    if (TextFactoryUtils.trimMarks(existingConfig?.SSID) == ssid) {
                        return existingConfig
                    }
                }
            }
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectWifiAboveQ(
        context: Context?,
        ssid: String,
        password: String,
        networkCallback: ConnectivityManager.NetworkCallback,
        isWap3: Boolean = false
    ) {
        val wpaSpecifier = WifiNetworkSpecifier.Builder().apply {
            setSsid(ssid)
            if (isWap3) {
                setWpa3Passphrase(password)
            } else {
                setWpa2Passphrase(password)
            }
        }.build()

        val networkRequest = NetworkRequest.Builder().apply {
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            setNetworkSpecifier(wpaSpecifier)
        }.build()
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager?.requestNetwork(networkRequest, networkCallback)
    }

    fun connectWifi(
        context: Context?,
        ssid: String,
        password: String,
        forceDeleteIfExist: Boolean = true,
        isWep: Boolean = false,
    ): Boolean {
        var networkId = -1
        var tempConfig = getConfiguredBySSID(ssid)
        // 如果该SSID已经连接过，而且本次连接需要强制重连，直接remove历史
        if (tempConfig != null && forceDeleteIfExist) {
            mWifiManager?.removeNetwork(tempConfig.networkId)
            tempConfig = null
        }

        networkId = if (null != tempConfig && tempConfig.networkId > -1) {
            tempConfig.networkId
        } else {
            val wcg = createWifiInfo(ssid, password, isWep)
            mWifiManager?.addNetwork(wcg) ?: -1
        }

        return connectWifi(context, networkId)
    }

    // 指定配置好的网络进行连接
    fun connectWifi(context: Context?, networkId: Int): Boolean {
        return if (networkId > -1 && mWifiManager?.connectionInfo?.networkId != networkId) {
            mWifiManager?.disconnect()
            mWifiManager?.enableNetwork(networkId, true)
            mWifiManager?.reconnect()
            true
        } else {
            mWifiChangeListener?.let {
                it.onWifiInfoChanged(context)
            }
            false
        }
    }

    // Wifi的名称
    fun isCurrent(ssid: String): Boolean {
        getConfiguredBySSID(ssid)?.let {
            return it.networkId > 0 && it.networkId == getNetworkId()
        }
        return false
    }

    // 打开WIFI
    fun openWifi() {
        if (mConnectivityManager?.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI) {
            return
        } else {
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext?.startActivity(intent)
            } else {
                mWifiManager?.isWifiEnabled = true
            }
            startScan()
        }
    }

    // 关闭WIFI
    fun closeWifi() {
        if (mWifiManager?.isWifiEnabled == true) {
            mWifiManager?.isWifiEnabled = false
        }
    }

    fun getWifiManager(): WifiManager? {
        return mWifiManager
    }

    // 扫描列表
    fun startScan() {
        ThreadManager.getInstance().start { mWifiManager?.startScan() }
    }

    fun init(context: Context, debug: Boolean) {
        init(context, debug, notifyRSSI = false, canScanWifi = false, canWifiConfiguration = false)
    }

    fun init(
        context: Context,
        debug: Boolean,
        notifyRSSI: Boolean,
        canScanWifi: Boolean,
        canWifiConfiguration: Boolean
    ) {
        // 取得WifiManager对象
        mContext = context
        isDebug = debug
        mNotifyRSSI = notifyRSSI
        mCanScanWifi = canScanWifi
        mCanWifiConfiguration = canWifiConfiguration
        if (hasInit) {
            context.applicationContext.unregisterReceiver(mWifiReceiver)
            register(context)
            return
        }
        hasInit = true

        mWifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mConnectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 取得WifiInfo对象
        refreshWifiInfo()
        register(context)
    }

    fun refreshWifiInfo() {
        updateBaseInfo()
        updateConfiguredListInfo()
        updateScanListInfo()
    }

    private fun register(context: Context) {
        // 需要过滤多个动作，则调用IntentFilter对象的addAction添加新动作
        val foundFilter = IntentFilter()
        // 监听Wifi当前网络状态
        foundFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        // 监听Wifi硬件状态(关闭、开启、...)
        foundFilter.addAction(WIFI_STATE_CHANGED_ACTION)
        // 监听与接入点之间的连接状态(新连接建立或者现有连接丢失)
        foundFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
        if (mNotifyRSSI) {
            // 监听Wifi信号强度变化
            foundFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
        }

        if (mCanScanWifi) {
            // 监听Wifi发现新的接入点
            foundFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        }

        context.applicationContext.registerReceiver(mWifiReceiver, foundFilter)
    }

    // 更新当前连接的wifi的信息
    private fun updateBaseInfo() {
        mWifiInfo = mWifiManager?.connectionInfo
        mWiFiNetworkInfo = mConnectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    }

    // 更新wifi列表信息
    private fun updateScanListInfo() {
        if (mCanScanWifi) {
            // 得到扫描结果
            try {
                mWifiList = mWifiManager?.scanResults
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateConfiguredListInfo() {
        // 得到配置好的网络连接
        if (mCanWifiConfiguration) {
            try {
                mWifiConfigurationList = mWifiManager?.configuredNetworks
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkBaseInfoNotify(context: Context?) {
        val temp = mWifiInfo
        updateBaseInfo()
        if (isDebug) ZLog.d(TAG + " networkId ${temp?.networkId} ;networkId ${getNetworkId()}")
        if (isDebug) ZLog.d(TAG + " SupplicantState ${temp?.supplicantState} ;SupplicantState ${mWifiInfo?.supplicantState}")
        mWifiChangeListener?.let { listener ->
            if (null != temp) {
                when {
                    temp.networkId != getNetworkId() -> // 连接新Wi-Fi
                        listener.onWifiInfoChanged(context)

                    temp.supplicantState != mWifiInfo?.supplicantState -> // 当前连接Wi-Fi 状态变化
                        listener.onWifiInfoChanged(context)

                    else -> {}
                }
            } else {
                mWifiInfo?.let {
                    listener.onWifiInfoChanged(context)
                }
            }
        }
    }

    private fun checkConfiguredInfoNotify(context: Context?) {
        val size = getConfigurationList().size
        updateConfiguredListInfo()
        if (isDebug) ZLog.d(TAG + " size $size ;size  + ${getConfigurationList().size}")
        mWifiChangeListener?.let {
            if (getConfigurationList().isNotEmpty() && getConfigurationList().size != size) {
                it.onConnectUpdate(context, getConfigurationList())
            }
        }
    }

    private fun getWifiConfigurationString(info: WifiConfiguration?): String {
        val sbuf = StringBuilder()
        info?.let {
            sbuf.append("SSID:").append(info.SSID)
            sbuf.append(";networkId:").append(info.networkId)
            sbuf.append(";status:").append(info.status)
            sbuf.append(";BSSID:").append(info.BSSID)
        }
        return sbuf.toString()
    }

    private fun getConfiguredByNetworkID(networkId: Int): WifiConfiguration? {
        if (networkId < 0) {
            return null
        }
        getConfigurationList().let {
            if (it.isNotEmpty()) {
                for (existingConfig in it) {
                    if (existingConfig?.networkId == networkId) {
                        return existingConfig
                    }
                }
            }
        }

        return null
    }

    private fun createWifiInfo(
        ssid: String,
        password: String,
        isWep: Boolean = false,
    ): WifiConfiguration {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"" + ssid + "\""
        if (TextUtils.isEmpty(password)) {
            // Open network
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else if (isWep) {
            // WEP network
            wifiConfig.wepKeys[0] = "\"" + password + "\""
            wifiConfig.wepTxKeyIndex = 0
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
        } else {
            // WPA/WPA2 network
            wifiConfig.preSharedKey = "\"" + password + "\""
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        }
        return wifiConfig
    }
}
