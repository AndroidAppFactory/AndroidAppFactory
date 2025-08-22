package com.bihe0832.android.base.compose.debug.encrypt;

import android.content.Context
import android.util.Base64
import com.bihe0832.android.app.encrypt.AAFEncryptConstants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.utils.keystore.AESKeyStoreUtils
import com.bihe0832.android.lib.utils.keystore.RSAKeyStoreUtils
import java.util.Arrays

internal fun androidRSAEncrypt(context: Context) {
    val data = TextFactoryUtils.getRandomString(16)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "系统自带秘钥 RSA ${AAFEncryptConstants.RSA_MOD} 加密原始数据 ：$data")
    val encryptResult =
        RSAKeyStoreUtils.encrypt(
            context,
            AAFEncryptConstants.RSA_MOD,
            AAFSecretEncrypt.DEMO_RSA_KEY_ALIAS,
            data.toByteArray(),
        )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "系统自带秘钥 RSA 内容加密 ：${Arrays.toString(encryptResult)}")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "系统自带秘钥 RSA 内容加密 ：${String(Base64.encode(encryptResult, Base64.DEFAULT))}",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    RSAKeyStoreUtils.decrypt(
        context,
        AAFEncryptConstants.RSA_MOD,
        AAFSecretEncrypt.DEMO_RSA_KEY_ALIAS,
        encryptResult,
    )?.let {
        ZLog.d(AAFSecretEncrypt.TAG, "系统自带秘钥 RSA 内容直接解密 ：${String(it)}")
    }
    encryptResult?.let {
        RSAKeyStoreUtils.decrypt(
            context,
            AAFEncryptConstants.RSA_MOD,
            AAFSecretEncrypt.DEMO_RSA_KEY_ALIAS,
            Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT),
        )?.let {
            ZLog.d(AAFSecretEncrypt.TAG, "系统自带秘钥 RSA 内容模拟解密 ：${String(it)}")
        }
    }
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
}

internal fun androidAESEncrypt(context: Context) {
    val data = TextFactoryUtils.getRandomString(16)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "系统自带秘钥 AES ${AAFEncryptConstants.AES_MOD} 加密原始数据 ：$data")
    val encryptResult =
        AESKeyStoreUtils.encrypt(
            context,
            AAFEncryptConstants.AES_MOD,
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
            data.toByteArray(),
        )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "系统自带秘钥 AES ${AAFEncryptConstants.AES_MOD} 内容加密 ：${Arrays.toString(encryptResult.result)}",
    )
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "系统自带秘钥 AES ${AAFEncryptConstants.AES_MOD} 内容加密 ：${
            String(
                Base64.encode(
                    encryptResult.result,
                    Base64.DEFAULT,
                ),
            )
        }",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "系统自带秘钥 AES ${AAFEncryptConstants.AES_MOD} 内容直接解密 ：${
            String(
                AESKeyStoreUtils.decrypt(
                    context,
                    AAFEncryptConstants.AES_MOD,
                    AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
                    encryptResult.iv,
                    encryptResult.result,
                ),
            )
        }",
    )
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "系统自带秘钥 AES ${AAFEncryptConstants.AES_MOD} 内容模拟解密 ：${
            String(
                AESKeyStoreUtils.decrypt(
                    context,
                    AAFEncryptConstants.AES_MOD,
                    AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
                    encryptResult.iv,
                    Base64.decode(Base64.encode(encryptResult.result, Base64.DEFAULT), Base64.DEFAULT),
                ),
            )
        }",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
}
