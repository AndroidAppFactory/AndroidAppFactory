package com.bihe0832.android.app.encrypt;

import com.bihe0832.android.lib.utils.encrypt.AESUtils;
import com.bihe0832.android.lib.utils.encrypt.RSAUtils;

/**
 * Summary
 *
 * @author hardyshi code@bihe0832.com
 *         Created on 2023/8/28.
 *         Description:
 */
public class AAFEncryptConstants {

    public static final String RSA_PUB_KEY_NAME = "demo_public_key.pem";
    public static final String RSA_PRI_KEY_NAME = "demo_private_key.pem";

    public static final String RSA_MOD = RSAUtils.MOD_OAEP;
    public static final String AES_MOD = AESUtils.MODE_CBC_PKCS7;

    public static final byte[] IV_BYTES = new byte[]{0x4D, 0x4E, 0x41, 0x40, 0x32, 0x30, 0x31, 0x37, 0x47, 0x4F, 0x48,
            0x45, 0x41, 0x44, 0x21, 0x21};
}
