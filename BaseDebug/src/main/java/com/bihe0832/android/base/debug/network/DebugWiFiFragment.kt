package com.bihe0832.android.base.debug.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil

class DebugWiFiFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_wifi_dric
    }

    override fun initView(view: View) {
        resetQRcodeData()
        view.findViewById<EditText>(R.id.qrcode_input).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resetQRcodeData()
            }

            override fun afterTextChanged(s: Editable?) {
                resetQRcodeData()
            }
        })

        view.findViewById<View>(R.id.qrcode_again).setOnClickListener {
            resetQRcodeData()
        }

        view.findViewById<View>(R.id.qrcode_scan).setOnClickListener {
            QrcodeUtils.openQrScan(this)
        }

        view.findViewById<View>(R.id.wifi_dric).setOnClickListener {
        }
    }

    private fun resetQRcodeData() {
        view?.let { rootview ->
            val text = rootview.findViewById<EditText>(R.id.qrcode_input).text.toString()
            if (TextUtils.isEmpty(text)) {
                rootview.findViewById<TextView>(R.id.qrcode_result).text = "源数据为空"
                rootview.findViewById<ImageView>(R.id.qrcode_image).setImageBitmap(null)
            } else {
                ThreadManager.getInstance().start {
                    QrcodeUtils.createQRCode(text, DisplayUtil.dip2px(rootview.context, 400f)).let {
                        rootview.findViewById<ImageView>(R.id.qrcode_image).setImageBitmap(it)
                        Thread.sleep(500L)
                        updateText()
                    }
                }
            }
        }
    }

    fun updateText() {
        view?.let { rootview ->
            ThreadManager.getInstance().start {
                BitmapUtil.getViewBitmap(rootview.findViewById<ImageView>(R.id.qrcode_image)).let {
                    val data = QrcodeUtils.decodeQRcode(it)
                    ThreadManager.getInstance().runOnUIThread {
                        rootview.findViewById<TextView>(R.id.qrcode_result).text =
                            "源数据为：\n${rootview.findViewById<EditText>(R.id.qrcode_input).text} \n二维码解析后的数据为：\n" + data?.text
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ZixieActivityRequestCode.QRCODE_SCAN -> {
                    data?.getStringExtra(ZixieActivityRequestCode.INTENT_EXTRA_KEY_QR_SCAN)?.let { qrString ->
                        view?.findViewById<EditText>(R.id.qrcode_input)?.setText(qrString)
                        val qrScanResultArray =
                            qrString.split(";".toRegex()).dropLastWhile { it.isEmpty() }?.toTypedArray()
                        val wifiName = ConvertUtils.getSafeValueFromArray(qrScanResultArray, 1, "")
                        val wifiPassword = ConvertUtils.getSafeValueFromArray(qrScanResultArray, 2, "")
                        ZLog.d("扫描结果:wifiName$wifiName,wifiPassword:$wifiPassword")
                        if (!TextUtils.isEmpty(wifiName) && !TextUtils.isEmpty(wifiPassword)) {
                            context?.let {
                                connectToWifi(
                                    it,
                                    wifiName,
                                    wifiPassword,
                                    object : AAFDataCallback<Boolean>() {
                                        override fun onSuccess(result: Boolean?) {
                                            ZixieContext.showToast("success:$result")
                                        }

                                        override fun onError(errorCode: Int, msg: String) {
                                            ZixieContext.showToast("onError:$errorCode")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun connectToWifi(
        context: Context,
        ssid: String,
        password: String,
        connectCallback: AAFDataCallback<Boolean>? = null,
    ) {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    ZixieContext.showToast("已连接到 Wi-Fi 网络")
                    connectCallback?.onSuccess(true)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    ZixieContext.showToast("无法连接到 Wi-Fi 网络")
                    // 您可以在此处处理 Wi-Fi 连接失败事件
                    connectCallback?.onSuccess(false)
                }
            }
            WifiManagerWrapper.connectWifiAboveQ(context, ssid, password, networkCallback, false)
        } else {
            val isConnected = WifiManagerWrapper.connectWifi(context, ssid, password)
            if (isConnected) {
                connectCallback?.onSuccess(true)
            } else {
                ZixieContext.showToast("无法连接到 Wi-Fi 网络")
                connectCallback?.onSuccess(false)
            }
        }
    }
}
