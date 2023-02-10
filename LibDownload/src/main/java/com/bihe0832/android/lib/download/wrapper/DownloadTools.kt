package com.bihe0832.android.lib.download.wrapper

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 主要用于二层封装，所有的下载会优先走到预处理的listener，处理结束以后再对外回调最终结果，仅能回调通过：
 *
 * DownLoadAPK，DownloadConfig，DownloadFile 触发的下载
 */

object DownloadTools {

    private var mTempDownloadListenerList = ConcurrentHashMap<Long, CopyOnWriteArrayList<DownloadListener>>()
    private var mGlobalDownloadListenerList = CopyOnWriteArrayList<DownloadListener>()

    private var mDownloadKeyListenerList = ConcurrentHashMap<Long, DownloadListener>()

    fun addGlobalDownloadListener(downloadListener: DownloadListener?) {
        mGlobalDownloadListenerList.add(downloadListener)
    }

    fun removeGlobalDownloadListener(downloadListener: DownloadListener?) {
        if (mGlobalDownloadListenerList.contains(downloadListener)) {
            mGlobalDownloadListenerList.remove(downloadListener)
        }
    }


    private fun addDownloadListenerToList(downloadID: Long, downloadListener: DownloadListener?) {
        downloadListener?.let {
            if (mTempDownloadListenerList.containsKey(downloadID) && null != mTempDownloadListenerList[downloadID]) {
                mTempDownloadListenerList[downloadID]!!.add(it)
            } else {
                mTempDownloadListenerList.put(downloadID, CopyOnWriteArrayList<DownloadListener>().apply {
                    add(it)
                })
            }
        }
    }

    private fun addNewKeyListener(downloadID: Long, finalPath: String, needRename: Boolean) {
        if (!mDownloadKeyListenerList.containsKey(downloadID)) {
            mDownloadKeyListenerList[downloadID] = object : DownloadListener {
                override fun onWait(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onWait(item)
                    }
                    mGlobalDownloadListenerList.forEach {
                        it.onWait(item)
                    }
                }

                override fun onStart(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onStart(item)
                    }
                    mGlobalDownloadListenerList.forEach {
                        it.onStart(item)
                    }
                }

                override fun onProgress(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onProgress(item)
                    }

                    mGlobalDownloadListenerList.forEach {
                        it.onProgress(item)
                    }
                }

                override fun onPause(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onPause(item)
                    }

                    mGlobalDownloadListenerList.forEach {
                        it.onPause(item)
                    }
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onFail(errorCode, msg, item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)

                    mGlobalDownloadListenerList.forEach {
                        it.onFail(errorCode, msg, item)
                    }
                }

                fun notifyDownloadSuccess(finalPath: String, item: DownloadItem): String {
                    var path = finalPath
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        path = it.onComplete(path, item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)

                    mGlobalDownloadListenerList.forEach {
                        path = it.onComplete(path, item)
                    }
                    return path
                }

                override fun onComplete(downloadFilePath: String, item: DownloadItem): String {
                    if (needRename) {
                        if (downloadFilePath == finalPath) {
                            return notifyDownloadSuccess(downloadFilePath, item)
                        } else {
                            FileUtils.copyFile(File(downloadFilePath), File(finalPath), true).let { result ->
                                if (result) {
                                    ThreadManager.getInstance().runOnUIThread {
                                        notifyDownloadSuccess(finalPath, item)
                                    }
                                    return finalPath
                                } else {
                                    ThreadManager.getInstance().runOnUIThread {
                                        onFail(DownloadErrorCode.ERR_FILE_RENAME_FAILED, "download success and rename failed", item)
                                    }
                                    return downloadFilePath
                                }
                            }
                        }
                    } else {
                        return notifyDownloadSuccess(downloadFilePath, item)
                    }
                }

                override fun onDelete(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onDelete(item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)

                    mGlobalDownloadListenerList.forEach {
                        it.onDelete(item)
                    }
                }
            }
        }
    }

    fun startDownload(context: Context, title: String, msg: String, url: String, path: String, isFilePath: Boolean, md5: String, sha256: String, forceDownloadNew: Boolean, UseMobile: Boolean, actionKey: String, forceDownload: Boolean, downloadListener: DownloadListener?) {
        DownloadItem().apply {
            if (FileMimeTypes.isApkFile(URLUtils.getFileName(url))) {
                setNotificationVisibility(true)
            } else {
                setNotificationVisibility(false)
            }
            downloadURL = url
            downloadTitle = title
            downloadDesc = msg
            if (!TextUtils.isEmpty(path)) {
                fileFolder = if (isFilePath) {
                    File(path).parent
                } else {
                    path
                }
            }

            fileMD5 = md5
            fileSHA256 = sha256
            isForceDownloadNew = forceDownloadNew
            this.actionKey = actionKey
            isDownloadWhenUseMobile = UseMobile
        }.let {
            addNewKeyListener(it.downloadID, path, isFilePath)
            addDownloadListenerToList(it.downloadID, downloadListener)
            if (isFilePath && FileUtils.checkFileExist(path, 0, md5, sha256, false)) {
                it.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                it.filePath = path
                mDownloadKeyListenerList[it.downloadID]?.onComplete(path, it)
            } else {
                it.downloadListener = mDownloadKeyListenerList[it.downloadID]
                DownloadUtils.startDownload(context, it, forceDownload)
            }

        }
    }
}