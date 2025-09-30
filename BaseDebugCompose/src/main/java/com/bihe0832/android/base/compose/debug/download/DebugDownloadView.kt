package com.bihe0832.android.base.compose.debug.download

import android.app.Activity
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.download.wrapper.DownloadRangeUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

@Composable
fun DebugDownloadView() {
    DebugContent {
        DebugItem("下载并计算文件的具体信息") { testDownload(it) }
        DebugItem("测试带进度下载") {
            (it as? Activity)?.let { activity ->
                testDownloadProcess(activity)
            }

        }
        DebugItem("测试批量区间下载") { testDownloadRange(it, 20) }
        DebugItem("测试区间下载") { testDownloadRange(it, 1) }
        DebugItem("暂停所有区间下载") { DownloadRangeUtils.pauseAll(true, true) }
        DebugItem("测试下载队列") { testDownloadList(it) }
        DebugItem("多位置触发下载") { testDownloadMoreThanOnce(it) }

        DebugItem("打开应用安装界面") {
            IntentUtils.startAppSettings(it, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        }
        DebugItem("卸载应用") {
            InstallUtils.uninstallAPP(it, "com.google.android.tts")
        }
        DebugItem("自定义Provider安装") { startDownload(it, INSTALL_BY_CUSTOMER) }
        DebugItem("默认Provider安装") { startDownload(it, INSTALL_BY_DEFAULT) }
        DebugItem("通过ZIP安装OBB") { testInstallOOBByZip(it) }
        DebugItem("通过ZIP安装超大OBB") { testInstallOOBByBigZip(it) }
        DebugItem("通过文件夹安装OBB") { testInstallOOBByFolder(it) }
        DebugItem("通过文件夹安装超大OBB") { testInstallBigOOBByFolder(it) }
        DebugItem("通过ZIP安装Split") { testInstallSplitByGoodZip(it) }
        DebugItem("通过非标准Split格式的ZIP安装Split") { testInstallSplitByBadZip(it) }
        DebugItem("通过文件夹安装Split") { testInstallSplitByFolder(it) }
        DebugItem("测试文件下载及GZIP 解压") { testDownloadGzip(it) }
    }
}


internal fun testDownloadRange(context: Context, num: Int) {
    for (i in 0 until num) {
        val url = URL_YYB_WZ
        val start = 0 * 50000000L
        startDownload(
            context, url, start, MathUtils.getRandNumByLimit(2000, 100000), ""
        )
    }
}


internal fun testInstallOOBByZip(context: Context) {
    testInstallOOB(context, "/sdcard/Download/jp.co.sumzap.pj0007.zip", "jp.co.sumzap.pj0007")
}

internal fun testInstallOOBByBigZip(context: Context) {
    testInstallOOB(
        context,
        "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
        "com.herogame.gplay.lastdayrulessurvival",
    )
}

internal fun testInstallOOBByFolder(context: Context) {
    testInstallOOB(
        context,
        ZixieFileProvider.getZixieTempFolder(context) + "/test/",
        "jp.co.sumzap.pj0007",
    )
}

internal fun testInstallBigOOBByFolder(context: Context) {
    testInstallOOB(
        context,
        "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927",
        "com.herogame.gplay.lastdayrulessurvival",
    )
}


internal fun testInstallSplitByGoodZip(context: Context) {
    val file = AAFFileWrapper.getFileTempFolder() + "com.supercell.brawlstars.zip"
    FileUtils.copyAssetsFileToPath(context, "com.supercell.brawlstars.zip", file)
    testInstallSplit(
        context,
        file,
        "com.supercell.brawlstars",
    )
}

internal fun testInstallSplitByBadZip(context: Context) {
    testInstallSplit(
        context,
        "/sdcard/Download/a3469c6189204495bc0283e909eb94a6_com.riotgames.legendsofruneterratw_113012.zip",
        "com.riotgames.legendsofruneterratw",
    )
}

internal fun testInstallSplitByFolder(context: Context) {
    testInstallSplit(
        context,
        ZixieFileProvider.getZixieTempFolder(context) + "/com.supercell.brawlstars",
        "com.supercell.brawlstars",
    )
}
