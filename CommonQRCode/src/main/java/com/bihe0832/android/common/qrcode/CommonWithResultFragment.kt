package com.bihe0832.android.common.qrcode

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.model.res.R as ModelResR
import com.google.zxing.Result

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/28.
 * Description: Description
 *
 */
class CommonWithResultFragment : CommonScanFragment() {
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
                    ThemeResourcesManager.getString(ModelResR.string.common_scan_failed) ?: "",
                    ThemeResourcesManager.getString(ResR.string.app_name)
                )
            )
        } else {
            try {
                Intent.parseUri(scanResult, Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    setComponent(null)
                    setSelector(null)
                }.let {
                    if (isSelfIntent(context!!, it)) {
                        playBeepSoundAndVibrate()
                        RouterAction.openFinalURL(scanResult)
                    } else {
                        if (!IntentUtils.startIntent(context!!, it)) {
                            ZixieContext.showToast(
                                String.format(
                                    ThemeResourcesManager.getString(ModelResR.string.common_scan_failed)
                                        ?: "",
                                    ThemeResourcesManager.getString(ResR.string.app_name)
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
        activity?.finish()
    }

    open fun isSelfIntent(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val packageName = context.packageName
        return intent.resolveActivity(packageManager)?.packageName.equals(packageName)
    }
}