package com.bihe0832.android.http.common;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * HTTPS 请求类
 */
public class HTTPSConnection extends BaseConnection {

    private HttpsURLConnection mConn = null;
    private SSLContext mSSLContext = null;

    public HTTPSConnection(String url) {
        super();
        TrustManager tm = null;
        try {
            tm = MyX509TrustManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[] { tm }, null);
            mConn = (HttpsURLConnection)new URL(url).openConnection();
            mConn.setDefaultSSLSocketFactory(mSSLContext.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected HttpURLConnection getURLConnection() {
        return mConn;
    }

}
