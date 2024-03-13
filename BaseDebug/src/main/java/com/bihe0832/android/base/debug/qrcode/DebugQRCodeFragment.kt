package com.bihe0832.android.base.debug.qrcode

import android.app.Activity
import android.content.Intent
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
import com.bihe0832.android.framework.router.shareByQrcode
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.DisplayUtil

class DebugQRCodeFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_qrcode
    }

    override fun initView(view: View) {

        resetQRcodeData()
        view.findViewById<ImageView>(R.id.qrcode_image).setOnLongClickListener { v ->
            Media.addToPhotos(context!!, BitmapUtil.getViewBitmap(v))
            ZixieContext.showToast("已经添加到相册")
            true
        }
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

        view.findViewById<View>(R.id.qrcode_again).setOnClickListener {
            resetQRcodeData()
        }

        view.findViewById<View>(R.id.qrcode_scan).setOnClickListener {
            QrcodeUtils.openQrScan(activity, true, true)
        }

        view.findViewById<View>(R.id.qrcode_scan_parse).setOnClickListener {
            QrcodeUtils.openQrScanAndParse(true, true)
        }

        view.findViewById<View>(R.id.qrcode_scan_selected).setOnClickListener {
            QrcodeUtils.openQrScanAndParse(true, true)
        }



        view.findViewById<View>(R.id.qrcode_share).setOnClickListener {
            shareByQrcode(
                view.findViewById<EditText>(R.id.qrcode_input).text.toString(),
//                    "我发现了一个好玩的#—#",
//                    "我发现了一个好玩的#—#，快扫码一起体验一下吧"
            )
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
                                ?: ""
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ZixieActivityRequestCode.QRCODE_SCAN -> {
                    data?.getStringExtra(ZixieActivityRequestCode.INTENT_EXTRA_KEY_QR_SCAN)?.let {
                        view?.findViewById<EditText>(R.id.qrcode_input)?.setText(it)
                    }
                }
            }
        }
    }
}
