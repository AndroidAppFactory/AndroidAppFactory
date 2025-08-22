package com.bihe0832.android.base.compose.debug.encrypt;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils;
import java.security.PrivateKey;

public class AAFSecretEncrypt {

    public static final String TAG = "AAFSecretEncrypt";
    public static final String RSA_PUB_KEY_NAME = "demo_public_key.pem";
    public static final String RSA_PRI_KEY_NAME = "demo_private_key.pem";

    public static final String DEMO_AES_KEY_ALIAS = "aes";
    public static final String DEMO_RSA_KEY_ALIAS = "rsa";

    public static final String DEMO_AES_KEY = "1234567890ASCDEF1234567890ASCDEF";

    /**
     * 读取Assets的RSA 私钥
     */
    public static PrivateKey getRSAPrivateKeyFormAssets() {
        String content = FileUtils.INSTANCE.getAssetFileContent(ZixieContext.INSTANCE.getApplicationContext(),
                RSA_PRI_KEY_NAME);
        return RSAUtils.pemStringToRSAPrivateKey(content);
    }
}