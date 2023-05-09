package com.bihe0832.android.common.share

import android.os.Bundle
import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import java.io.File


/**
 * 不同分享的公共代码，分享的Activity的基类，提供基础的UI样式
 *
 *
 * 主题使用 AAF.ActivityTheme.Bottom
 */
@Module(RouterConstants.MODULE_NAME_SHARE_APK)
class ShareAPPActivity : ShareQRCodeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.share_apk_panel_send_source).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                APKUtils.getAPKPath(this.context, packageName).let {
                    if (FileUtils.checkFileExist(it)) {
                        if (File(it).length() > FileUtils.SPACE_MB * 100) {
                            ZixieContext.showToast(ThemeResourcesManager.getString(R.string.com_bihe0832_share_app_big)!!)
                        }
                        AAFFileTools.sendFile(it)
                    } else {
                        ZixieContext.showToast(ThemeResourcesManager.getString(R.string.com_bihe0832_share_app_faild)!!)
                    }
                }
            }
        }
    }

    override fun getShareData(): String {
        return ThemeResourcesManager.getString(R.string.com_bihe0832_share_app_target)!!
    }

    // 文本分享的内容
    override fun getShareLink(): String {
        return String.format(ThemeResourcesManager.getString(R.string.com_bihe0832_share_app)!!, ThemeResourcesManager.getString(R.string.app_name), ThemeResourcesManager.getString(R.string.com_bihe0832_share_app_target))

    }

    override fun getShareDialogTitle(): String {
        return String.format(ThemeResourcesManager.getString(R.string.com_bihe0832_share_dialog_title)!!, ThemeResourcesManager.getString(R.string.app_name))
    }
}