package com.bihe0832.android.lib.http.common.core;

import android.net.Network;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * HTTP 请求类
 */
public class HTTPConnection extends BaseConnection {

    private HttpURLConnection mConn = null;

    public HTTPConnection(String url, Network network) {
        super();
        try {
            if (network == null) {
                mConn = (HttpURLConnection) new URL(url).openConnection();
            } else {
                mConn = (HttpURLConnection) network.openConnection(new URL(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpURLConnection getURLConnection() {
        return mConn;
    }
}
