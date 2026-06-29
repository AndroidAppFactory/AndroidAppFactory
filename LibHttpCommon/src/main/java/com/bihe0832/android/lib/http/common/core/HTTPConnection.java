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
    private String mOriginalUrl = null;

    public HTTPConnection(String url, Network network) {
        super();
        initConnection(url, network);
    }

    /**
     * 带原始 URL 的构造函数（HTTPDNS 场景）
     *
     * @param url         DNS 解析后的 URL（IP 替换后的地址）
     * @param network     网络接口
     * @param originalUrl 原始 URL（含域名，用于设置 Host 请求头）
     */
    public HTTPConnection(String url, Network network, String originalUrl) {
        super();
        this.mOriginalUrl = originalUrl;
        initConnection(url, network);
    }

    private void initConnection(String url, Network network) {
        try {
            if (network == null) {
                mConn = (HttpURLConnection) new URL(url).openConnection();
            } else {
                mConn = (HttpURLConnection) network.openConnection(new URL(url));
            }
            // 如果构造时传入了原始 URL，设置 Host 请求头
            if (mOriginalUrl != null) {
                try {
                    String host = new URL(mOriginalUrl).getHost();
                    if (host != null && !host.isEmpty()) {
                        mConn.setRequestProperty("Host", host);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
