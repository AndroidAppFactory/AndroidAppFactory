package com.bihe0832.android.base.test.network

import android.os.Bundle
import com.bihe0832.android.base.test.R
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.WifiManagerWrapper
import kotlinx.android.synthetic.main.activity_test_text.*

class TestNetworkActivity : BaseActivity() {

    private val networkChangeListener = NetworkChangeManager.NetworkChangeListener { sPreNetType, curNetType, intent ->
        if (sPreNetType != curNetType) {
            ZixieContext.showToast("网络切换：从 " + NetworkUtil.getNetworkName(sPreNetType) + " 切换到" + NetworkUtil.getNetworkName(curNetType))
        }
        updateContent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_text)
        common_toolbar.setNavigationOnClickListener { onBackPressedSupport() }
        NetworkChangeManager.getInstance().addListener(networkChangeListener)
        updateContent()
    }

    override fun onDestroy() {
        NetworkChangeManager.getInstance().removeListener(networkChangeListener)
        super.onDestroy()
    }

    private fun updateContent() {
        val builder = StringBuffer().append("当前网络信息：(${NetworkChangeManager.getInstance().networkName})\n\n")
        if (NetworkUtil.getNetworkState(this) == NetworkUtil.NETWORK_CLASS_WIFI) {
            builder.append("\n").append(NetworkUtil.getNetworkName(this)).append(":\n")
                    .append("    SSID(").append(WifiManagerWrapper.getSSID())
                    .append(");\n    BSSID(").append(WifiManagerWrapper.getBSSID())
                    .append(");\n    强度(").append(WifiManagerWrapper.getSignalLevel())
                    .append(");\n    IP(").append(NetworkUtil.getDtTypeInfo(this).wifiIp).append(")\n")
        }

        if(NetworkUtil.getNetworkState(this) == NetworkUtil.NETWORK_CLASS_2_G
                || NetworkUtil.getNetworkState(this) == NetworkUtil.NETWORK_CLASS_3_G
                || NetworkUtil.getNetworkState(this) == NetworkUtil.NETWORK_CLASS_4_G
                || NetworkUtil.getNetworkState(this) == NetworkUtil.NETWORK_CLASS_5_G
        ){
            builder.append("\n").append(NetworkUtil.getNetworkName(this)).append(":\n")
                    .append("    CellInfo(").append(MobileUtil.getPhoneCellInfo(this))
                    .append(");\n    运营商(").append(DeviceInfoManager.getInstance().getMobileOperatorType())
                    .append(");\n    强度(").append(MobileUtil.getSignalLevel())
                    .append(");\n    IP(").append(NetworkUtil.getDtTypeInfo(this).mobileIp).append(")")
        }
        runOnUiThread { result.text = builder.toString() }
    }
}
