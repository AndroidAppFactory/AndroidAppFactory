package com.bihe0832.android.lib.download.core.dabase;

import android.content.Context
import android.database.Cursor
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadPartInfo

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/6/12.
 * Description: Description
 */
object DownloadInfoDBManager {

    public const val TAG = "DownloadInfoDBManager"
    private var mApplicationContext: Context? = null
    private var commonDBHelperInstance: DownloadInfoDBHelper? = null
    fun init(context: Context) {
        mApplicationContext = context
        if (commonDBHelperInstance == null) {
            commonDBHelperInstance = DownloadInfoDBHelper(mApplicationContext)
            DownloadInfoTableModel.initData(getDownloadInfoDBHelper())
        }
    }

    fun getApplicationContext(): Context? {
        return mApplicationContext
    }
    @Synchronized
    private fun getDownloadInfoDBHelper(): DownloadInfoDBHelper? {
        return commonDBHelperInstance
    }

    fun hasDownloadPartInfo(downloadID: Long, showLines: Boolean): Boolean {
        return DownloadPartInfoTableModel.hasData(getDownloadInfoDBHelper(), downloadID, showLines)
    }

    fun getDownloadInfo(url: String, downloadActionKey: String): DownloadItem? {
        return DownloadInfoTableModel.getDownloadInfo(getDownloadInfoDBHelper(), url, downloadActionKey)
    }

    fun getDownloadInfoFromPackageName(packageName: String): DownloadItem? {
        return DownloadInfoTableModel.getDownloadInfoFromPackageName(getDownloadInfoDBHelper(), packageName)
    }

    fun saveDownloadInfo(item: DownloadItem): Boolean {
        return DownloadInfoTableModel.saveData(getDownloadInfoDBHelper(), item)
    }

    fun getFinishedBefore(downloadID: Long): Long {
        return DownloadPartInfoTableModel.getFinished(getDownloadInfoDBHelper(), downloadID)
    }


    fun saveDownloadPartInfo(downloadPartInfo: DownloadPartInfo): Boolean {
        return DownloadPartInfoTableModel.saveData(
            getDownloadInfoDBHelper(),
            downloadPartInfo.downloadPartID,
            downloadPartInfo.partID,
            downloadPartInfo.downloadID,
            downloadPartInfo.partStart,
            downloadPartInfo.partEnd,
            downloadPartInfo.partFinished
        )
    }

    fun updateDownloadFinished(downloadPartID: String, hasdownloadLength: Long): Boolean {
        return DownloadPartInfoTableModel.updateDownloadFinished(
            getDownloadInfoDBHelper(), downloadPartID, hasdownloadLength
        )
    }

    fun getDownloadPartInfo(downloadID: Long): Cursor {
        return DownloadPartInfoTableModel.getDownloadPartInfo(getDownloadInfoDBHelper(), downloadID)
    }

    fun clearDownloadPartByID(downloadID: Long): Boolean {
        return DownloadPartInfoTableModel.clearData(getDownloadInfoDBHelper(), downloadID)
    }
    fun clearDownloadInfoByID(downloadID: Long): Boolean {
        return DownloadInfoTableModel.clearData(getDownloadInfoDBHelper(), downloadID)
    }
}