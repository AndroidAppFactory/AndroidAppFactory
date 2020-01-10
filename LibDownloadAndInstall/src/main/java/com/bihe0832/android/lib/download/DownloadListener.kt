package com.bihe0832.android.lib.download

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-09.
 * Description: Description
 *
 */
interface DownloadListener {
    fun onProgress(total: Long, cur: Long)
    fun onSuccess(finalFileName: String)
    fun onError(error: Int, errmsg: String)
}
