package com.bihe0832.android.base.debug.download

import android.provider.Settings
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.request.ZixieRequestHttp
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.*
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.utils.encrypt.GzipUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.SHA256
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.io.File

class DebugDownloadFragment : BaseDebugListFragment() {

    companion object {
        val LOG_TAG = "DebugDownloadFragment"

        val globalListener = object : SimpleDownloadListener() {
            override fun onProgress(item: DownloadItem) {

            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownload onComplete : ${filePath}")
            }

            override fun onStart(item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownload onStart : ${item}")
            }

        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("下载并计算文件的具体信息", View.OnClickListener { testDownload() }))
            add(DebugItemData("测试带进度下载", View.OnClickListener { testDownloadProcess() }))
            add(DebugItemData("测试下载队列", View.OnClickListener { testDownloadList() }))
            add(DebugItemData("打开应用安装界面", View.OnClickListener {
                IntentUtils.startAppSettings(
                        context,
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                )
            }))
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
            add(DebugItemData("多位置触发下载", View.OnClickListener { testDownloadMoreThanOnce() }))

        }
    }

    val INSTALL_BY_DEFAULT = 0
    val INSTALL_BY_CUSTOMER = 1


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
            downloadURL = "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk"

//            downloadURL = if (type == INSTALL_BY_CUSTOMER) {
//                "https://imtt.dd.qq.com/sjy.10001/16891/apk/E2F59135FAE358442D2137E446AB59DE.apk"
//            } else {
//                "https://imtt.dd.qq.com/sjy.10001/16891/apk/2A5BC6AA4E69DCE13C6D5D3FB820706E.apk"
//            }
            isForceDownloadNew = true
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


    override fun initView(view: View) {
        super.initView(view)
        DownloadTools.addGlobalDownloadListener(globalListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DownloadTools.removeGlobalDownloadListener(globalListener)
    }


    private fun testDownloadGzip() {
        DownloadFile.forceDownload(requireContext(),
                "http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip",
                AAFFileWrapper.getFileCacheFolder() + System.currentTimeMillis() + "_20220321114751.json.gzip",
                false,
                object : SimpleDownloadListener() {
                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                        ZLog.d(LOG_TAG, "onFail:" + msg)

                    }

                    override fun onComplete(filePath: String, item: DownloadItem) {
                        ZLog.d(LOG_TAG, "MD5 $filePath:" + MD5.getFileMD5(filePath))

                        ZLog.d(LOG_TAG, FileUtils.getFileContent(filePath, true))

                        MD5.getFileMD5(filePath).let { data ->
                            ZLog.d(LOG_TAG, "$filePath MD5 is: $data")
                            ZLog.d(LOG_TAG, "$filePath MD5 length is: ${data.length}")
                        }
                        MessageDigestUtils.getFileDigestData(filePath, "MD5").let { data ->
                            ZLog.d(LOG_TAG, "$filePath MD5 is: $data")
                            ZLog.d(LOG_TAG, "$filePath MD5 length is: ${data.length}")
                        }
                        SHA256.getFileSHA256(filePath).let { data ->
                            ZLog.d(LOG_TAG, "$filePath SHA256 is: $data")
                            ZLog.d(LOG_TAG, "$filePath SHA256 length is: ${data.length}")
                        }
                        MessageDigestUtils.getFileDigestData(filePath, "SHA-256").let { data ->
                            ZLog.d(LOG_TAG, "$filePath SHA256 is: $data")
                            ZLog.d(LOG_TAG, "$filePath SHA256 length is: ${data.length}")
                        }
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

    fun testDownloadProcess() {
        DownloadAPK.startDownloadWithProcess(
                activity!!,
                String.format(
                        ZixieContext.applicationContext!!.getString(com.bihe0832.android.framework.R.string.dialog_apk_updating),
                        "（V.2.2.21)"
                ),
                "这是一个Desc测试",
                "https://dldir1.qq.com/INO/voice/taimei_trylisten.m4a",
                "340190503EE8DACBF2FE8DCC133C304E",
                "",
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

    fun testDownloadMoreThanOnce() {

        var url =
                "https://c5ea82c62f3216ea883c2d99c8d55e0d.rdt.tfogc.com:49156/imtt.dd.qq.com/sjy.20002/sjy.00001/16891/apk/717B4E2AED4A487A68D81C9976E48E20.apk"
        for (i in 0..3) {
            ThreadManager.getInstance().start({
                DownloadFile.download(
                        activity!!,
                        url, "", false, "fsdfdsffd$i", object : SimpleDownloadListener() {
                    private fun getString(): String {
                        return "SimpleDownloadListener" + this.hashCode() + "-" + i
                    }

                    override fun onComplete(filePath: String, item: DownloadItem) {
                        ZLog.d(
                                "testDownloadMoreThanOnce",
                                "onComplete : ${getString()} ${filePath}"
                        )
                    }

                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                        ZLog.d(
                                "testDownloadMoreThanOnce",
                                "onFail : ${getString()} ${errorCode} ${msg}"
                        )
                    }

                    override fun onProgress(item: DownloadItem) {
                        ZLog.d(
                                "testDownloadMoreThanOnce",
                                "testDownloadMoreThanOnce : ${getString()} ${item.process}"
                        )
                    }

                })

            }, 10 * i)
        }

    }

    fun testDownload() {
        showInputDialog(
                "文件下载到本地",
                "请输入要下载文件的URL",
                "https://cdn.bihe0832.com/audio/xiangsi.mp3",
                object : InputDialogCompletedCallback {
                    override fun onInputCompleted(p0: String?) {
                        if (URLUtils.isHTTPUrl(p0)) {
                            DownloadFile.download(activity!!, p0!!, ZixieContext.getLogFolder() + URLUtils.getFileName(p0), true, object : SimpleDownloadListener() {

                                override fun onComplete(filePath: String, item: DownloadItem) {
                                    ZLog.d(LOG_TAG, "testDownload onComplete : ${filePath}")
                                    ZLog.d(LOG_TAG, "testDownload onComplete : ${filePath}")
                                    filePath.let {
                                        ZLog.d(LOG_TAG, "getFileName: ${FileUtils.getFileName(it)}")
                                        ZLog.d(
                                                LOG_TAG,
                                                "getExtensionName: ${FileUtils.getExtensionName(it)}"
                                        )
                                        ZLog.d(
                                                LOG_TAG,
                                                "getFileNameWithoutEx: ${FileUtils.getFileNameWithoutEx(it)}"
                                        )
                                        ZLog.d(LOG_TAG, "getFileMD5: ${FileUtils.getFileMD5(it)}")
                                        ZLog.d(
                                                LOG_TAG,
                                                "getFileMD5: ${MD5.getFileMD5(it, 0, File(it).length())}"
                                        )
                                        ZLog.d(LOG_TAG, "getFileSHA256: ${FileUtils.getFileSHA256(it)}")
                                        ZLog.d(
                                                LOG_TAG,
                                                "getFileSHA256: ${
                                                    SHA256.getFileSHA256(
                                                            it,
                                                            0,
                                                            File(it).length()
                                                    )
                                                }"
                                        )
                                    }
                                }

                                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                                    ZLog.d(LOG_TAG, "testDownload onFail : ${errorCode} ${msg} $item")
                                }

                                override fun onProgress(item: DownloadItem) {
                                    ZLog.d(LOG_TAG, "testDownload : ${item.process}")
                                }

                            })
                        }
                    }

                }
        )
    }

    private var currentNum = 0
    fun testDownloadList() {
        val URL_YYB_WZ =
                "https://dlied4.myapp.com/myapp/1104922185/cos.release-77942/10053761_com.tencent.tmgp.speedmobile_a2238881_1.32.0.2188_uPIKoV.apk"
        val URL_YYB_TTS = "http://dldir1.qq.com/INO/assistant/com.google.android.tts.apk"
        val URL_YYB_CHANNEL = "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk"
        val URL_YYB_DDZ =
                "https://imtt.dd.qq.com/16891/apk/6670A2D979F70D880519412D6E951162.apk?fsname=com.qqgame.hlddz_7.012.001_217.apk&csr=1bbd"
        val URL_FILE = "https://dldir1.qq.com/INO/voice/taimei_trylisten.m4a"
        val URL_CONFIG = "https://cdn.bihe0832.com/app/update/get_apk.json"
        var listener = object : SimpleDownloadListener() {
            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownloadList onFail: ${errorCode} ${msg} $item")
            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownloadList onComplete: $filePath ${item}")
            }

            override fun onProgress(item: DownloadItem) {

            }

            override fun onWait(item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownloadList onWait: $item")
            }

        }
        mutableListOf<String>(
//                URL_YYB_DDZ, URL_YYB_QQ, URL_YYB_TTS, URL_YYB_GG, URL_FILE, URL_CONFIG
                URL_YYB_WZ, URL_YYB_DDZ, URL_YYB_TTS, URL_YYB_CHANNEL, URL_FILE, URL_CONFIG
        ).let {
            for (currentNum in 0 until it.size) {
                ThreadManager.getInstance().start({
                    ZLog.d(LOG_TAG, "testDownloadList : ${it.get(currentNum)}")
                    if (it.get(currentNum).equals(URL_CONFIG)) {
                        DownloadConfig.download(
                                requireContext(),
                                it.get(currentNum),
                                "",
                                object : DownloadConfig.ResponseHandler {
                                    override fun onSuccess(type: Int, response: String) {
                                        ZLog.d(
                                                LOG_TAG,
                                                "testDownloadList  onComplete: ${type} $response"
                                        )
                                    }

                                    override fun onFailed(errorCode: Int, msg: String) {
                                        ZLog.d(
                                                LOG_TAG,
                                                "testDownloadList DownloadConfig: ${errorCode} ${msg}"
                                        )
                                    }

                                })
                    } else if (it.get(currentNum).equals(URL_FILE) || it.get(currentNum)
                                    .equals(URL_YYB_CHANNEL)
                    ) {
                        DownloadFile.download(requireContext(), it.get(currentNum), listener)
                    } else {
                        DownloadAPK.download(requireContext(), it.get(currentNum), "", "")
                    }
                }, currentNum)


            }
        }


    }
}