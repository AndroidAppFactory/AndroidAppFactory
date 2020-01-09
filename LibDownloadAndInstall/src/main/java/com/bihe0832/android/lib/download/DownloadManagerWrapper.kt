package com.bihe0832.android.lib.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.bihe0832.android.lib.utils.encypt.MD5
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-09.
 * Description: Description
 */
object DownloadManagerWrapper {

    val TAG = "Download ->"

    private val CONTENT_URI = Uri.parse("content://downloads/my_downloads")
    private var mCurrentDownloadList = HashMap<String, DownloadItem>()
    private var applicationContext: Context? = null

    interface DownloadListener {
        fun onProgress(total: Int, cur: Int)
        fun onSuccess(finalFileName: String)
        fun onError(error: Int, errmsg: String)
    }

    class DownloadItem {
        var downloadID = 0L
        var dowmloadTitle = ""
        var downloadDesc = ""
        var notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
        var downloadURL = ""
        var fileName = ""
        var fileMD5 = ""
        var forceDownloadNew = false
        val downloadNotifyListenerList = ArrayList<DownloadListener>()
        override fun toString(): String {
            return "DownloadItem(downloadID=$downloadID, dowmloadTitle='$dowmloadTitle', downloadDesc='$downloadDesc', notificationVisibility=$notificationVisibility, downloadURL='$downloadURL', fileName='$fileName', fileMD5='$fileMD5', forceDownloadNew=$forceDownloadNew)"
        }
    }

    private var hasRegisterContentObserver = false
    private val contentObserver by lazy {
        object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                // 查询进度并回调
                val dm = applicationContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                mCurrentDownloadList.forEach { listItem ->
                    listItem.value?.let { downloadInfo ->
                        var c: Cursor? = null
                        try {
                            val query = DownloadManager.Query().setFilterById(downloadInfo.downloadID)
                            c = dm.query(query)
                            if(c != null && c.moveToFirst()) {
                                val curSize = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val totalSize = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                if (totalSize > 0) {
                                    downloadInfo.downloadNotifyListenerList?.forEach {
                                        it.onProgress(totalSize, curSize)
                                    }
                                }
                                if (curSize == totalSize) {
                                    mCurrentDownloadList.remove(downloadInfo.downloadURL)
                                    val fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    val fileUri = c.getString(fileUriIdx)
                                    var downloadFile: String = Uri.parse(fileUri).path
                                    var finalFileName: String = applicationContext!!.getExternalFilesDir(applicationContext!!.getString(R.string.lib_bihe0832_install_folder)).absolutePath + "/" + downloadInfo.fileName
                                    try {
                                        val oldfile = File(downloadFile)
                                        Log.d(TAG, " oldfile:$oldfile")
                                        Log.d(TAG, " oldfile:" + MD5.getFileMD5(oldfile))
                                        val md5 = MD5.getFileMD5(downloadFile)
                                        if (TextUtils.isEmpty(downloadInfo.fileMD5) || md5 == downloadInfo.fileMD5) {
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
                                                downloadInfo.downloadNotifyListenerList?.forEach {
                                                    it.onError(-1, "Sorry! the file can't be renamed")
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
                                }else{
                                    Log.e(TAG, "startDownloadApk downloading~")
                                }
                            }else{
                                Log.e(TAG, "startDownloadApk c may null")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "startDownloadApk: " + e.message)
                        } finally {
                            c?.close()
                        }
                    }
                }
            }
        }
    }

    private var hasRegisterDownloadBroadcastReceiver = false
    private val downloadBroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val id = intent.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1)?:0L
                mCurrentDownloadList.filter { it.value.downloadID == id }.forEach { listItem ->
                    val c = dm.query(DownloadManager.Query().setFilterById(id))
                    if (c != null && c.moveToFirst()) {
                        val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        // 下载失败也会返回这个广播，所以要判断下是否真的下载成功
                        if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                            listItem.value.downloadNotifyListenerList.forEach {
                                it.onError(-5, "download failed")
                            }
                        }
                        mCurrentDownloadList.remove(listItem.key)
                        c.close()
                    }
                }

            }
        }
    }

    fun startDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?) {
        Log.d(TAG, "info:${info}")
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
        if (hasDownload(info.downloadURL)) {
            if(info.forceDownloadNew){
                mCurrentDownloadList[info.downloadURL]?.downloadNotifyListenerList?.let {
                    info.downloadNotifyListenerList.addAll(it)
                }
                cancleDownload(info.downloadURL)
            }else{
                downloadListener?.let {
                    mCurrentDownloadList[info.downloadURL]?.apply {
                        downloadNotifyListenerList.add(downloadListener)
                    }
                }
                return
            }
        }else{
            downloadListener?.let {
                info.downloadNotifyListenerList.add(it)
            }
        }

        if (TextUtils.isEmpty(info.fileName) || TextUtils.isEmpty(info.downloadURL)) {
            mCurrentDownloadList[info.downloadURL]?.downloadNotifyListenerList?.forEach {
                it.onError(-4, "bad para")
            }
            return
        }
        var finalFileName = applicationContext!!.getExternalFilesDir(applicationContext!!.getString(R.string.lib_bihe0832_install_folder)).absolutePath + "/" + info.fileName
        if (checkFileExist(finalFileName, info.fileMD5)) {
            if (info.forceDownloadNew) {
                try {
                    File(finalFileName).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                mCurrentDownloadList[info.downloadURL]?.downloadNotifyListenerList?.forEach {
                    it.onSuccess(finalFileName)
                }
                return
            }
        }


        val request = DownloadManager.Request(Uri.parse(info.downloadURL)).apply {
            //设置允许使用的网络类型，这里是移动网络和wifi都可以
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)

            if (!TextUtils.isEmpty(info.dowmloadTitle)) {
                setTitle(info.dowmloadTitle)
            }else{
                setTitle(info.fileName)
            }

            if (!TextUtils.isEmpty(info.downloadDesc)) {
                setDescription(info.downloadDesc)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                allowScanningByMediaScanner()
                setNotificationVisibility(info.notificationVisibility)
            }
            setDestinationInExternalFilesDir(context, applicationContext!!.getString(R.string.lib_bihe0832_install_folder), "temp_" + System.currentTimeMillis() + "_" + info.fileName)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        info.downloadID = dm.enqueue(request)
        if(info.downloadID > 0){
            mCurrentDownloadList[info.downloadURL] = info
            if(!hasRegisterContentObserver){
                context.contentResolver.registerContentObserver(CONTENT_URI, true, contentObserver)
            }
            if(!hasRegisterDownloadBroadcastReceiver){
                context.registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        }else{
            info.downloadNotifyListenerList?.forEach {
                it.onError(-6,"id is bad")
            }
        }
    }

    fun cancleDownload(url:String){
        mCurrentDownloadList[url]?.let {
            val dm = applicationContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm?.remove(it.downloadID)
        }
        mCurrentDownloadList.remove(url)
    }

    fun hasDownload(url:String) : Boolean{
        return mCurrentDownloadList?.containsKey(url)
    }

    fun checkFileExist(filePath: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            file != null && file.length() > 0 && file.exists() && file.isFile
        }
    }

    fun checkFileExist(filePath: String, fileMD5: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            if (TextUtils.isEmpty(fileMD5)) {
                file != null && file.length() > 0 && file.exists() && file.isFile
            } else {
                MD5.getMd5(filePath).equals(fileMD5, ignoreCase = true)
            }
        }
    }
}
