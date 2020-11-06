package com.bihe0832.android.test.module.download

import android.app.DownloadManager
import android.os.Bundle
import android.support.v4.content.FileProvider
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.test.R
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem
import java.io.File
import java.util.*

class TestDownloadFragment : BaseTestFragment() {
    val LOG_TAG = "TestDownloadFragment"

    override fun getDataList(): List<TestItem> {
        val items: MutableList<TestItem> = ArrayList()
        items.add(TestItem("自定义Provider安装") { startDownload(INSTALL_BY_CUSTOMER) })
        items.add(TestItem("默认Provider安装") { startDownload(INSTALL_BY_DEFAULT) })

        items.add(TestItem("OOB安装") { testInstallOOB() })
        items.add(TestItem("Split安装") { testInstallSplit() })

        return items
    }

    companion object {
        fun newInstance(): TestDownloadFragment {
            val args = Bundle()
            val fragment = TestDownloadFragment()
            fragment.arguments = args
            return fragment
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

    private fun testInstallOOB() {
        ZLog.d("test")
        InstallUtils.installAPP(context, "/sdcard/Download/jp.co.sumzap.pj0007.zip", "jp.co.sumzap.pj0007", object : InstallListener {
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

    private fun testInstallSplit() {
        ZLog.d("test")
        InstallUtils.installAPP(context, "/sdcard/Download/com.supercell.brawlstars.zip", "com.supercell.brawlstars", object : InstallListener {
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