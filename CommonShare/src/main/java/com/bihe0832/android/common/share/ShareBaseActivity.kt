package com.bihe0832.android.common.share

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.framework.ui.BaseBottomActivity

/**
 * 不同分享的公共代码，分享的Activity的基类，提供基础的UI样式
 *
 *
 * 主题使用 AAF.ActivityTheme.Bottom
 */
abstract class ShareBaseActivity : BaseBottomActivity() {
    /**
     * 布局layout
     *
     * @return
     */

    open fun getLayoutID(): Int {
        return R.layout.common_activity_share
    }

    private var onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.BaseRlContainer -> {
                onShareCancelClick()
            }
            R.id.BaseShareToWeChatBtn -> {
                onShareToWechatSessionBtnClick()
            }
            R.id.BaseShareToFriendsBtn -> {
                onShareToWechatTimelineBtnClick()
            }
            R.id.BaseShareToQQBtn -> {
                onShareToQQSessionBtnClick()
            }
            R.id.BaseShareToQzoneBtn -> {
                onShareToQZoneBtnClick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
    }

    protected open fun initSuperView() {
        findViewById<View>(R.id.BaseRlContainer)?.let { container ->
            container.setOnClickListener(onClickListener)
            findViewById<View>(R.id.BaseShareToWeChatBtn)?.setOnClickListener(onClickListener)
            findViewById<View>(R.id.BaseShareToFriendsBtn)?.setOnClickListener(onClickListener)
            findViewById<View>(R.id.BaseShareToQQBtn)?.setOnClickListener(onClickListener)
            findViewById<View>(R.id.BaseShareToQzoneBtn)?.setOnClickListener(onClickListener)
            if (showShareLink()) {
                findViewById<View>(R.id.BaseShareLinkBtn)?.visibility = View.VISIBLE
            }
            if (showSavePic()) {
                findViewById<View>(R.id.BaseShareDownloadBtn)?.apply {
                    visibility = View.VISIBLE
                }
            }
            if (showPicPreview()) {
                findViewById<View>(R.id.shareImagePreview)?.visibility = View.VISIBLE
            }
        }
    }

    protected open fun showShareLink(): Boolean {
        return true
    }

    protected open fun showPicPreview(): Boolean {
        return false
    }

    protected open fun showSavePic(): Boolean {
        return false
    }

    override fun onBack() {
        onShareCancelClick()
    }

    protected abstract fun onShareCancelClick()
    protected abstract fun onShareToQQSessionBtnClick()
    protected abstract fun onShareToQZoneBtnClick()
    protected abstract fun onShareToWechatSessionBtnClick()
    protected abstract fun onShareToWechatTimelineBtnClick()

}