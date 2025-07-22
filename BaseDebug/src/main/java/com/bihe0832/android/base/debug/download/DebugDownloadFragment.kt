package com.bihe0832.android.base.debug.download

import android.provider.Settings
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.request.ZixieRequestHttp
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadConfig
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.download.wrapper.DownloadRangeUtils
import com.bihe0832.android.lib.download.wrapper.DownloadTools
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.io.File

class DebugDownloadFragment : BaseDebugListFragment() {
    val URL_YYB_WZ =
        "https://dlied4.myapp.com/myapp/1104922185/cos.release-77942/10053761_com.tencent.tmgp.speedmobile_a2238881_1.32.0.2188_uPIKoV.apk"

    val URL_YYB_TTS = "http://dldir1.qq.com/INO/assistant/com.google.android.tts.apk"
    val URL_YYB_CHANNEL = "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk"
    val URL_YYB_DDZ =
        "https://imtt.dd.qq.com/16891/apk/6670A2D979F70D880519412D6E951162.apk?fsname=com.qqgame.hlddz_7.012.001_217.apk&csr=1bbd"
    val URL_FILE = "https://dldir1.qq.com/INO/voice/taimei_trylisten.m4a"
    val URL_CONFIG = "https://cdn.bihe0832.com/app/update/get_apk.json"
    val MD5_FILE = "4ef99863858b0ee17177f773580e4f2a"
    val MD5_CONFIG = "E1E127FE9F951F0A71FD4AA695449305"

