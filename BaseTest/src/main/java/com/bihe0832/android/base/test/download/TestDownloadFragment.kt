package com.bihe0832.android.base.test.download

import android.app.DownloadManager
import android.support.v4.content.FileProvider
import android.view.View
import com.bihe0832.android.base.test.R
import com.bihe0832.android.common.test.base.BaseTestFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.file.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import java.io.File
import java.util.*

class TestDownloadFragment : BaseTestFragment() {
    val LOG_TAG = "TestDownloadFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("卸载应用", View.OnClickListener { InstallUtils.uninstallAPP(context,"com.google.android.tts")}))
            add(TestItemData("自定义Provider安装", View.OnClickListener { startDownload(INSTALL_BY_CUSTOMER) }))
            add(TestItemData("默认Provider安装", View.OnClickListener { startDownload(INSTALL_BY_DEFAULT) }))
            add(TestItemData("通过ZIP安装OBB", View.OnClickListener { testInstallOOBByZip() }))
            add(TestItemData("通过ZIP安装超大OBB", View.OnClickListener { testInstallOOBByBigZip() }))
            add(TestItemData("通过文件夹安装OBB", View.OnClickListener { testInstallOOBByFolder() }))
            add(TestItemData("通过文件夹安装超大OBB", View.OnClickListener { testInstallBigOOBByFolder() }))
            add(TestItemData("通过ZIP安装Split", View.OnClickListener { testInstallSplitByGoodZip() }))
            add(TestItemData("通过非标准Split格式的ZIP安装Split", View.OnClickListener { testInstallSplitByBadZip() }))
            add(TestItemData("通过文件夹安装Split", View.OnClickListener { testInstallSplitByFolder() }))
        }
    }

    val INSTALL_BY_DEFAULT = 0
    val INSTALL_BY_CUSTOMER = 1

    fun startDownload(type: Int) {
        val item = DownloadItem()
        item.notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
        item.dowmloadTitle = this.getString(R.string.app_name)
        item.downloadDesc = "ffsf"
        item.fileName = "MobileAssistant_1.apk"
        item.downloadURL = "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk"
        item.forceDownloadNew = true
        DownloadUtils.startDownload(context!!, item, object : DownloadListener {
            override fun onProgress(total: Long, cur: Long) {
                showResult("$cur/$total")
            }

            override fun onSuccess(finalFileName: String) {
//                    var finalFileName: String = applicationContext!!.getExternalFilesDir("zixie/").absolutePath + "/" + "MobileAssistant_1.apk"
                showResult("startDownloadApk download installApkPath: $finalFileName")
                if (type == INSTALL_BY_CUSTOMER) {
                    var photoURI = FileProvider.getUriForFile(context!!, "com.bihe0832.android.test.bihe0832.test", File(finalFileName))
                    InstallUtils.installAPP(context, photoURI, File(finalFileName))
                }

                if (type == INSTALL_BY_DEFAULT) {
                    InstallUtils.installAPP(context, finalFileName, "", object : InstallListener {
                        override fun onUnCompress() {
                            ZLog.d(LOG_TAG, "onUnCompress")
                        }

                        override fun onInstallPrepare() {
                            ZLog.d(LOG_TAG, "onInstallPrepare")
                        }

                        override fun onInstallStart() {
                            ZLog.d(LOG_TAG, "onInstallStart")
                        }

                        override fun onInstallFailed(errorCode: Int) {
                            ZLog.d(LOG_TAG, "onInstallFailed $errorCode")
                        }


                    })
                }
            }

            override fun onError(error: Int, errmsg: String) {
                showResult("应用下载失败（$error）")
            }
        })
    }

    private fun testInstallOOBByZip() {
        testInstallOOB("/sdcard/Download/jp.co.sumzap.pj0007.zip","jp.co.sumzap.pj0007")
    }

    private fun testInstallOOBByBigZip() {
        testInstallOOB("/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip","com.herogame.gplay.lastdayrulessurvival")
    }

    private fun testInstallOOBByFolder() {
        testInstallOOB(ZixieFileProvider.getZixieFilePath(context!!) + "/test/","jp.co.sumzap.pj0007")
    }

    private fun testInstallBigOOBByFolder() {
        testInstallOOB("/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927","com.herogame.gplay.lastdayrulessurvival")
    }


    private fun testInstallOOB(filePath: String,packangeName: String) {
        ZLog.d("testInstallOOB")
        InstallUtils.installAPP(context, filePath, packangeName, object : InstallListener {
            override fun onUnCompress() {
                ZLog.d(LOG_TAG, "onUnCompress")
            }

            override fun onInstallPrepare() {
                ZLog.d(LOG_TAG, "onInstallPrepare")
            }

            override fun onInstallStart() {
                ZLog.d(LOG_TAG, "onInstallStart")
            }

            override fun onInstallFailed(errorCode: Int) {
                ZLog.d(LOG_TAG, "onInstallFailed $errorCode")
            }

        })
    }

    private fun testInstallSplitByGoodZip() {
        testInstallSplit("/sdcard/Download/com.supercell.brawlstars.zip", "com.supercell.brawlstars")
    }

    private fun testInstallSplitByBadZip() {
        testInstallSplit("/sdcard/Download/a3469c6189204495bc0283e909eb94a6_com.riotgames.legendsofruneterratw_113012.zip", "com.riotgames.legendsofruneterratw")
    }

    private fun testInstallSplitByFolder() {
        testInstallSplit(ZixieFileProvider.getZixieFilePath(context!!) + "/com.supercell.brawlstars", "com.supercell.brawlstars")
    }

    private fun testInstallSplit(filePath: String, packangeName: String) {
        ZLog.d("test")
        InstallUtils.installAPP(context, filePath, packangeName, object : InstallListener {
            override fun onUnCompress() {
                ZLog.d(LOG_TAG, "onUnCompress")
            }

            override fun onInstallPrepare() {
                ZLog.d(LOG_TAG, "onInstallPrepare")
            }

            override fun onInstallStart() {
                ZLog.d(LOG_TAG, "onInstallStart")
            }

            override fun onInstallFailed(errorCode: Int) {
                ZLog.d(LOG_TAG, "onInstallFailed $errorCode")
            }

        })
    }
}