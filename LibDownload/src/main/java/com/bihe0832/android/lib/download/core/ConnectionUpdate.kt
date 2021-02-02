package com.bihe0832.android.lib.download.core

import android.os.Build
import java.net.HttpURLConnection

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/6/19.
 * Description: Description
 *
 */

fun HttpURLConnection.upateRequestInfo() {
    connectTimeout = 30000
    readTimeout = 15000
    requestMethod = "GET"
    setRequestProperty("Content-Type", "application/octet-stream; charset=UTF-8")
    setRequestProperty("Accept-Encoding", "identity")
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
        setRequestProperty("Connection", "close")
    }
}