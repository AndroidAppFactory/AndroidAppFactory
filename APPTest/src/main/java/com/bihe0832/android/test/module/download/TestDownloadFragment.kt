package com.bihe0832.android.test.module.download

import android.app.DownloadManager
import android.os.Bundle
import android.support.v4.content.FileProvider
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.test.R
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem
import java.io.File
import java.util.*

class TestDownloadFragment : BaseTestFragment() {
    override fun getDataList(): List<TestItem> {
        val items: MutableList<TestItem> = ArrayList()
        items.add(TestItem("自定义Provider安装") { startDownload(INSTALL_BY_CUSTOMER) })
        items.add(TestItem("默认Provider安装") { startDownload(INSTALL_BY_DEFAULT) })
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
                    InstallUtils.installAPP(context, finalFileName)
                }
            }

            override fun onError(error: Int, errmsg: String) {
                showResult("应用下载失败（$error）")
            }
        })
    }
}