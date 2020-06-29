package com.bihe0832.android.http.common;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class MyX509TrustManager implements X509TrustManager {
    X509TrustManager sunJSSEX509TrustManager;

    private static volatile MyX509TrustManager instance;

    public static MyX509TrustManager getInstance () {
        if (instance == null) {
            synchronized (MyX509TrustManager.class) {
                if (instance == null) {
                    try {
                        instance = new MyX509TrustManager();
                    }catch (Exception e){
                        e.printStackTrace();
                        instance = null;
                    }
                }
            }
        }
        return instance;
    }


    private MyX509TrustManager() throws Exception {
        // create a "default" JSSE X509TrustManager.
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
        } catch (Exception e){
            e.printStackTrace();
        }
        TrustManager tms [] = {};
        if (ks != null){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream("trustedCerts");
                ks.load(fis, "passphrase".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
                tmf.init(ks);
                tms = tmf.getTrustManagers();
            } finally {
                if (fis != null) {
                    fis.close();
                }
                fis = null;
            }
        }else{
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            tms =  tmf.getTrustManagers();

        }
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                sunJSSEX509TrustManager = (X509TrustManager) tms[i];
                return;
            }
        }
        throw new Exception("Couldn't initialize");
    }
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return sunJSSEX509TrustManager.getAcceptedIssuers();
    }
}