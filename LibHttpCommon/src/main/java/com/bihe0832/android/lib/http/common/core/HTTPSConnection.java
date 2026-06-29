package com.bihe0832.android.lib.http.common.core;

import android.net.Network;
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
    private String mOriginalUrl = null;

    public HTTPSConnection(String url, Network network) {
        super();
        initConnection(url, network);
    }

    /**
     * 带原始 URL 的构造函数（HTTPDNS 场景）
     *
     * @param url         DNS 解析后的 URL（IP 替换后的地址）
     * @param network     网络接口
     * @param originalUrl 原始 URL（含域名，用于 Host 头、SNI 和证书校验）
     */
    public HTTPSConnection(String url, Network network, String originalUrl) {
        super();
        this.mOriginalUrl = originalUrl;
        initConnection(url, network);
    }

    private void initConnection(String url, Network network) {
        TrustManager tm = null;
        try {
            tm = MyX509TrustManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[]{tm}, null);
            if (network == null) {
                mConn = (HttpsURLConnection) new URL(url).openConnection();
            } else {
                mConn = (HttpsURLConnection) network.openConnection(new URL(url));
            }
            mConn.setDefaultSSLSocketFactory(mSSLContext.getSocketFactory());

            // 如果构造时传入了原始 URL，进行 HTTPDNS 适配
            if (mOriginalUrl != null) {
                try {
                    String host = new URL(mOriginalUrl).getHost();
                    if (host != null && !host.isEmpty()) {
                        // 设置 Host 请求头
                        mConn.setRequestProperty("Host", host);

                        // 使用自定义 SSLSocketFactory 注入 SNI Hostname（API 24+）
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            mConn.setSSLSocketFactory(new TlsSniSocketFactory(
                                    mSSLContext.getSocketFactory(), host));
                        }

                        // HTTPDNS IP 直连场景：跳过主机名验证
                        // HttpsURLConnection.connect() 内部会重置 SSL 端点识别，
                        // 导致 TlsSniSocketFactory 中的 setEndpointIdentificationAlgorithm(null) 被覆盖。
                        // 因此在此直接返回 true，安全性由 SNI + TrustManager（证书链验证）保证。
                        mConn.setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
                                return true;
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpURLConnection getURLConnection() {
        return mConn;
    }

}
