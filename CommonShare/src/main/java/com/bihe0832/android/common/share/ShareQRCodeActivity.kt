package com.bihe0832.android.common.share

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.ui.dialog.senddata.SendTextUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.qrcode.QRCodeEncodingHandler
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import java.net.URLDecoder

/**
 * 不同分享的公共代码，分享的Activity的基类，提供基础的UI样式
 *
 *
 * 主题使用 AAF.ActivityTheme.Bottom
 */
@Module(RouterConstants.MODULE_NAME_SHARE_QRCODE)
open class ShareQRCodeActivity : ShareBaseActivity() {

    private var mShareData = ""
    private var mShareTitle = ""
    private var mShareDesc = ""

    private fun parseShareString(intent: Intent, key: String): String? {
        try {
            if (intent.hasExtra(key)) {
                return URLDecoder.decode(intent.getStringExtra(key))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun getLayoutID(): Int {
        return R.layout.common_activity_share_qrcode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mShareData = parseShareString(intent, RouterConstants.INTENT_EXTRA_KEY_SHARE_DATA_WITH_ENCODE)
            ?: getShareData()
        mShareTitle = parseShareString(intent, RouterConstants.INTENT_EXTRA_KEY_SHARE_TITLE_WITH_ENCODE)
            ?: getShareTitle()
        mShareDesc = parseShareString(intent, RouterConstants.INTENT_EXTRA_KEY_SHARE_DESC_WITH_ENCODE)
            ?: getShareDesc()
        if (TextUtils.isEmpty(mShareData)) {
            finish()
        }

        ThreadManager.getInstance().start {
            val log = BitmapUtil.getLocalBitmap(ZixieContext.applicationContext, R.mipmap.icon, 1)
            QRCodeEncodingHandler.createQRCode(
                mShareData,
                DisplayUtil.dip2px(this, 200f),
                DisplayUtil.dip2px(this, 200f),
                log,
            )?.let {
                findViewById<ImageView>(R.id.share_qrcode_icon)?.apply {
                    post { setImageBitmap(it) }
                }
            }
        }

        findViewById<View>(R.id.share_qrcode_container).setOnClickListener {
            onBack()
        }

        findViewById<TextView>(R.id.share_qrcode_title).text = mShareTitle

        findViewById<TextView>(R.id.share_qrcode_desc).text = mShareDesc

        findViewById<View>(R.id.share_qrcode_panel_send).setOnClickListener {
            ThreadManager.getInstance().start {
                BitmapUtil.getViewBitmap(findViewById(R.id.share_qrcode_desc_layout)).let {
                    ThreadManager.getInstance().runOnUIThread {
                        FileUtils.sendFile(this, getShareDialogTitle(), it, FileMimeTypes.getMimeType(it))
                    }
                }
            }
        }

        findViewById<View>(R.id.share_qrcode_panel_download).setOnClickListener {
            ThreadManager.getInstance().start {
                BitmapUtil.getViewBitmap(findViewById(R.id.share_qrcode_desc_layout)).let {
                    ThreadManager.getInstance().runOnUIThread {
                        Media.addToPhotos(this, it)
                        ZixieContext.showToast("二维码已添加到相册")
                    }
                }
            }
        }

        getShareLink()?.let { linkText ->
            findViewById<View>(R.id.share_qrcode_panel_link).apply {
                if (TextUtils.isEmpty(linkText)) {
                    findViewById<View>(R.id.share_qrcode_panel_link).visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        SendTextUtils.sendInfo(context, getShareDialogTitle(), linkText)
                    }
                }
            }
        }
    }

    // 二维码数据
    open fun getShareData(): String {
        return ""
    }

    // 图片主文字
    open fun getShareTitle(): String {
        return ThemeResourcesManager.getString(R.string.app_name)!!
    }

    // 图片副文字
    open fun getShareDesc(): String {
        return ThemeResourcesManager.getString(R.string.com_bihe0832_share_desc_qrcode)!!
    }

    // 文本分享的内容
    open fun getShareLink(): String {
        return String.format(ThemeResourcesManager.getString(R.string.com_bihe0832_share_link)!!, mShareData)
    }

    open fun getShareDialogTitle(): String {
        return String.format(ThemeResourcesManager.getString(R.string.com_bihe0832_share_dialog_title)!!, "")
    }

    override fun onShareCancelClick() {
        finish()
    }

    override fun onShareToQQSessionBtnClick() {}
    override fun onShareToQZoneBtnClick() {}
    override fun onShareToWechatSessionBtnClick() {}
    override fun onShareToWechatTimelineBtnClick() {}
}
