package com.bihe0832.android.lib.download.manager

import android.content.Context
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-10.
 * Description: Description
 *
 */

class DownloadByHttp : DownloadWrapper() {

    var mCurrentConnectList = HashMap<String, HttpURLConnection>()

    override fun goDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?) {
        mCurrentDownloadList[info.downloadURL] = info
        ThreadManager.getInstance().start {
            var count: Int
            var input: BufferedInputStream? = null
            var finalFileName = applicationContext!!.getExternalFilesDir(DOWNLOAD_PATH).absolutePath + "/" + "temp_" + System.currentTimeMillis() + "_" + info.fileName
            val output = FileOutputStream(finalFileName)
            try {
                val url = URL(info.downloadURL)
                val conection = url.openConnection() as HttpURLConnection
                mCurrentConnectList[info.downloadURL] = conection
                conection.connect()
                val lenghtOfFile = conection.contentLength
                input = BufferedInputStream(url.openStream(), 8192)

                val data = ByteArray(1024)
                var total: Long = 0

                do {
                    count = input.read(data)
                    if (count == -1) break
                    total += count.toLong()
                    mCurrentDownloadList[info.downloadURL]?.let { downloadItem ->
                        downloadItem.downloadNotifyListenerList.forEach {
                            it.onProgress(lenghtOfFile.toLong(),total)
                        }
                    }
                    output.write(data, 0, count)
                } while (true)
                mCurrentDownloadList[info.downloadURL]?.let {
                    notifyDownload(it, finalFileName)
                }
                mCurrentDownloadList.remove(info.downloadURL)
                output.flush()
            } catch (e: Throwable) {
                e.printStackTrace()
                mCurrentDownloadList[info.downloadURL]?.let { downloadItem ->
                    downloadItem.downloadNotifyListenerList.forEach {
                        it.onError(-7, "download with exception")
                    }
                }
                cancleDownload(info.downloadURL)
            } finally {
                try {
                    output.close()
                    input?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun cancleDownload(url: String) {
        mCurrentConnectList[url]?.let {
            it.disconnect()
        }
        stopDownload(url)
    }

    override fun onDestory() {

    }
}