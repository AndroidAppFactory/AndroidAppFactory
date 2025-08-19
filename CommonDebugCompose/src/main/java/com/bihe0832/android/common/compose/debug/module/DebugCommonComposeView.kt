package com.bihe0832.android.common.compose.debug.module

import android.provider.Settings
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.DebugComposeItemManager
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.showAPPInfo
import com.bihe0832.android.common.compose.debug.item.showOtherAPPInfo
import com.bihe0832.android.common.compose.debug.item.showUsedInfo
import com.bihe0832.android.common.compose.debug.module.device.GetDeviceInfoView
import com.bihe0832.android.common.compose.debug.module.device.getMobileInfo
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

@Composable
fun DebugCommonComposeView(showLog: () -> Unit) {
    DebugContent {
        DebugTips("APPFactory的通用组件和工具")
        DebugItem("查看应用版本及环境") { context -> showAPPInfo(context) }
        DebugItem("查看使用情况") { context -> showUsedInfo(context) }
        DebugItem("查看设备概要信息") { context ->
            DebugUtilsV2.showInfoWithHTML(context, "设备概要信息", getMobileInfo(context))
        }
        DebugItem("查看设备详细信息") { context ->
            DebugComposeItemManager.register("GetDeviceInfoView") { GetDeviceInfoView() }
            DebugUtilsV2.startComposeActivity(
                ZixieContext.applicationContext!!,
                "设备详细信息",
                "GetDeviceInfoView"
            )
        }
        DebugItem("查看第三方应用信息") { context -> showOtherAPPInfo(context) }
        DebugItem("<font color ='#3AC8EF'><b>日志管理</b></font>") { context -> showLog() }
        DebugItem("打开开发者模式") { context ->
            IntentUtils.startSettings(
                context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        }
        DebugItem("打开应用设置") { context -> IntentUtils.startAppDetailSettings(context) }
        DebugItem("清除缓存") { context ->
            AAFFileWrapper.clear()
            FileUtils.deleteDirectory(context.cacheDir)
            ZixieContext.restartApp()
        }
        DebugItem("清除用户信息授权") {
            AgreementPrivacy.resetPrivacy()
            ZixieContext.restartApp()
        }
    }
}