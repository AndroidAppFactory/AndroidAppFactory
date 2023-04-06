package com.bihe0832.android.common.share

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.google.zxing.encoding.EncodingHandler
import java.io.File


/**
 * 不同分享的公共代码，分享的Activity的基类，提供基础的UI样式
 *
 *
 * 主题使用 AAF.ActivityTheme.Bottom
 */
@Module(RouterConstants.MODULE_NAME_SHARE_APK)
class ShareAPPActivity : ShareBaseActivity() {

    override fun getLayoutID(): Int {
        return R.layout.common_activity_share_apk
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThreadManager.getInstance().start {
            EncodingHandler.createQRCode(getString(R.string.com_bihe0832_share_target), DisplayUtil.dip2px(this, 200f), DisplayUtil.dip2px(this, 200f), BitmapUtil.getLocalBitmap(this, R.id.icon, 1))?.let {
                findViewById<ImageView>(R.id.share_apk_icon)?.apply {
                    setImageBitmap(it)
                }
            }
        }

        findViewById<View>(R.id.share_apk_container).setOnClickListener {
            onBack()
        }

        findViewById<View>(R.id.share_apk_panel_send).setOnClickListener {
            ThreadManager.getInstance().start {
                BitmapUtil.getViewBitmap(findViewById(R.id.share_apk_desc_layout)).let {
                    ThreadManager.getInstance().runOnUIThread {
                        FileUtils.sendFile(this, getShareTitle(), it, FileMimeTypes.getMimeType(it))
                    }
                }
            }
        }

        findViewById<View>(R.id.share_apk_panel_send_source).setOnClickListener {
            APKUtils.getAPKPath(this, packageName).let {
                if (FileUtils.checkFileExist(it)) {
                    if (File(it).length() > FileUtils.SPACE_MB * 100) {
                        ZixieContext.showToast(getString(R.string.com_bihe0832_share_app_big))
                    }
                    AAFFileTools.sendFile(it)
                } else {
                    ZixieContext.showToast(getString(R.string.com_bihe0832_share_app_faild))
                }
            }
        }

        findViewById<View>(R.id.share_apk_panel_download).setOnClickListener {
            ThreadManager.getInstance().start {
                BitmapUtil.getViewBitmap(findViewById(R.id.share_apk_desc_layout)).let {
                    ThreadManager.getInstance().runOnUIThread {
                        Media.addPicToPhotos(this, it)
                        ZixieContext.showToast("二维码已添加到相册")
                    }
                }
            }
        }

        findViewById<View>(R.id.share_apk_panel_link).setOnClickListener {
            String.format(getString(R.string.com_bihe0832_share_app), getString(R.string.app_name), getString(R.string.com_bihe0832_share_target)).let {
                DebugTools.sendInfo(this, getShareTitle(), it)
            }
        }
    }

    fun getShareTitle(): String {
        return String.format(getString(R.string.com_bihe0832_share_app_title), getString(R.string.app_name))
    }

    override fun onShareCancelClick() {
        finish()
    }

    override fun onShareToQQSessionBtnClick() {}
    override fun onShareToQZoneBtnClick() {}
    override fun onShareToWechatSessionBtnClick() {}
    override fun onShareToWechatTimelineBtnClick() {}
}