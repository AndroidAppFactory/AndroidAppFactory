package com.bihe0832.android.lib.download.wrapper

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 主要用于二层封装
 */

object DownloadTools {

    private var mTempDownloadListenerList = ConcurrentHashMap<Long, CopyOnWriteArrayList<DownloadListener>>()
    private var mDownloadKeyListenerList = ConcurrentHashMap<Long, DownloadListener>()

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
                }

                override fun onStart(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onStart(item)
                    }
                }

                override fun onProgress(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onProgress(item)
                    }
                }

                override fun onPause(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onPause(item)
                    }
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onFail(errorCode, msg, item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)
                }

                fun notifySucc(finalPath: String, item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onComplete(finalPath, item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)
                }

                override fun onComplete(downloadFilePath: String, item: DownloadItem) {
                    if (needRename) {
                        if (downloadFilePath == finalPath) {
                            notifySucc(downloadFilePath, item)
                        } else {
                            ThreadManager.getInstance().start {
                                FileUtils.copyFile(File(downloadFilePath), File(finalPath), false).let { result ->
                                    if (result) {
                                        item.finalFilePath = finalPath
                                        ThreadManager.getInstance().runOnUIThread {
                                            notifySucc(finalPath, item)
                                        }
                                    } else {
                                        ThreadManager.getInstance().runOnUIThread {
                                            onFail(DownloadErrorCode.ERR_FILE_RENAME_FAILED, "download success and rename failed", item)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        notifySucc(downloadFilePath, item)
                    }
                }

                override fun onDelete(item: DownloadItem) {
                    mTempDownloadListenerList[item.downloadID]?.forEach {
                        it.onDelete(item)
                    }
                    mTempDownloadListenerList.remove(item.downloadID)
                }
            }
        }
    }

    fun download(context: Context, title: String, msg: String, url: String, path: String, isFilePath: Boolean, md5: String, sha256: String, forceDownloadNew: Boolean, UseMobile: Boolean, actionKey: String, forceDownload: Boolean, downloadListener: DownloadListener?) {
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
            it.downloadListener = mTempDownloadListenerList[it.downloadID]?.firstOrNull()
            DownloadUtils.startDownload(context, it, forceDownload)
        }
    }
}