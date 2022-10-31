package com.bihe0832.android.base.debug.empty

import android.os.Bundle
import android.widget.ImageView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.share.ShareBaseActivity
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
}

