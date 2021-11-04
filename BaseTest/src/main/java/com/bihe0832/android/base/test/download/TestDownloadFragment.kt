package com.bihe0832.android.base.test.download

import android.support.v4.content.FileProvider
import android.view.View
import com.bihe0832.android.base.test.R
import com.bihe0832.android.common.test.base.BaseTestListFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadUtils
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import java.io.File
import java.util.*

class TestDownloadFragment : BaseTestListFragment() {
    val LOG_TAG = "TestDownloadFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("卸载应用", View.OnClickListener { InstallUtils.uninstallAPP(context, "com.google.android.tts") }))
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

        DownloadItem().apply {
            setNotificationVisibility(true)
            downloadTitle = getString(R.string.app_name)
            downloadDesc = "ffsf"
//            downloadURL = "https://cdn.bihe0832.com/app/release/ZPUZZLE_official.apk"
            downloadURL = "https://imtt.dd.qq.com/16891/apk/23C6DAF12A8C041F0937AABFCAE70BF6.apk"
            isForceDownloadNew = false
            setCanDownloadByPart(true)
            downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    showResult("应用下载失败（$errorCode）")
                }

                override fun onComplete(filePath: String, item: DownloadItem) {
                    showResult("startDownloadApk download installApkPath: $filePath")
                    if (type == INSTALL_BY_CUSTOMER) {
                        var photoURI = FileProvider.getUriForFile(context!!, "com.bihe0832.android.test.bihe0832.test", File(filePath))
                        InstallUtils.installAPP(context, photoURI, File(filePath))
                    }

                    if (type == INSTALL_BY_DEFAULT) {
                        InstallUtils.installAPP(context, filePath, "", object : InstallListener {
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

                override fun onProgress(item: DownloadItem) {
                    showResult("${item.finished}/${item.fileLength}")
                }

            }
        }.let {
            DownloadUtils.startDownload(context, it, it.isForceDownloadNew)
        }

    }

    private fun testInstallOOBByZip() {
        testInstallOOB("/sdcard/Download/jp.co.sumzap.pj0007.zip", "jp.co.sumzap.pj0007")
    }

    private fun testInstallOOBByBigZip() {
        testInstallOOB("/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip", "com.herogame.gplay.lastdayrulessurvival")
    }

    private fun testInstallOOBByFolder() {
        testInstallOOB(ZixieFileProvider.getZixieFilePath(context!!) + "/test/", "jp.co.sumzap.pj0007")
    }

    private fun testInstallBigOOBByFolder() {
        testInstallOOB("/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927", "com.herogame.gplay.lastdayrulessurvival")
    }


    private fun testInstallOOB(filePath: String, packangeName: String) {
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