package com.bihe0832.android.lib.download

import android.app.DownloadManager
import java.util.ArrayList

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-09.
 * Description: Description
 *
 */

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