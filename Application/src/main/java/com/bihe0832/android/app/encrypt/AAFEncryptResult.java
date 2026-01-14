package com.bihe0832.android.app.encrypt;

import com.bihe0832.android.lib.utils.encrypt.aes.AESEncryptResult;

/**
 * AAF 加密结果封装类
 *
 * 继承自 AESEncryptResult，在 AES 加密结果基础上增加了 RSA 加密后的密钥数据
 * 用于混合加密场景：AES 加密数据，RSA 加密 AES 密钥
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/8/28.
 */
public class AAFEncryptResult extends AESEncryptResult {

    /** RSA 加密后的 AES 密钥数据 */
    public byte[] keyEncryptData;

    /**
     * 构造加密结果
     *
     * @param keyEncryptData RSA 加密后的 AES 密钥
     * @param iv AES 加密的初始化向量
     * @param result AES 加密后的数据
     */
    public AAFEncryptResult(byte[] keyEncryptData, byte[] iv, byte[] result) {
        super(iv, result);
        this.keyEncryptData = keyEncryptData;
    }
}
