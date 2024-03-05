package com.bihe0832.android.lib.utils.keystore;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.time.DateUtil;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/8/25.
 *         Description:
 */
public class RSAKeyStoreUtils {

    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    public static boolean hasKey(String keyAlias) {
        try {
            // 加载一个AndroidKeyStore类型的KeyStore，貌似是定死的类型。
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return keyStore.containsAlias(keyAlias);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void buildKey(Context context, String keyAlias) {
        try {
            // 先获取密钥对生成器，采用RSA算法，AndroidKeyStore类型
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
            // 加密算法的相关参数
            AlgorithmParameterSpec spec;
            // 密钥的有效起止时间，从现在到999年后，时间大家自己定
            long todayStart = DateUtil.getDayStartTimestamp(System.currentTimeMillis());
            Date start = new Date(todayStart);
            Date end = new Date(todayStart + DateUtil.MILLISECOND_OF_YEAR * 999);
            // 生成加密参数，从Android6.0（API23）开始有所不同
            if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.M) {
                // 根据密钥别名生成加密参数，提供加密和解密操作
                spec = new KeyGenParameterSpec.Builder(keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        // SHA (Secure Hash Algorithm，译作安全散列算法) 是美国国家安全局 (NSA) 设计，美国国家标准与技术研究院 (NIST) 发布的一系列密码散列函数。 感兴趣的同学可以了解一下
                        .setDigests(KeyProperties.DIGEST_SHA512)
                        // 填充模式，一般RSA加密常用PKCS1Padding模式
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        // 限定密钥有效期起止时间
                        .setCertificateNotBefore(start)
                        .setCertificateNotAfter(end)
                        .build();
            } else {
                // 相对于Android6.0（API23）的方式，这种稍显简单
                spec = new KeyPairGeneratorSpec.Builder(context.getApplicationContext())
                        .setAlias(keyAlias)
                        // 设置用于生成的密钥对的自签名证书的主题，X500Principal这东西不认识，资料真少，看的头大
                        .setSubject(new X500Principal("CN=" + keyAlias))
                        // 设置用于生成的密钥对的自签名证书的序列号，从BigInteger取即可
                        .setSerialNumber(BigInteger.TEN)
                        // 限定密钥有效期起止时间
                        .setStartDate(start)
                        .setEndDate(end)
                        .build();
            }
            // 用加密参数初始化密钥对生成器，生成密钥对
            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    // 加密方法
    public static byte[] doEncrypt(Context context, String mod, String keyAlias, byte[] data, int mode) {
        if (!hasKey(keyAlias)) {
            buildKey(context, keyAlias);
        }
        try {
            // 获取"AndroidKeyStore"类型的KeyStore，加载
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            // 拿到密钥别名对应的Entry
            KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);
                if (Cipher.ENCRYPT_MODE == mode) {
                    PublicKey publicKey = keyEntry.getCertificate().getPublicKey();
                    return RSAUtils.encrypt(mod, publicKey, data);
                } else {
                    PrivateKey privateKey = keyEntry.getPrivateKey();
                    return RSAUtils.decrypt(mod, privateKey, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 加密方法
    public static byte[] encrypt(Context context, String mod, String keyAlias, byte[] data) {
        return doEncrypt(context, mod, keyAlias, data, Cipher.ENCRYPT_MODE);
    }

    // 解密方法
    public static byte[] decrypt(Context context, String mod, String keyAlias, byte[] data) {
        return doEncrypt(context, mod, keyAlias, data, Cipher.DECRYPT_MODE);
    }
}