    companion object {
        val LOG_TAG = "DebugDownloadFragment"

        val globalListener = object : SimpleDownloadListener() {
            override fun onProgress(item: DownloadItem) {
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                ZLog.d(LOG_TAG, "globalListener onFail " + msg)
            }

            override fun onComplete(filePath: String, item: DownloadItem): String {
                ZLog.d(LOG_TAG, "globalListener onComplete : $filePath")
                return filePath
            }

            override fun onStart(item: DownloadItem) {
                ZLog.d(LOG_TAG, "testDownload onStart : $item")
            }
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("下载并计算文件的具体信息", View.OnClickListener { testDownload() }))
            add(getDebugItem("测试带进度下载", View.OnClickListener { testDownloadProcess() }))
            add(getDebugItem("测试区间下载", View.OnClickListener { testDownloadRange() }))
            add(getDebugItem("测试下载队列", View.OnClickListener { testDownloadList() }))
            add(getDebugItem("多位置触发下载", View.OnClickListener { testDownloadMoreThanOnce() }))

            add(
                getDebugItem(
                    "打开应用安装界面",
                    View.OnClickListener {
                        IntentUtils.startAppSettings(
                            context,
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        )
                    },
                ),
            )
            add(
                getDebugItem(
                    "卸载应用",
                    View.OnClickListener {
                        InstallUtils.uninstallAPP(
                            context,
                            "com.google.android.tts",
                        )
                    },
                ),
            )
            add(
                getDebugItem(
                    "自定义Provider安装",
                    View.OnClickListener { startDownload(INSTALL_BY_CUSTOMER) },
                ),
            )
            add(
                getDebugItem(
                    "默认Provider安装",
                    View.OnClickListener { startDownload(INSTALL_BY_DEFAULT) },
                ),
            )
            add(getDebugItem("通过ZIP安装OBB", View.OnClickListener { testInstallOOBByZip() }))
            add(
                getDebugItem(
                    "通过ZIP安装超大OBB",
                    View.OnClickListener { testInstallOOBByBigZip() })
            )
            add(
                getDebugItem(
                    "通过文件夹安装OBB",
                    View.OnClickListener { testInstallOOBByFolder() })
            )
            add(
                getDebugItem(
                    "通过文件夹安装超大OBB",
                    View.OnClickListener { testInstallBigOOBByFolder() })
            )
            add(
                getDebugItem(
                    "通过ZIP安装Split",
                    View.OnClickListener { testInstallSplitByGoodZip() })
            )
            add(
                getDebugItem(
                    "通过非标准Split格式的ZIP安装Split",
                    View.OnClickListener { testInstallSplitByBadZip() },
                ),
            )
            add(
                getDebugItem(
                    "通过文件夹安装Split",
                    View.OnClickListener { testInstallSplitByFolder() })
            )
            add(
                getDebugItem(
                    "测试文件下载及GZIP 解压",
                    View.OnClickListener { testDownloadGzip() })
            )
        }
    }

    val INSTALL_BY_DEFAULT = 0
    val INSTALL_BY_CUSTOMER = 1

    fun startDownload(type: Int) {
        DownloadItem().apply {
            setNotificationVisibility(true)
            downloadTitle = ThemeResourcesManager.getString(R.string.app_name)
            downloadDesc = "ffsf"
            downloadIcon = if (type == INSTALL_BY_CUSTOMER) {
                "https://cdn.bihe0832.com/images/zixie_32.ico"
            } else {
                "https://cdn.bihe0832.com/images/head.jpg"
            }
            downloadURL = "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk"
            contentMD5 = "E4DFE6298B5C727CD7E6134173BEE6D4"

//            downloadURL = if (type == INSTALL_BY_CUSTOMER) {
//                "https://imtt.dd.qq.com/sjy.10001/16891/apk/E2F59135FAE358442D2137E446AB59DE.apk"
//            } else {
//                "https://imtt.dd.qq.com/sjy.10001/16891/apk/2A5BC6AA4E69DCE13C6D5D3FB820706E.apk"
//            }
            setShouldForceReDownload(false)
            downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    showResult("应用下载失败（$errorCode）")
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    showResult("startDownloadApk download installApkPath: $filePath")
                    if (type == INSTALL_BY_CUSTOMER) {
                        var photoURI =
                            ZixieFileProvider.getZixieFileProvider(context!!, File(filePath))
//                        InstallUtils.installAPP(context, photoURI, File(filePath))
                        InstallUtils.installAPP(
                            context,
                            filePath,
                            "com.bihe0832.puzzle",
                            object : InstallListener {
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

                                override fun onInstallSuccess() {
                                    ZLog.d(LOG_TAG, "onInstallSuccess")
                                }

                                override fun onInstallTimeOut() {
                                    ZLog.d(LOG_TAG, "onInstallTimeOut")
                                }
                            })
                    }

                    if (type == INSTALL_BY_DEFAULT) {
                        InstallUtils.installAPP(context, filePath)
                    }
                    return filePath
                }

                override fun onProgress(item: DownloadItem) {
                    showResult("${item.finished}/${item.contentLength}")
                }
            }
        }.let {
            DownloadFileUtils.startDownload(context, it, it.shouldForceReDownload())
        }
    }

    private fun testInstallOOBByZip() {
        testInstallOOB("/sdcard/Download/jp.co.sumzap.pj0007.zip", "jp.co.sumzap.pj0007")
    }

    private fun testInstallOOBByBigZip() {
        testInstallOOB(
            "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
            "com.herogame.gplay.lastdayrulessurvival",
        )
    }

    private fun testInstallOOBByFolder() {
        testInstallOOB(
            ZixieFileProvider.getZixieTempFolder(context!!) + "/test/",
            "jp.co.sumzap.pj0007",
        )
    }

    private fun testInstallBigOOBByFolder() {
        testInstallOOB(
            "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927",
            "com.herogame.gplay.lastdayrulessurvival",
        )
    }

    private fun testInstallOOB(filePath: String, packangeName: String) {
        ZLog.d("testInstallOOB")
        InstallUtils.installAPP(
            context,
            filePath,
            packangeName,
            object : InstallListener {
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

                override fun onInstallSuccess() {
                    ZLog.d(LOG_TAG, "onInstallSuccess")
                }

                override fun onInstallTimeOut() {
                    ZLog.d(LOG_TAG, "onInstallTimeOut")
                }
            },
        )
    }

    private fun testInstallSplitByGoodZip() {
        val file = AAFFileWrapper.getFileTempFolder() + "com.supercell.brawlstars.zip"
        FileUtils.copyAssetsFileToPath(context!!, "com.supercell.brawlstars.zip", file)
        testInstallSplit(
            file,
            "com.supercell.brawlstars",
        )
    }

    private fun testInstallSplitByBadZip() {
        testInstallSplit(
            "/sdcard/Download/a3469c6189204495bc0283e909eb94a6_com.riotgames.legendsofruneterratw_113012.zip",
            "com.riotgames.legendsofruneterratw",
        )
    }

    private fun testInstallSplitByFolder() {
        testInstallSplit(
            ZixieFileProvider.getZixieTempFolder(context!!) + "/com.supercell.brawlstars",
            "com.supercell.brawlstars",
        )
    }

    private fun testInstallSplit(filePath: String, packangeName: String) {
        ZLog.d("test")
        InstallUtils.installAPP(
            context,
            filePath,
            packangeName,
            object : InstallListener {
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

                override fun onInstallSuccess() {
                    ZLog.d(LOG_TAG, "onInstallSuccess")
                }

                override fun onInstallTimeOut() {
                    ZLog.d(LOG_TAG, "onInstallTimeOut")
                }
            },
        )
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
        DownloadFile.forceDownload(
            requireContext(),
            "http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip",
            AAFFileWrapper.getFileCacheFolder() + System.currentTimeMillis() + "_20220321114751.json.gzip",
            false,
            object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ZLog.d(LOG_TAG, "onFail:" + msg)
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
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
                    return filePath
                }

                override fun onProgress(item: DownloadItem) {
                }
            },
        )

        ZixieRequestHttp.getOrigin("http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip")
            ?.let {
                val filePath = AAFFileWrapper.getFileTempFolder() + "a.gzip"

                FileUtils.writeToFile(filePath, it, false)
                FileUtils.writeHexToFile(filePath, "00FFDD", true)
                ZLog.d(LOG_TAG, "MD5 $filePath:" + MD5.getFileMD5(filePath))
//            ZLog.d(LOG_TAG, "hhh 1" + GzipUtils.decompress(it))
                ZLog.d(LOG_TAG, "hhh 2" + FileUtils.getFileContent(filePath, true))
            }

        ZixieRequestHttp.get("https://cdn.bihe0832.com/app/update/get_apk.json").let {
            ZLog.d(LOG_TAG, "result 1 :$it")
        }
    }

    private fun startDownload(
        url: String?, start: Long, length: Int, md5: String
    ) {
        val file = File(AAFFileWrapper.getFileCacheFolder() + FileUtils.getFileName(url))
        file.createNewFile()
        DownloadRangeUtils.startDownload(
            activity!!,
            url,
            file.absolutePath,
            start,
            length.toLong(),
            0,
            md5,
            object : SimpleDownloadListener() {
                override fun onWait(item: DownloadItem) {
                    ZLog.d(
                        "testDownloadRange", "onStart : ${item.downloadID} ${item.processDesc}"
                    )
                }

                override fun onStart(item: DownloadItem) {
                    ZLog.d(
                        "testDownloadRange", "onStart : ${item.downloadID} ${item.processDesc}"
                    )

                }

                override fun onProgress(item: DownloadItem) {
                    ZLog.d(
                        "testDownloadRange",
                        "onProgress : ${item.downloadID} - ${item.processDesc}",
                    )
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ZLog.w(
                        "testDownloadRange",
                        "onFail : ${item.downloadID} - ${errorCode} ${msg}",
                    )
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    ZLog.w(
                        "testDownloadRange",
                        "onComplete start $start : ${item.downloadID} - ${
                            MD5.getMd5(
                                FileUtils.readDataFromFile(
                                    filePath, start,
                                    length
                                )
                            )
                        }",
                    )
//                    DownloadRangeUtils.deleteTask(item.downloadID, true)

                    return filePath
                }
            })
    }

    fun testDownloadRange() {
//        for (i in 0 until 1) {
        val url = URL_YYB_WZ
        val start = 0 * 50000000L
        startDownload(url, start, 2000000, "cfd58b9748b44df6c56dbf22f89a737d")
//        }
    }

    fun testDownloadProcess() {
        val url = URL_CONFIG
        val md5 = MD5_CONFIG
//        val url = ""
//        val md5 = ""
        val header = HashMap<String, String>().apply {
            put("zixie", "NetworkApi")
        }
        DownloadFile.downloadWithProcess(
            activity!!,
            String.format(
                ThemeResourcesManager.getString(com.bihe0832.android.framework.R.string.dialog_apk_updating)!!,
                "（V.2.2.21)",
            ),
            "这是一个Desc测试", url, header, "", false,
            md5, "",
            canCancel = true,
            forceDownloadNew = false,
            useMobile = true,
            forceDownload = true,
            needRecord = false,
            listener = object : OnDialogListener {
                override fun onPositiveClick() {
                }

                override fun onNegativeClick() {
                }

                override fun onCancel() {
                }
            },
            downloadListener = object : SimpleDownloadListener() {
                override fun onProgress(item: DownloadItem) {
                    ZLog.d(
                        "testDownloadProcess",
                        "onProgress : ${item.downloadID} - ${item.processDesc}",
                    )
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ZLog.w(
                        "testDownloadProcess",
                        "onFail : ${item.downloadID} - ${errorCode} ${msg}",
                    )
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    ZLog.w(
                        "testDownloadProcess",
                        "onComplete : ${item.downloadID} - ${
                            MD5.getMd5(
                                FileUtils.readDataFromFile(
                                    filePath,
                                    0,
                                    20000
                                )
                            )
                        }",
                    )
                    for (i in 0 until 6) {
                        val start = i * 10000L
                        ZLog.w(
                            "testDownloadProcess",
                            "onComplete  start $start: ${item.downloadID} - ${
                                MD5.getMd5(
                                    FileUtils.readDataFromFile(
                                        filePath, start, 10000
                                    )
                                )
                            }",
                        )
                    }
                    Thread.sleep(4000)
//                    FileUtils.deleteFile(filePath)
                    return filePath
                }

            }
        )
    }

    fun testDownloadMoreThanOnce() {
        var url =
            "http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip"
        val filePath =
            "/storage/emulated/0/Android/data/com.bihe0832.android.test/files/zixie/"
        for (i in 0..4) {
            ThreadManager.getInstance().start({
                DownloadFile.download(
                    activity!!,
                    url,
                    filePath,
                    i % 2 == 1,
                    object : SimpleDownloadListener() {
                        private fun getString(): String {
                            return "SimpleDownloadListener" + this.hashCode() + "-" + i
                        }

                        override fun onComplete(filePath: String, item: DownloadItem): String {
                            ZLog.d(
                                LOG_TAG,
                                "testDownloadMoreThanOnce onComplete : ${getString()} $filePath",
                            )
                            return filePath
                        }

                        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                            ZLog.d(
                                LOG_TAG,
                                "testDownloadMoreThanOnce onFail : ${getString()} $errorCode $msg",
                            )
                        }

                        override fun onProgress(item: DownloadItem) {
                            ZLog.d(
                                "testDownloadMoreThanOnce",
                                "testDownloadMoreThanOnce : ${getString()} ${item.process}",
                            )
                        }
                    },
                )
            }, 2)
        }
    }

    fun testDownload() {
        showInputDialog(
            "文件下载到本地",
            "请输入要下载文件的URL",
            URL_YYB_TTS,
            object : DialogCompletedStringCallback {
                override fun onResult(p0: String?) {
                    if (URLUtils.isHTTPUrl(p0)) {
                        FileUtils.deleteFile(AAFFileWrapper.getTempFolder() + "a.a")
//                        DownloadFile.download(
//                            activity!!,
//                            p0!!,
//                            AAFFileWrapper.getFileTempFolder(),
//                            false,
//                            null,
//                        )
                        DownloadFile.forceDownload(
                            activity!!,
                            URL_FILE,
                            AAFFileWrapper.getTempFolder() + "a.a",
                            true,
                            MD5_FILE,
                            object : SimpleDownloadListener() {

                                override fun onComplete(
                                    filePath: String,
                                    item: DownloadItem
                                ): String {
                                    ZLog.d(LOG_TAG, "testDownload onComplete 1 : ${filePath}")
                                    val a = 0
                                    var b = 5 / a
                                    filePath.let {
                                        ZLog.d(LOG_TAG, "getFileName: ${FileUtils.getFileName(it)}")
                                        ZLog.d(
                                            LOG_TAG,
                                            "getExtensionName: ${FileUtils.getExtensionName(it)}"
                                        )
                                        ZLog.d(
                                            LOG_TAG,
                                            "getFileNameWithoutEx: ${
                                                FileUtils.getFileNameWithoutEx(
                                                    it
                                                )
                                            }"
                                        )
                                        ZLog.d(LOG_TAG, "getFileMD5: ${FileUtils.getFileMD5(it)}")
//                                        ZLog.d(
//                                            LOG_TAG,
//                                            "getFileMD5: ${MD5.getFileMD5(it, 0, File(it).length())}"
//                                        )
//                                        ZLog.d(LOG_TAG, "getFileSHA256: ${FileUtils.getFileSHA256(it)}")
//                                        ZLog.d(
//                                            LOG_TAG,
//                                            "getFileSHA256: ${
//                                                SHA256.getFileSHA256(
//                                                    it,
//                                                    0,
//                                                    File(it).length()
//                                                )
//                                            }"
//                                        )
                                    }
                                    return filePath
                                }

                                override fun onFail(
                                    errorCode: Int,
                                    msg: String,
                                    item: DownloadItem
                                ) {
                                    ZLog.d(
                                        LOG_TAG,
                                        "testDownload onFail 1: ${errorCode} ${msg} $item"
                                    )
                                }

                                override fun onProgress(item: DownloadItem) {
                                    ZLog.d(LOG_TAG, "testDownload : ${item.process}")
                                }

                            })

                        DownloadFile.forceDownload(
                            activity!!,
                            URL_FILE,
                            AAFFileWrapper.getTempFolder() + "a.a",
                            true,
                            MD5_FILE,
                            object : SimpleDownloadListener() {

                                override fun onComplete(
                                    filePath: String,
                                    item: DownloadItem
                                ): String {
                                    ZLog.d(LOG_TAG, "testDownload onComplete 2: ${filePath}")
                                    filePath.let {
                                        ZLog.d(LOG_TAG, "getFileName: ${FileUtils.getFileName(it)}")
                                        ZLog.d(
                                            LOG_TAG,
                                            "getExtensionName: ${FileUtils.getExtensionName(it)}"
                                        )
                                        ZLog.d(
                                            LOG_TAG,
                                            "getFileNameWithoutEx: ${
                                                FileUtils.getFileNameWithoutEx(
                                                    it
                                                )
                                            }"
                                        )
                                        ZLog.d(LOG_TAG, "getFileMD5: ${FileUtils.getFileMD5(it)}")
//                                        ZLog.d(
//                                            LOG_TAG,
//                                            "getFileMD5: ${MD5.getFileMD5(it, 0, File(it).length())}"
//                                        )
//                                        ZLog.d(LOG_TAG, "getFileSHA256: ${FileUtils.getFileSHA256(it)}")
//                                        ZLog.d(
//                                            LOG_TAG,
//                                            "getFileSHA256: ${
//                                                SHA256.getFileSHA256(
//                                                    it,
//                                                    0,
//                                                    File(it).length()
//                                                )
//                                            }"
//                                        )
                                    }
                                    return filePath
                                }

                                override fun onFail(
                                    errorCode: Int,
                                    msg: String,
                                    item: DownloadItem
                                ) {
                                    ZLog.d(
                                        LOG_TAG,
                                        "testDownload onFail 2: ${errorCode} ${msg} $item"
                                    )
                                }

                                override fun onProgress(item: DownloadItem) {
                                    ZLog.d(LOG_TAG, "testDownload : ${item.process}")
                                }

                            })
                    }
                }
            },
        )
    }

    private var currentNum = 0
    fun testDownloadList() {


        var listener = null
//        var listener = object : SimpleDownloadListener() {
//            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
//                ZLog.d(LOG_TAG, "testDownloadList onFail: $errorCode $msg $item")
//            }
//
//            override fun onComplete(filePath: String, item: DownloadItem): String {
//                ZLog.d(LOG_TAG, "testDownload onComplete : $filePath")
//                return filePath
//            }
//
//            override fun onProgress(item: DownloadItem) {
//            }
//
//            override fun onWait(item: DownloadItem) {
//                ZLog.d(LOG_TAG, "testDownloadList onWait: $item")
//            }
//        }
        mutableListOf<String>(
//                URL_YYB_DDZ, URL_YYB_QQ, URL_YYB_TTS, URL_YYB_GG, URL_FILE, URL_CONFIG
//            URL_YYB_WZ,
//            URL_YYB_DDZ,
//            URL_YYB_TTS,
//            URL_YYB_CHANNEL,
//            URL_FILE,
            URL_CONFIG,
        ).let {
            for (currentNum in 0 until it.size) {
                ThreadManager.getInstance().start({
                    ZLog.d(LOG_TAG, "testDownloadList : ${it.get(currentNum)}")
                    if (it.get(currentNum).equals(URL_CONFIG)) {
                        DownloadConfig.download(
                            requireContext(),
                            it.get(currentNum),
                            AAFFileWrapper.getConfigFolder(),
                            "E1E127FE9F951F0A71FD4AA695449305",
                            object : DownloadConfig.ResponseHandler {
                                override fun onSuccess(type: Int, response: String) {
                                    ZLog.d(
                                        LOG_TAG,
                                        "testDownloadList  onComplete: $type $response",
                                    )
                                }

                                override fun onFailed(errorCode: Int, msg: String) {
                                    ZLog.d(
                                        LOG_TAG,
                                        "testDownloadList DownloadConfig: $errorCode $msg",
                                    )
                                }
                            },
                        )
                    } else if (it.get(currentNum).equals(URL_FILE) || it.get(currentNum)
                            .equals(URL_YYB_CHANNEL)
                    ) {
                        DownloadFile.download(requireContext(), it.get(currentNum), listener)
                    } else {
                        DownloadFile.download(requireContext(), it.get(currentNum), listener)
//                        DownloadAPK.download(requireContext(), it.get(currentNum), "", "")
                    }
                }, currentNum)
            }
        }
    }
}
