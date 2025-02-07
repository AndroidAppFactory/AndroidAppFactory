package com.bihe0832.android.base.debug.audio.asr

import android.app.Activity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.zip.ZipUtils
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
object ASRModelDownloadManager {

    const val TAG = "ASRModelManager"

    fun checkAndDoAction(
        activity: Activity, model: String, url: String, md5: String, action: () -> Unit
    ) {
        ZLog.d(TAG, "model:$model")
        ZLog.d(TAG, "url:$url")
        ZLog.d(TAG, "md5:$md5")
        val path = getASRModelRoot() + model
        ZLog.d(TAG, "path:$path")
        val folder = File(path)
        if (folder.isDirectory && folder.exists()) {
            ZixieContext.showToast("模型 $model 已准备就绪")
            folder.listFiles()?.forEach {
                ZLog.d(TAG, "files :${it.absolutePath}")
            }
            action()
        } else {
            DialogUtils.showConfirmDialog(activity,
                "语音识别模型缺失",
                "识别模型：$model 未下载，是否启动下载",
                false,
                object : OnDialogListener {
                    override fun onPositiveClick() {
                        prepareASRModel(
                            activity,
                            model,
                            url,
                            md5,
                            object : AAFDataCallback<Boolean>() {
                                override fun onSuccess(result: Boolean?) {
                                    if (result == true) {
                                        action()
                                    } else {
                                        ZixieContext.showToast("模型下载失败！")
                                    }
                                }
                            })
                    }

                    override fun onNegativeClick() {

                    }

                    override fun onCancel() {

                    }

                })
        }
    }

    fun prepareASRModel(
        activity: Activity,
        model: String,
        url: String,
        mD5: String,
        callBack: AAFDataCallback<Boolean>?
    ) {
        val dialog = LoadingDialog(activity)
        downASRModel(activity, model, url, mD5, object : SimpleDownloadListener() {
            override fun onComplete(filePath: String, item: DownloadItem): String {
                ZLog.d(TAG, "File:$filePath")
                ZLog.d(TAG, "MD5:" + FileUtils.getFileMD5(filePath))
                unzipModel1(
                    dialog,
                    filePath,
                    getASRModelRoot(),
                    object : AAFDataCallback<Boolean>() {
                        override fun onSuccess(result: Boolean?) {
                            dialog.dismiss()
                            if (result == true) {
                                ZixieContext.showToast("模型$model 已准备就绪")
                                callBack?.onSuccess(true)
                            } else {
                                ZixieContext.showToast("模型$model 已准备异常")
                                callBack?.onSuccess(false)
                            }
                        }

                        override fun onError(errorCode: Int, msg: String) {
                            super.onError(errorCode, msg)
                            dialog.dismiss()
                            callBack?.onSuccess(false)
                        }
                    })

                return filePath
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                callBack?.onSuccess(false)
            }

            override fun onProgress(item: DownloadItem) {

            }

        })
    }

    fun unzipModel1(
        dialog: LoadingDialog,
        modelZipFile: String,
        modelFolder: String,
        callback: AAFDataCallback<Boolean>
    ) {
        dialog.show("正在解压模型文件...")
        ThreadManager.getInstance().start {
            val result = ZipUtils.unzip(modelZipFile, modelFolder)
            ZLog.d(TAG, "unzipModel1:$result")
            ThreadManager.getInstance().runOnUIThread {
                callback.onSuccess(result)
            }
        }
    }

    private fun downASRModel(
        activity: Activity,
        model: String,
        url: String,
        mD5: String,
        downloadListener: SimpleDownloadListener
    ) {
        DownloadFile.downloadWithCheckAndProcess(
            activity,
            "ASR 识别模型下载",
            "当前正在为您下载模型 $model 的资源，请稍候。",
            url,
            AAFFileWrapper.getTempFolder(),
            false,
            mD5,
            true,
            object : OnDialogListener {
                override fun onPositiveClick() {

                }

                override fun onNegativeClick() {

                }

                override fun onCancel() {

                }

            },
            downloadListener
        )
    }

    fun checkFileInfo(model: String, fileName: String) {
        val path = getASRModelRoot() + "$model/$fileName"
        ZLog.d(TAG, "path:$path")
        ZLog.d(TAG, "checkFileExist:${FileUtils.checkFileExist(path)}")
        ZLog.d(TAG, "md5:${FileUtils.getFileMD5(path)}")
    }
}