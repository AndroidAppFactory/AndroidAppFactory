package com.bihe0832.android.base.debug.network

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.DtTypeInfo
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper

class DebugNetworkActivity : BaseActivity() {

    private val networkChangeListener = object : NetworkChangeManager.NetworkChangeListener {
        override fun onNetworkChange(sPreNetType: Int, curNetType: Int, intent: Intent?) {
            if (sPreNetType != curNetType) {
                ZixieContext.showDebug(
                    "网络切换：从 " + NetworkUtil.getNetworkName(sPreNetType) + " 切换到" + NetworkUtil.getNetworkName(
                        curNetType,
                    ),
                )
            }
            updateContent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_text)
        findViewById<Toolbar>(R.id.common_toolbar).setNavigationOnClickListener { onBackPressed() }
        NetworkChangeManager.addListener(networkChangeListener)
        updateContent()
        WifiManagerWrapper.init(
            this,
            ZixieContext.enableLog(),
            notifyRSSI = true,
            canScanWifi = true,
            canWifiConfiguration = false
        )
    }

    override fun onDestroy() {
        NetworkChangeManager.removeListener(networkChangeListener)
        super.onDestroy()
    }

    private fun updateContent() {
        val builder =
            StringBuffer().append("当前网络信息：(${NetworkChangeManager.getLastNetTypeName()})\n\n")
        var netTYpe = NetworkChangeManager.getRealNetType(this)
        if (NetworkUtil.isWifiNet(netTYpe)) {
            builder.append("\n").append(NetworkUtil.getNetworkName(this)).append(":\n")
                .append("    SSID(")
                .append(NetworkChangeManager.getLastWifiSSID()).append(");\n    BSSID(")
                .append(NetworkChangeManager.getLastWifiBssId())
                .append(");\n    强度(").append(WifiManagerWrapper.getSignalLevel())
                .append(");\n    IP(")
                .append(DtTypeInfo.getDtTypeInfo(this).wifiIp).append(");\n    周边数量(")
                .append(WifiManagerWrapper.getScanResultList().size).append(");\n")
        }

        if (NetworkUtil.isMobileNet(netTYpe)) {
            builder.append("\n").append(NetworkUtil.getNetworkName(this)).append(":\n")
                .append("    CellInfo(")
                .append(MobileUtil.getPhoneCellInfo(this)).append(");\n    运营商(")
                .append(DeviceInfoManager.getInstance().operatorName).append(");\n    强度(")
                .append(MobileUtil.getSignalLevel()).append(");\n    IP(")
                .append(DtTypeInfo.getDtTypeInfo(this).mobileIp).append(")")
        }
        runOnUiThread { findViewById<TextView>(R.id.result).text = builder.toString() }
    }
}
