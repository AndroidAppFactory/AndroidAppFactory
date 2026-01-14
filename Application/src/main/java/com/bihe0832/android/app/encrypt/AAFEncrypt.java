package com.bihe0832.android.app.encrypt;

import android.content.Context;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.utils.encrypt.aes.AESEncryptResult;
import com.bihe0832.android.lib.utils.encrypt.aes.AESUtils;
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils;
import com.bihe0832.android.lib.utils.keystore.AESKeyStoreUtils;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AAF 加密工具类
 *
 * 提供数据加密解密功能，支持：
 * - AES 对称加密（使用 Keystore 管理密钥）
 * - RSA 非对称加密（用于加密 AES 密钥）
 * - 混合加密方案（AES 加密数据 + RSA 加密密钥）
 *
 * 推荐使用场景：
 * 1. 登录数据等本地加密，本地解密的数据，可以使用：aesEncrypt && aesDecrypt
 * 2. 前后台数据交互的数据加密，推荐使用：encryptDataWithKeyStore
 * 3. 若是本地提前硬编码的加密数据，建议本地落地 AES 加密后的数据，然后 AES 加密的秘钥后台下发或者使用其他方式保存
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/8/28.
 */
public class AAFEncrypt {

    /**
     * 从 Assets 读取 RSA 公钥
     *
     * @param context 上下文
     * @param fileName Assets 中的公钥文件名
     * @return RSA 公钥对象
     */
    public static PublicKey getRSAPublicKeyFormAssets(Context context, String fileName) {
        String content = FileUtils.INSTANCE.getAssetFileContent(context, fileName);
        return RSAUtils.pemStringToRSAPublicKey(content);
    }

    /**
     * 从 Assets 读取 RSA 私钥
     *
     * @param context 上下文
     * @param fileName Assets 中的私钥文件名
     * @return RSA 私钥对象
     */
    public static PrivateKey getRSAPrivateKeyFormAssets(Context context, String fileName) {
        String content = FileUtils.INSTANCE.getAssetFileContent(context, fileName);
        return RSAUtils.pemStringToRSAPrivateKey(content);
    }

    /**
     * 使用 Keystore 自动生成的 AES 密钥加密数据
     *
     * @param context 上下文
     * @param aesKeyAlias Keystore 中的密钥别名
     * @param data 待加密的数据
     * @return 加密结果，包含 IV 和密文
     */
    public static AESEncryptResult aesEncrypt(Context context, String aesKeyAlias, String data) {
        SecretKey key = AESKeyStoreUtils.getAESKeyByKeystore(context, aesKeyAlias);
        return AESUtils.doAESEncrypt(AAFEncryptConstants.AES_MOD, key, null, Cipher.ENCRYPT_MODE, data.getBytes());
    }

    /**
     * 使用 Keystore 自动生成的 AES 密钥解密数据
     *
     * @param context 上下文
     * @param aesKeyAlias Keystore 中的密钥别名
     * @param ivParaBytes 加密时使用的 IV
     * @param data 待解密的数据
     * @return 解密后的明文字符串
     */
    public static String aesDecrypt(Context context, String aesKeyAlias, byte[] ivParaBytes, byte[] data) {
        SecretKey key = AESKeyStoreUtils.getAESKeyByKeystore(context, aesKeyAlias);
        AESEncryptResult result = AESUtils.doAESEncrypt(AAFEncryptConstants.AES_MOD, key, ivParaBytes,
                Cipher.DECRYPT_MODE,
                data);
        if (result != null) {
            return new String(result.result);
        }
        return null;
    }

    /**
     * 使用系统自动生成的 AES 密钥加密数据，并用 RSA 公钥加密 AES 密钥
     *
     * @param context 上下文
     * @param rsaKeyName Assets 中的 RSA 公钥文件名
     * @param data 待加密的数据
     * @return 加密结果，包含 RSA 加密的 AES 密钥、IV 和密文
     */
    public static AAFEncryptResult encryptDataWithKeyStore(Context context, String rsaKeyName, String data) {
        SecretKey secretKey = AESKeyStoreUtils.getAESKeyByKeystore();
        return encryptDataWithKeyStore(context, secretKey, rsaKeyName, data);
    }

    /**
     * 使用指定的 AES 密钥加密数据，并用 RSA 公钥加密 AES 密钥
     *
     * @param context 上下文
     * @param aesKey AES 密钥字符串
     * @param rsaKeyName Assets 中的 RSA 公钥文件名
     * @param data 待加密的数据
     * @return 加密结果，包含 RSA 加密的 AES 密钥、IV 和密文
     */
    public static AAFEncryptResult encryptDataWithKeyStore(Context context, String aesKey, String rsaKeyName,
            String data) {
        SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(), "AES");
        return encryptDataWithKeyStore(context, secretKey, rsaKeyName, data);
    }

    /**
     * 使用 SecretKey 加密数据，并用 RSA 公钥加密 AES 密钥
     *
     * @param context 上下文
     * @param secretKey AES 密钥对象
     * @param rsaKeyName Assets 中的 RSA 公钥文件名
     * @param data 待加密的数据
     * @return 加密结果，包含 RSA 加密的 AES 密钥、IV 和密文
     */
    public static AAFEncryptResult encryptDataWithKeyStore(Context context, SecretKey secretKey, String rsaKeyName,
            String data) {
        AESEncryptResult result = AESUtils.doAESEncrypt(AAFEncryptConstants.AES_MOD, secretKey, null,
                Cipher.ENCRYPT_MODE,
                data.getBytes());
        PublicKey rsaKey = getRSAPublicKeyFormAssets(context, rsaKeyName);
        byte[] key = RSAUtils.encryptSecretKeyWithRSAPublicKey(AAFEncryptConstants.RSA_MOD, rsaKey, secretKey);
        return new AAFEncryptResult(key, result.iv, result.result);
    }
}
