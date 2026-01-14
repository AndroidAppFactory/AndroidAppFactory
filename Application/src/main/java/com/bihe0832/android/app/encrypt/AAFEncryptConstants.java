package com.bihe0832.android.app.encrypt;

import com.bihe0832.android.lib.utils.encrypt.aes.AESUtils;
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils;

/**
 * AAF 加密常量定义
 *
 * 定义加密相关的常量配置，包括 RSA 和 AES 的加密模式、初始化向量等
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/8/28.
 */
public class AAFEncryptConstants {

    /** RSA 加密模式：PKCS#1 填充 */
    public static final String RSA_MOD = RSAUtils.MOD_PKCS_1;

    /** AES 加密模式：CBC 模式 + PKCS7 填充 */
    public static final String AES_MOD = AESUtils.MODE_CBC_PKCS7;

    /** AES 加密的初始化向量 (IV) */
    public static final byte[] IV_BYTES = new byte[]{0x4D, 0x4E, 0x41, 0x40, 0x32, 0x30, 0x31, 0x37, 0x47, 0x4F, 0x48,
            0x45, 0x41, 0x44, 0x21, 0x21};
}
