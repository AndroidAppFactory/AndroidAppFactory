package com.bihe0832.android.base.compose.debug.intent

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.compose.debug.lock.DebugLockService
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

@Composable
fun DebugIntentComposeView() {
    DebugContent {
        val activity = LocalContext.current as? Activity
        DebugItem("打开指定schema") { openSchema(it) }
        DebugItem("弹出隐私弹框页面") {
            AgreementPrivacy.showPrivacy(activity!!) {}
        }
        DebugItem("默认关于页") { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) }
        DebugItem("打开指定应用设置") {
            IntentUtils.startAppSettings(
                it.applicationContext,
                "com.bihe0832.android.app.test",
                "",
                true,
            )
        }

        DebugItem("打开应用安装界面") {
            IntentUtils.startAppSettings(it, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        }

        DebugItem("打开反馈页面") {
            RouterAction.openFinalURL(getFeedBackURL())
        }

        DebugItem("启动锁屏页面") {
            DebugLockService.startLockServiceWithPermission(it)
        }

        DebugItem("启动Service") {
            val intent = Intent()
            intent.setComponent(
                ComponentName(
                    it.applicationContext,
                    "com.bihe0832.android.base.debug.lock.DebugLockService",
                ),
            )
            it.applicationContext.startService(intent)
        }

        DebugItem("弹出评分页面") {
            UserPraiseManager.showUserPraiseDialog(activity!!, getFeedBackURL())
        }
    }
}

private fun openSchema(context: Context) {
    DialogUtils.showInputDialog(
        context,
        "Schma调试",
        "zapk://about",
        object : DialogCompletedStringCallback {
            override fun onResult(p0: String?) {
                IntentUtils.jumpToOtherApp(context, p0)
            }
        },
    )
}

private fun getFeedBackURL(): String {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] =
        URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
    return RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK, map)
}