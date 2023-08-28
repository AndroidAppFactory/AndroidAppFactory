package com.bihe0832.android.app.encrypt;

import android.content.Context;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.utils.encrypt.AESEncryptResult;
import com.bihe0832.android.lib.utils.encrypt.AESUtils;
import com.bihe0832.android.lib.utils.encrypt.RSAUtils;
import com.bihe0832.android.lib.utils.keystore.AESKeyStoreUtils;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Summary
 *
 * @author hardyshi code@bihe0832.com
 *         Created on 2023/8/28.
 *         推荐使用场景：
 *         1. 登录数据等本地加密，本地解密的数据，可以使用：aesEncrypt && aesDecrypt
 *         2. 前后台数据交互的数据加密，推荐使用：encryptDataWithKeyStore
 *         3. 若是本地提前硬编码的加密数据，建议本地落地AES加密后的数据，然后 AES 加密的秘钥后台下发或者使用其他方式保存
 */
public class AAFEncrypt {

    /**
     * 读取Assets的RSA 公钥
     */
    public static PublicKey getRSAPublicKeyFormAssets(Context context, String fileName) {
        String content = FileUtils.INSTANCE.getAssetFileContent(context, fileName);
        return RSAUtils.pemStringToRSAPublicKey(content);
    }

    /**
     * 读取Assets的RSA 私钥
     */
    public static PrivateKey getRSAPrivateKeyFormAssets(Context context, String fileName) {
        String content = FileUtils.INSTANCE.getAssetFileContent(context, fileName);
        return RSAUtils.pemStringToRSAPrivateKey(content);
    }

    /**
     * 利用Keystore自动生成的AES秘钥加密数据
     */
    public static AESEncryptResult aesEncrypt(Context context, String aesKeyAlias, String data) {
        SecretKey key = AESKeyStoreUtils.getAESKeyByKeystore(context, aesKeyAlias);
        return AESUtils.doAESEncrypt(key, null, Cipher.ENCRYPT_MODE, data.getBytes());
    }

    /**
     * 利用Keystore自动生成的AES秘钥解密数据
     */
    public static String aesDecrypt(Context context, String aesKeyAlias, byte[] ivParaBytes, byte[] data) {
        SecretKey key = AESKeyStoreUtils.getAESKeyByKeystore(context, aesKeyAlias);
        AESEncryptResult result = AESUtils.doAESEncrypt(key, ivParaBytes, Cipher.DECRYPT_MODE, data);
        if (result != null) {
            return new String(result.result);
        }
        return null;
    }

    /**
     * 利用系统自动生成秘钥AES加密数据，并用公钥加密AES秘钥
     */
    public static AAFEncryptResult encryptDataWithKeyStore(Context context, String rsaKeyName, String data) {
        SecretKey secretKey = AESKeyStoreUtils.getAESKeyByKeystore();
        return encryptDataWithKeyStore(context, secretKey, rsaKeyName, data);
    }

    /**
     * 利用秘钥AES加密数据，并用公钥加密AES秘钥
     */
    public static AAFEncryptResult encryptDataWithKeyStore(Context context, String aesKey, String rsaKeyName,
            String data) {
        SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(), "AES");
        return encryptDataWithKeyStore(context, secretKey, rsaKeyName, data);
    }

    public static AAFEncryptResult encryptDataWithKeyStore(Context context, SecretKey secretKey, String rsaKeyName,
            String data) {
        AESEncryptResult result = AESUtils.doAESEncrypt(secretKey, null, Cipher.ENCRYPT_MODE, data.getBytes());
        PublicKey rsaKey = getRSAPublicKeyFormAssets(context, rsaKeyName);
        byte[] key = RSAUtils.encryptSecretKeyWithRSAPublicKey(rsaKey, secretKey);
        return new AAFEncryptResult(key, result.iv, result.result);
    }
}
