package com.bihe0832.android.base.compose.debug.encrypt;

import android.content.Context
import android.util.Base64
import com.bihe0832.android.app.encrypt.AAFEncrypt
import com.bihe0832.android.app.encrypt.AAFEncryptConstants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.utils.encrypt.aes.AESUtils
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils
import java.util.Arrays

internal fun saveAES(context: Context) {
    val data = TextFactoryUtils.getRandomString(16)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "AES ${AAFEncryptConstants.AES_MOD} 内容加密原始数据 ：$data")
    val encryptResult = AAFEncrypt.aesEncrypt(context, AAFSecretEncrypt.DEMO_AES_KEY_ALIAS, data)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "AES ${AAFEncryptConstants.AES_MOD} 内容加密向量 ：${Arrays.toString(encryptResult.iv)}",
    )
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "AES ${AAFEncryptConstants.AES_MOD} 内容加密结果 ：${
            String(
                Base64.encode(
                    encryptResult.result,
                    Base64.DEFAULT,
                ),
            )
        }",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    encryptResult?.let {
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES ${AAFEncryptConstants.AES_MOD} 内容模拟解密 ：${
                AAFEncrypt.aesDecrypt(
                    context!!,
                    AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
                    encryptResult.iv,
                    encryptResult.result,
                )
            }",
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    }
}

internal fun saveData(context: Context) {
    val data = TextFactoryUtils.getRandomString(16)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "内容加密 原始数据 ：$data")
    val encryptResult = AAFEncrypt.encryptDataWithKeyStore(context, AAFSecretEncrypt.RSA_PUB_KEY_NAME, data)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "内容 AES ${AAFEncryptConstants.AES_MOD}  加密 Data：${
            String(
                Base64.encode(
                    encryptResult.result,
                    Base64.DEFAULT,
                ),
            )
        }",
    )
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "内容 AES ${AAFEncryptConstants.AES_MOD}  加密 IV：${
            String(
                Base64.encode(
                    encryptResult.iv,
                    Base64.DEFAULT,
                ),
            )
        }",
    )
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "内容加密Key RSA ${AAFEncryptConstants.RSA_MOD} 加密后：\n${
            String(
                Base64.encode(
                    encryptResult.keyEncryptData,
                    Base64.DEFAULT,
                ),
            )
        }",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    val key = RSAUtils.decrypt(
        AAFEncryptConstants.RSA_MOD,
        AAFSecretEncrypt.getRSAPrivateKeyFormAssets(),
        encryptResult.keyEncryptData,
    )
    val result = AESUtils.decrypt(AAFEncryptConstants.AES_MOD, key, encryptResult.iv, encryptResult.result)
    ZLog.d(AAFSecretEncrypt.TAG, "内容 AES ${AAFEncryptConstants.AES_MOD} 解密 Data：${String(result)}")
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
}

internal fun checkSig(context: Context) {
    var data = TextFactoryUtils.getRandomString(16)
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(AAFSecretEncrypt.TAG, "数据公私钥签名验证 原始数据 ：\"$data\"")
    val encryptResult = RSAUtils.signDataWithRSAPrivateKey(
        AAFSecretEncrypt.getRSAPrivateKeyFormAssets(),
        data,
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "数据公私钥签名验证 签名结果：${String(Base64.encode(encryptResult, Base64.DEFAULT))}",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "数据公私钥签名验证 原始数据验证：${
            RSAUtils.verifySignatureWithRSAPublicKey(
                AAFEncrypt.getRSAPublicKeyFormAssets(context, AAFSecretEncrypt.RSA_PUB_KEY_NAME),
                data,
                encryptResult,
            )
        }",
    )
    data = "$data "
    ZLog.d(AAFSecretEncrypt.TAG, "数据公私钥签名验证 修改后数据 ：\"$data\"")
    ZLog.d(
        AAFSecretEncrypt.TAG,
        "数据公私钥签名验证 修改后数据验证：${
            RSAUtils.verifySignatureWithRSAPublicKey(
                AAFEncrypt.getRSAPublicKeyFormAssets(context, AAFSecretEncrypt.RSA_PUB_KEY_NAME),
                data,
                encryptResult,
            )
        }",
    )
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
}
