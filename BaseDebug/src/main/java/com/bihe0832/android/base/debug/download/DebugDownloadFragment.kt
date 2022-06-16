package com.bihe0832.android.base.debug.download

import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.request.ZixieRequestHttp
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.DownloadUtils
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.encrypt.GzipUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import java.io.File

class DebugDownloadFragment : BaseDebugListFragment() {
    val LOG_TAG = "DebugDownloadFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                    DebugItemData(
                            "卸载应用",
                            View.OnClickListener {
                                InstallUtils.uninstallAPP(
                                        context,
                                        "com.google.android.tts"
                                )
                            })
            )
            add(
                    DebugItemData(
                            "自定义Provider安装",
                            View.OnClickListener { startDownload(INSTALL_BY_CUSTOMER) })
            )
            add(
                    DebugItemData(
                            "默认Provider安装",
                            View.OnClickListener { startDownload(INSTALL_BY_DEFAULT) })
            )
            add(DebugItemData("通过ZIP安装OBB", View.OnClickListener { testInstallOOBByZip() }))
            add(DebugItemData("通过ZIP安装超大OBB", View.OnClickListener { testInstallOOBByBigZip() }))
            add(DebugItemData("通过文件夹安装OBB", View.OnClickListener { testInstallOOBByFolder() }))
            add(DebugItemData("通过文件夹安装超大OBB", View.OnClickListener { testInstallBigOOBByFolder() }))
            add(DebugItemData("通过ZIP安装Split", View.OnClickListener { testInstallSplitByGoodZip() }))
            add(
                    DebugItemData(
                            "通过非标准Split格式的ZIP安装Split",
                            View.OnClickListener { testInstallSplitByBadZip() })
            )
            add(DebugItemData("通过文件夹安装Split", View.OnClickListener { testInstallSplitByFolder() }))
            add(DebugItemData("测试文件下载及GZIP 解压", View.OnClickListener { testDownloadGzip() }))
            add(DebugItemData("测试带进度下载", View.OnClickListener { testDownloadProcess() }))
        }
    }

    val INSTALL_BY_DEFAULT = 0
    val INSTALL_BY_CUSTOMER = 1

    fun testDownloadProcess() {
        DownloadAPK.startDownloadWithProcess(
                activity!!,
                String.format(ZixieContext.applicationContext!!.getString(com.bihe0832.android.framework.R.string.dialog_apk_updating), "（V.2.2.21)"),
                "这是一个Desc测试",
                "http://dldir1.qq.com/INO/Android/tmga/6.5.0_105785_0614/MNA_V6.5.0_105785_0614_official_legu_20035.apk",
                "2edab141ebf9903a3f8abc4f071699ac",
                "com.tencent.cmocmna",
                canCancel = true, downloadMobile = true,
                listener = object : OnDialogListener {
                    override fun onPositiveClick() {
                    }

                    override fun onNegativeClick() {
                    }

                    override fun onCancel() {
                    }
                })
    }

    fun startDownload(type: Int) {

        DownloadItem().apply {
            setNotificationVisibility(true)
            downloadTitle = getString(R.string.app_name)
            downloadDesc = "ffsf"
            downloadIcon = if (type == INSTALL_BY_CUSTOMER) {
                "https://cdn.bihe0832.com/images/zixie_32.ico"
            } else {
                "https://cdn.bihe0832.com/images/head.jpg"
            }
//            downloadURL = "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk"

            downloadURL = if (type == INSTALL_BY_CUSTOMER) {
                "https://imtt.dd.qq.com/sjy.10001/16891/apk/E2F59135FAE358442D2137E446AB59DE.apk"
            } else {
                "https://imtt.dd.qq.com/sjy.10001/16891/apk/2A5BC6AA4E69DCE13C6D5D3FB820706E.apk"
            }
            isForceDownloadNew = true
            setCanDownloadByPart(true)
            downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    showResult("应用下载失败（$errorCode）")
                }

                override fun onComplete(filePath: String, item: DownloadItem) {
                    showResult("startDownloadApk download installApkPath: $filePath")
                    if (type == INSTALL_BY_CUSTOMER) {
                        var photoURI =
                                ZixieFileProvider.getZixieFileProvider(context!!, File(filePath))
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
        testInstallOOB(
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
                "com.herogame.gplay.lastdayrulessurvival"
        )
    }

    private fun testInstallOOBByFolder() {
        testInstallOOB(
                ZixieFileProvider.getZixieFilePath(context!!) + "/test/",
                "jp.co.sumzap.pj0007"
        )
    }

    private fun testInstallBigOOBByFolder() {
        testInstallOOB(
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927",
                "com.herogame.gplay.lastdayrulessurvival"
        )
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
        testInstallSplit(
                "/sdcard/Download/com.supercell.brawlstars.zip",
                "com.supercell.brawlstars"
        )
    }

    private fun testInstallSplitByBadZip() {
        testInstallSplit(
                "/sdcard/Download/a3469c6189204495bc0283e909eb94a6_com.riotgames.legendsofruneterratw_113012.zip",
                "com.riotgames.legendsofruneterratw"
        )
    }

    private fun testInstallSplitByFolder() {
        testInstallSplit(
                ZixieFileProvider.getZixieFilePath(context!!) + "/com.supercell.brawlstars",
                "com.supercell.brawlstars"
        )
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


    private fun testDownloadGzip() {
        DownloadFile.startDownload(context!!,
                "http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip",
                object : SimpleDownloadListener() {
                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    }

                    override fun onComplete(filePath: String, item: DownloadItem) {
                        ZLog.d(LOG_TAG, "MD5 $filePath:" + MD5.getFileMD5(filePath))

                        ZLog.d(LOG_TAG, FileUtils.getFileContent(filePath, true))
                    }


                    override fun onProgress(item: DownloadItem) {
                    }

                })


        ZixieRequestHttp.getOrigin("http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip")
                .let {
                    val filePath =
                            "/storage/emulated/0/Android/data/com.bihe0832.android.test/files/zixie/new_${System.currentTimeMillis()}.json.gzip"

                    FileUtils.writeToFile(filePath, it, false)
                    ZLog.d(LOG_TAG, "MD5 $filePath:" + MD5.getFileMD5(filePath))
                    ZLog.d(LOG_TAG, "hhh 1" + GzipUtils.decompress(it))
                    ZLog.d(LOG_TAG, "hhh 2" + FileUtils.getFileContent(filePath, true))

                }


        ZixieRequestHttp.get("https://cdn.bihe0832.com/app/update/get_apk.json").let {
            ZLog.d(LOG_TAG, "result 1 :$it")
        }


    }
}