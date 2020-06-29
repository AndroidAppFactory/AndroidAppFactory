package com.bihe0832.android.http.common;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * HTTP 请求类
 *
 */
public class HTTPConnection extends BaseConnection {

    private HttpURLConnection mConn = null;
    public HTTPConnection(String url) {
        super();
        try {
            mConn = (HttpURLConnection)new URL(url).openConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected HttpURLConnection getURLConnection() {
        return mConn;
    }
}
