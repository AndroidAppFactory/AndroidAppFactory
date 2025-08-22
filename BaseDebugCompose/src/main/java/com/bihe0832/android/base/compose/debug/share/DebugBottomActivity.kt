package com.bihe0832.android.base.compose.debug.share

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.common.share.ShareBaseActivity
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.loadCenterInsideImage

class DebugBottomActivity : ShareBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSuperView()
        findViewById<ImageView>(R.id.shareImagePreview).loadCenterInsideImage("https://cdn.bihe0832.com/images/cv_v.png")
    }

    override fun onShareCancelClick() {
        finish()
    }

    override fun onShareToQQSessionBtnClick() {
        QrcodeUtils.openQrScan(this)
    }

    override fun onShareToQZoneBtnClick() {

    }

    override fun onShareToWechatSessionBtnClick() {

    }

    override fun onShareToWechatTimelineBtnClick() {

    }

    override fun showPicPreview(): Boolean {
        return true
    }

    override fun showSavePic(): Boolean {
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZixieActivityRequestCode.QRCODE_SCAN){
            ZLog.d("ffdf:" + data?.getStringExtra(ZixieActivityRequestCode.INTENT_EXTRA_KEY_QR_SCAN))
        }
    }
}

