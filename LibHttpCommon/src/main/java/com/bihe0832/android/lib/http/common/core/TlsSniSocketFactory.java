package com.bihe0832.android.lib.http.common.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * 支持自定义 SNI Hostname 的 SSLSocketFactory
 *
 * 用于 HTTPDNS 场景：URL 被替换为 IP 地址后，
 * SSL 握手时需要使用原始域名作为 SNI（Server Name Indication）。
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
public class TlsSniSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;
    private final String sniHostname;

    public TlsSniSocketFactory(SSLSocketFactory delegate, String sniHostname) {
        this.delegate = delegate;
        this.sniHostname = sniHostname;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return injectSniHostname(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return injectSniHostname(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return injectSniHostname(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return injectSniHostname(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return injectSniHostname(delegate.createSocket(address, port, localAddress, localPort));
    }

    /**
     * 在 SSL Socket 上注入 SNI Hostname 并关闭端点识别
     *
     * Android API 28+ 默认启用 endpointIdentificationAlgorithm="HTTPS"，
     * 在 IP 直连场景下会导致 SSLPeerUnverifiedException。
     * 关闭后由 HttpsURLConnection 的 HostnameVerifier 负责域名校验。
     */
    private Socket injectSniHostname(Socket socket) {
        if (socket instanceof SSLSocket && sniHostname != null) {
            try {
                SSLSocket sslSocket = (SSLSocket) socket;
                javax.net.ssl.SSLParameters params = sslSocket.getSSLParameters();
                params.setServerNames(java.util.Collections.singletonList(
                        new javax.net.ssl.SNIHostName(sniHostname)));
                // 关闭端点识别，避免 IP 直连时 SSL 握手阶段报 SSLPeerUnverifiedException
                params.setEndpointIdentificationAlgorithm(null);
                sslSocket.setSSLParameters(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return socket;
    }
}
