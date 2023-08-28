package com.bihe0832.android.lib.utils.keystore;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.encrypt.AESEncryptResult;
import com.bihe0832.android.lib.utils.encrypt.AESUtils;
import com.bihe0832.android.lib.utils.time.DateUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Summary
 *
 * @author hardyshi code@bihe0832.com
 *         Created on 2023/8/25.
 *         Description:
 */
public class AESKeyStoreUtils {

    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    public static boolean hasKey(String keyAlias) {
        try {
            // 加载一个AndroidKeyStore类型的KeyStore，貌似是定死的类型。
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return keyStore.containsAlias(keyAlias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void buildKey(Context context, String keyAlias) {
        try {
            // 先获取密钥对生成器，采用RSA算法，AndroidKeyStore类型
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            // 加密算法的相关参数
            AlgorithmParameterSpec spec;
            // 密钥的有效起止时间，从现在到999年后，时间大家自己定
            long todayStart = DateUtil.getDayStartTimestamp(System.currentTimeMillis());
            Date start = new Date(todayStart);
            Date end = new Date(todayStart + DateUtil.MILLISECOND_OF_YEAR * 999);
            // 生成加密参数，从Android6.0（API23）开始有所不同
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 根据密钥别名生成加密参数，提供加密和解密操作
                spec = new KeyGenParameterSpec.Builder(keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA512)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setKeySize(256)
                        .setRandomizedEncryptionRequired(true)
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
            keyGenerator.init(spec);
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    // 加密方法
    public static AESEncryptResult doEncrypt(Context context, String aesMode, String keyAlias, byte[] ivParaBytes,
            int mode, byte[] data) {
        if (!hasKey(keyAlias)) {
            buildKey(context, keyAlias);
        }
        try {
            return AESUtils.doAESEncrypt(aesMode, getAESKeyByKeystore(context, keyAlias), ivParaBytes, mode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 加密方法
    public static AESEncryptResult encrypt(Context context, String aesMode, String keyAlias, byte[] data) {
        return doEncrypt(context, aesMode, keyAlias, null, Cipher.ENCRYPT_MODE, data);
    }

    // 解密方法
    public static byte[] decrypt(Context context, String aesMode, String keyAlias, byte[] ivParaBytes, byte[] data) {
        AESEncryptResult result = doEncrypt(context, aesMode, keyAlias, ivParaBytes, Cipher.DECRYPT_MODE, data);
        if (result != null) {
            return result.result;
        }
        return null;
    }


    // 仅能用于本地加密
    public static SecretKey getAESKeyByKeystore(Context context, String keyAlias) {
        if (!hasKey(keyAlias)) {
            buildKey(context, keyAlias);
        }
        try {
            // 获取"AndroidKeyStore"类型的KeyStore，加载
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            // 拿到密钥别名对应的Entry
            KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
            if (entry instanceof KeyStore.SecretKeyEntry) {
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null);
                SecretKey secretKey = secretKeyEntry.getSecretKey();
                return secretKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //可以随意使用
    public static SecretKey getAESKeyByKeystore() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(256, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

}
