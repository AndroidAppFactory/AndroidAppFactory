package com.bihe0832.android.lib.download.manager

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
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-10.
 * Description: Description
 *
 */

class DownloadByDownloadManager : DownloadWrapper() {

    private var hasRegisterContentObserver = false
    private var hasRegisterDownloadBroadcastReceiver = false

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
                            if (c != null && c.moveToFirst()) {
                                val curSize = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val totalSize = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                if (totalSize > 0) {
                                    downloadInfo.downloadNotifyListenerList?.forEach {
                                        it.onProgress(totalSize.toLong(), curSize.toLong())
                                    }
                                }
                                if (curSize == totalSize) {
                                    val fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    val fileUri = c.getString(fileUriIdx)
                                    notifyDownload(downloadInfo,fileUri)
                                } else {
                                    val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                    Log.e(TAG, "startDownloadApk downloading 11" + c.getInt(columnIndex))
                                }
                            } else {
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

    private val downloadBroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val id = intent.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: 0L
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

    override fun goDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?){
        if (!hasRegisterContentObserver) {
            context.contentResolver.registerContentObserver(CONTENT_URI, true, contentObserver)
        }
        if (!hasRegisterDownloadBroadcastReceiver) {
            context.registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
        val request = DownloadManager.Request(Uri.parse(info.downloadURL)).apply {
            //设置允许使用的网络类型，这里是移动网络和wifi都可以
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)

            if (!TextUtils.isEmpty(info.dowmloadTitle)) {
                setTitle(info.dowmloadTitle)
            } else {
                setTitle(info.fileName)
            }

            if (!TextUtils.isEmpty(info.downloadDesc)) {
                setDescription(info.downloadDesc)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                allowScanningByMediaScanner()
                setNotificationVisibility(info.notificationVisibility)
            }
            if (info.notificationVisibility == DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED ||
                    info.notificationVisibility == DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION) {
                setDestinationInExternalFilesDir(context, DOWNLOAD_PATH, info.fileName)
            } else {
                setDestinationInExternalFilesDir(context, DOWNLOAD_PATH, "temp_" + System.currentTimeMillis() + "_" + info.fileName)
            }
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        info.downloadID = dm.enqueue(request)
        if (info.downloadID > 0) {
            mCurrentDownloadList[info.downloadURL] = info
        } else {
            info.downloadNotifyListenerList?.forEach {
                it.onError(-6, "id is bad")
            }
        }
    }

    override fun cancleDownload(url: String) {
        mCurrentDownloadList[url]?.let {
            val dm = applicationContext?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm?.remove(it.downloadID)
            it.downloadNotifyListenerList.forEach {
                it.onError(-8, "cancle")
            }
        }
        stopDownload(url)
    }

    override fun onDestory() {
        if(hasRegisterContentObserver){
            applicationContext?.contentResolver?.unregisterContentObserver(contentObserver)
            hasRegisterContentObserver = false
        }
        if(hasRegisterDownloadBroadcastReceiver){
            applicationContext?.unregisterReceiver(downloadBroadcastReceiver)
            hasRegisterDownloadBroadcastReceiver = false
        }
    }
}