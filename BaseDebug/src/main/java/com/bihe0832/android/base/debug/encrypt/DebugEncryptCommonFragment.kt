package com.bihe0832.android.base.debug.encrypt

import android.util.Base64
import android.view.View
import com.bihe0832.android.app.encrypt.AAFEncrypt
import com.bihe0832.android.app.encrypt.AAFEncryptConstants
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.utils.encrypt.AESUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.RSAUtils
import com.bihe0832.android.lib.utils.encrypt.SHA256
import java.util.Arrays

open class DebugEncryptCommonFragment : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("通用加解密方案"))
            add(DebugItemData("通用完整性校验（MD5、SHA256）", View.OnClickListener { testMessageDigest() }))
            add(DebugItemData("RSA加解密调试", View.OnClickListener { rsaEncryptDebug() }))
            add(
                DebugItemData(
                    "使用 RSA ${RSAUtils.MOD_OAEP} 加密解密",
                    View.OnClickListener { rsaEncrypt(RSAUtils.MOD_OAEP) },
                ),
            )
            add(
                DebugItemData(
                    "使用 RSA ${RSAUtils.MOD_PKCS_1} 加密解密",
                    View.OnClickListener { rsaEncrypt(RSAUtils.MOD_PKCS_1) },
                ),
            )

            add(DebugItemData("AES加解密调试", View.OnClickListener { aesEncryptDebug() }))
            add(DebugItemData("使用 AES 加密解密", View.OnClickListener { aesEncrypt() }))
        }
    }

    private fun testMessageDigest() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) {
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            MD5.getMd5(it).let { data ->
                ZLog.d(AAFSecretEncrypt.TAG, "$it MD5 is: $data")
                ZLog.d(AAFSecretEncrypt.TAG, "$it MD5 length is: ${data.length}")
            }
            MessageDigestUtils.getDigestData(it, "MD5").let { data ->
                ZLog.d(AAFSecretEncrypt.TAG, "$it MD5 is: $data")
                ZLog.d(AAFSecretEncrypt.TAG, "$it MD5 length is: ${data.length}")
            }
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            SHA256.getSHA256(it).let { data ->
                ZLog.d(AAFSecretEncrypt.TAG, "$it SHA256 is: $data")
                ZLog.d(AAFSecretEncrypt.TAG, "$it SHA256 length is: ${data.length}")
            }
            MessageDigestUtils.getDigestData(it, "SHA-256").let { data ->
                ZLog.d(AAFSecretEncrypt.TAG, "$it SHA256 is: $data")
                ZLog.d(AAFSecretEncrypt.TAG, "$it SHA256 length is: ${data.length}")
            }
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        }
    }

    private fun rsaEncrypt(mod: String, shortData: String) {
        try {
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            ZLog.d(AAFSecretEncrypt.TAG, "RSA $mod 加密原始数据 ：$shortData")
            val encryptResult = RSAUtils.encrypt(
                mod,
                AAFEncrypt.getRSAPublicKeyFormAssets(context!!, AAFSecretEncrypt.RSA_PUB_KEY_NAME),
                shortData.toByteArray(),
            )
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            ZLog.d(AAFSecretEncrypt.TAG, "RSA $mod 内容加密 ：${Arrays.toString(encryptResult)}")
            ZLog.d(AAFSecretEncrypt.TAG, "RSA $mod 内容加密：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
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

    private fun rsaEncrypt(mod: String) {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            rsaEncrypt(mod, p0)
        }
    }

    private fun rsaEncryptDebug() {
        var default = TextFactoryUtils.getRandomString(16)
        rsaEncrypt(RSAUtils.MOD_OAEP, default)
        rsaEncrypt(RSAUtils.MOD_PKCS_1, default)
        default = TextFactoryUtils.getRandomString(256)
        rsaEncrypt(RSAUtils.MOD_OAEP, default)
        rsaEncrypt(RSAUtils.MOD_PKCS_1, default)
    }

    private fun aesEncrypt(shortData: String) {
        shortData?.let { data ->
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            ZLog.d(AAFSecretEncrypt.TAG, "AES 加密原始数据 ：$data")
            val encryptResult = AESUtils.encrypt(AAFSecretEncrypt.DEMO_AES_KEY.toByteArray(), data)
            ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
            ZLog.d(AAFSecretEncrypt.TAG, "AES 内容加密 ：${Arrays.toString(encryptResult)}")
            ZLog.d(AAFSecretEncrypt.TAG, "AES 内容加密 ：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
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

    private fun aesEncrypt(mod: String, shortData: String) {
        shortData?.let { data ->
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

    private fun aesEncrypt() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            aesEncrypt(p0)
            aesEncrypt(AESUtils.MODE_CBC_PKCS7, p0)
        }
    }

    private fun aesEncryptDebug() {
        var default = TextFactoryUtils.getRandomString(16)
        aesEncrypt(default)
        aesEncrypt(AESUtils.MODE_CBC_PKCS7, default)
        default = TextFactoryUtils.getRandomString(256)
        aesEncrypt(default)
        aesEncrypt(AESUtils.MODE_CBC_PKCS7, default)
    }
}
