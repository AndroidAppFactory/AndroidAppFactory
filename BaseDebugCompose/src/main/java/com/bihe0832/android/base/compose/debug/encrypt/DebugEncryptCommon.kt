package com.bihe0832.android.base.compose.debug.encrypt;

import android.content.Context
import android.util.Base64
import com.bihe0832.android.app.encrypt.AAFEncrypt
import com.bihe0832.android.app.encrypt.AAFEncryptConstants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.encrypt.aes.AESUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils
import java.util.Arrays

fun testMessageDigest(context: Context, content: String) {
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    MD5.getMd5(content).let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "$content MD5 is: $data")
        ZLog.d(AAFSecretEncrypt.TAG, "$content MD5 length is: ${data.length}")
    }
    MessageDigestUtils.getDigestData(content, "MD5").let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "$content MD5 is: $data")
        ZLog.d(AAFSecretEncrypt.TAG, "$content MD5 length is: ${data.length}")
    }
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    SHA256.getSHA256(content).let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "$content SHA256 is: $data")
        ZLog.d(AAFSecretEncrypt.TAG, "$content SHA256 length is: ${data.length}")
    }
    MessageDigestUtils.getDigestData(content, "SHA-256").let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "$content SHA256 is: $data")
        ZLog.d(AAFSecretEncrypt.TAG, "$content SHA256 length is: ${data.length}")
    }
    ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    APKUtils.getSigPublicKey(context, context!!.packageName).let {
        ZLog.d(
            "PublicKeyByteStringToWindows:\n" + RSAUtils.transPublicKeyByteStringToWindows(
                RSAUtils.getPublicKeyByteString(
                    it,
                ),
            ),
        )
        ZLog.d("getPublicKeyByteString:\n" + RSAUtils.getPublicKeyByteString(it))
        ZLog.d("getPublicKeyContent:\n" + RSAUtils.getPublicKeyContent(it, 0))
        ZLog.d("getPublicKeyPemString:\n" + RSAUtils.getPublicKeyPemString(it))
    }
}

internal fun testMessageDigest(context: Context) {
    DialogUtils.showInputDialog(
        context,
        "要加密的内容",
        "1234567890ABCDEF",
    ) {
        Thread { testMessageDigest(context, it) }.start()
//        Thread { testMessageDigest(context!!, it) }.start()
//        Thread { testMessageDigest(context!!, it) }.start()
//        Thread { testMessageDigest(context!!, it) }.start()
    }
}

internal fun rsaEncrypt(context: Context, mod: String, shortData: String) {
    try {
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "RSA $mod 加密原始数据 ：$shortData")
        val encryptResult = RSAUtils.encrypt(
            mod,
            AAFEncrypt.getRSAPublicKeyFormAssets(context, AAFSecretEncrypt.RSA_PUB_KEY_NAME),
            shortData.toByteArray(),
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "RSA $mod 内容加密 ：${Arrays.toString(encryptResult)}")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "RSA $mod 内容加密：${String(Base64.encode(encryptResult, Base64.DEFAULT))}"
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "RSA $mod 内容直接解密 ：${
                String(
                    RSAUtils.decrypt(
                        mod,
                        AAFSecretEncrypt.getRSAPrivateKeyFormAssets(),
                        encryptResult,
                    ),
                )
            }",
        )
        Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT).let {
            ZLog.d(
                AAFSecretEncrypt.TAG,
                "RSA $mod 内容模拟解密 ：${
                    String(
                        RSAUtils.decrypt(
                            mod,
                            AAFSecretEncrypt.getRSAPrivateKeyFormAssets(),
                            it,
                        ),
                    )
                }",
            )
        }
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal fun rsaEncrypt(context: Context, mod: String) {
    DialogUtils.showInputDialog(
        context = context!!,
        titleName = "要加密的内容",
        defaultValue = "1234567890ABCDEF",
    ) { p0 ->
        rsaEncrypt(context, mod, p0)
    }
}

internal fun rsaEncryptDebug(context: Context) {
    var default = TextFactoryUtils.getRandomString(16)
    rsaEncrypt(context, RSAUtils.MOD_OAEP, default)
    rsaEncrypt(context, RSAUtils.MOD_PKCS_1, default)
    default = TextFactoryUtils.getRandomString(256)
    rsaEncrypt(context, RSAUtils.MOD_OAEP, default)
    rsaEncrypt(context, RSAUtils.MOD_PKCS_1, default)
}

internal fun aesEncrypt(shortData: String) {
    shortData.let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "AES 加密原始数据 ：$data")
        val encryptResult = AESUtils.encrypt(AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(), data)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "AES 内容加密 ：${Arrays.toString(encryptResult)}")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES 内容加密 ：${String(Base64.encode(encryptResult, Base64.DEFAULT))}"
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES 内容直接解密 ：${
                String(
                    AESUtils.decrypt(
                        AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(),
                        encryptResult,
                    ),
                )
            }",
        )
        Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT).let {
            ZLog.d(AAFSecretEncrypt.TAG, "AES 内容模拟解密 ：${Arrays.toString(it)}")
            ZLog.d(
                AAFSecretEncrypt.TAG,
                "AES 内容模拟解密 ：${
                    String(
                        AESUtils.decrypt(
                            AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(),
                            it,
                        ),
                    )
                }",
            )
        }
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    }
}

internal fun aesEncrypt(mod: String, shortData: String) {
    shortData.let { data ->
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "AES $mod 加密原始数据 ：$data")
        val encryptIVResult = AESUtils.encrypt(
            mod,
            AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(),
            AAFEncryptConstants.IV_BYTES,
            data.toByteArray(),
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "AES $mod 内容向量加密 ：${Arrays.toString(encryptIVResult)}")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES $mod 内容向量加密 ：${String(Base64.encode(encryptIVResult, Base64.DEFAULT))}",
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")

        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES $mod 内容向量直接解密 ：${
                String(
                    AESUtils.decrypt(
                        mod,
                        AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(),
                        AAFEncryptConstants.IV_BYTES,
                        encryptIVResult,
                    ),
                )
            }",
        )
        Base64.decode(Base64.encode(encryptIVResult, Base64.DEFAULT), Base64.DEFAULT).let {
            ZLog.d(AAFSecretEncrypt.TAG, "AES $mod 内容向量模拟解密 ：${Arrays.toString(it)}")
            ZLog.d(
                AAFSecretEncrypt.TAG,
                "AES $mod 内容向量模拟解密 ：${
                    String(
                        AESUtils.decrypt(
                            mod,
                            AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(),
                            AAFEncryptConstants.IV_BYTES,
                            it,
                        ),
                    )
                }",
            )
        }
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    }
}

internal fun aesEncrypt(context: Context) {
    DialogUtils.showInputDialog(
        context,
        "要加密的内容",
        "1234567890ABCDEF",
    ) { p0 ->
        aesEncrypt(p0)
        aesEncrypt(AESUtils.MODE_CBC_PKCS7, p0)
    }
}

internal fun aesEncryptDebug() {
    var default = TextFactoryUtils.getRandomString(16)
    aesEncrypt(default)
    aesEncrypt(AESUtils.MODE_CBC_PKCS7, default)
    default = TextFactoryUtils.getRandomString(256)
    aesEncrypt(default)
    aesEncrypt(AESUtils.MODE_CBC_PKCS7, default)
}
