package com.bihe0832.android.common.qrcode

import android.content.Intent
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.google.zxing.Result

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN_AND_PARSE)
class QrcodeWithResultActivity : QrcodeScanActivity() {

    override fun handleDecode(result: Result?) {
        if (null != result && !TextUtils.isEmpty(result.text)) {
            handleQrcodeResult(result.text)
        } else {
            handleQrcodeResult("")
        }
    }

    open fun handleQrcodeResult(scanResult: String) {
        if (TextUtils.isEmpty(scanResult)) {
            ZixieContext.showToast("扫码失败，" + ThemeResourcesManager.getString(R.string.app_name) + "无法识别该二维码")
        } else {
            Intent.parseUri(scanResult, Intent.URI_INTENT_SCHEME).let {
                if (isSelfIntent(it)) {
                    playBeepSoundAndVibrate()
                    RouterAction.openFinalURL(scanResult)
                } else {
                    if (!IntentUtils.startIntent(this, it)) {
                        ZixieContext.showToast("扫码失败，" + ThemeResourcesManager.getString(R.string.app_name) + "无法解析该二维码")
                    } else {
                        playBeepSoundAndVibrate()
                    }
                }
            }
        }
        finish()
    }

    open fun isSelfIntent(intent: Intent): Boolean {
        return intent.resolveActivity(packageManager)?.packageName.equals(packageName)
    }
}