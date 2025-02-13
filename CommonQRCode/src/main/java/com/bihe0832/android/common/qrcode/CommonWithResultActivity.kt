package com.bihe0832.android.common.qrcode

import android.content.Intent
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.google.zxing.Result

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN_AND_PARSE)
open class CommonWithResultActivity : CommonScanActivity() {

    override fun handleDecode(result: Result?) {
        if (null != result && !TextUtils.isEmpty(result.text)) {
            handleQrcodeResult(result.text)
        } else {
            handleQrcodeResult("")
        }
    }

    open fun handleQrcodeResult(scanResult: String) {
        ZLog.d("Qrcode", scanResult)
        if (TextUtils.isEmpty(scanResult)) {
            ZixieContext.showToast(
                String.format(
                    ThemeResourcesManager.getString(R.string.common_scan_failed) ?: "",
                    ThemeResourcesManager.getString(R.string.app_name)
                )
            )
        } else {
            try {
                Intent.parseUri(scanResult, Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    setComponent(null)
                    setSelector(null)
                }.let {
                    if (isSelfIntent(it)) {
                        playBeepSoundAndVibrate()
                        RouterAction.openFinalURL(scanResult)
                    } else {
                        if (!IntentUtils.startIntent(this, it)) {
                            ZixieContext.showToast(
                                String.format(
                                    ThemeResourcesManager.getString(R.string.common_scan_failed)
                                        ?: "",
                                    ThemeResourcesManager.getString(R.string.app_name)
                                )
                            )
                        } else {
                            playBeepSoundAndVibrate()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        finish()
    }

    open fun isSelfIntent(intent: Intent): Boolean {
        return intent.resolveActivity(packageManager)?.packageName.equals(packageName)
    }
}
