package com.bihe0832.android.lib.download.manager

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.bihe0832.android.lib.download.*
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encypt.MD5
import java.io.File

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-09.
 * Description: Description
 */
abstract class DownloadWrapper {

    protected abstract fun goDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?)
    abstract fun cancleDownload(url: String)
    protected abstract fun onDestory()

    val TAG = "Download ->"

    protected val CONTENT_URI = Uri.parse("content://downloads/my_downloads")
    protected var mCurrentDownloadList = HashMap<String, DownloadItem>()
    protected var applicationContext: Context? = null

    fun startDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?) {
        Log.d(TAG, "info:${info}")
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }

        if (TextUtils.isEmpty(info.fileName) || TextUtils.isEmpty(info.downloadURL)) {
            downloadListener?.onError(-4, "bad para")
            return
        }

        var finalFileName = applicationContext!!.getExternalFilesDir(applicationContext!!.getString(R.string.lib_bihe0832_file_folder)).absolutePath + "/" + info.fileName
        if (FileUtils.checkFileExist(finalFileName, info.fileMD5)) {
            if (info.forceDownloadNew) {
                FileUtils.deleteFile(finalFileName)
            } else {
                downloadListener?.onSuccess(finalFileName)
                return
            }
        }

        when (hasDownload(applicationContext!!, info.downloadURL, info.fileName, info.fileMD5, info.forceDownloadNew)) {
            NO_DOWNLOAD -> {
                downloadListener?.let {
                    info.downloadNotifyListenerList.add(it)
                }
            }
            IS_DOWNLOADING -> {
                if (info.forceDownloadNew) {
                    mCurrentDownloadList[info.downloadURL]?.downloadNotifyListenerList?.let {
                        info.downloadNotifyListenerList.addAll(it)
                    }
//                    cancleDownload(info.downloadURL)
                }else{
                    downloadListener?.let {
                        mCurrentDownloadList[info.downloadURL]?.apply {
                            downloadNotifyListenerList.add(downloadListener)
                        }
                    }
                    return
                }
            }
        }
        goDownload(context, info, downloadListener)
    }

    protected fun stopDownload(url: String) {
        mCurrentDownloadList.remove(url)
        if (mCurrentDownloadList.isEmpty()) {
            ThreadManager.getInstance().start({
                if (mCurrentDownloadList.isEmpty()) {
                    onDestory()
                }
            }, 60)
        }
    }

    protected fun notifyDownload(downloadInfo : DownloadItem,fileUri :String){
        mCurrentDownloadList.remove(downloadInfo.downloadURL)
        var downloadFile: String = Uri.parse(fileUri).path
        var finalFileName: String = applicationContext!!.getExternalFilesDir(applicationContext!!.getString(R.string.lib_bihe0832_file_folder)).absolutePath + "/" + downloadInfo.fileName
        try {
            val oldfile = File(downloadFile)
            Log.d(TAG, " oldfile:$oldfile")
            Log.d(TAG, " oldfile:" + MD5.getFileMD5(oldfile))
            val md5 = MD5.getFileMD5(downloadFile)
            if (TextUtils.isEmpty(downloadInfo.fileMD5) || md5.equals(downloadInfo.fileMD5,ignoreCase = true)) {
                val newfile = File(finalFileName)
                if (newfile.exists()) {
                    newfile.delete()
                }
                if (oldfile.renameTo(newfile)) {
                    Log.d(TAG, " File renamed")
                    Log.d(TAG, " newfile:$newfile")
                    Log.d(TAG, " newfile:" + MD5.getFileMD5(newfile))
                    Log.d(TAG, " :$finalFileName")
                    Log.d(TAG, " :" + MD5.getFileMD5(finalFileName))
                    downloadInfo.downloadNotifyListenerList?.forEach {
                        it.onSuccess(finalFileName)
                    }
                } else {
                    Log.d(TAG, "Sorry! the file can't be renamed")
                    downloadInfo.downloadNotifyListenerList?.forEach {
                        it.onSuccess(downloadFile)
                    }
                }
            } else {
                downloadInfo.downloadNotifyListenerList?.forEach {
                    it.onError(-2, "Sorry! the file md5 is bad")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            downloadInfo.downloadNotifyListenerList?.forEach {
                it.onError(-3, "Sorry! the file can't be renamed")
            }
        }
    }

    fun hasDownload(context: Context, url: String, fileName: String, fileMD5: String, forceDownloadNew: Boolean): Int {
        if(applicationContext == null){
            applicationContext = context
        }
        var filePath = applicationContext!!.getExternalFilesDir(applicationContext!!.getString(R.string.lib_bihe0832_file_folder)).absolutePath + "/" + fileName
        return if (FileUtils.checkFileExist(filePath, fileMD5) && !forceDownloadNew) {
            //已下载
            HAS_DOWNLOAD
        } else {
            if (mCurrentDownloadList?.containsKey(url)) {
                //正在下载
                IS_DOWNLOADING
            } else {
                //未下载
                NO_DOWNLOAD
            }
        }
    }
}
