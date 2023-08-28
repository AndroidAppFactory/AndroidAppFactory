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
import com.bihe0832.android.lib.utils.keystore.AESKeyStoreUtils
import com.bihe0832.android.lib.utils.keystore.RSAKeyStoreUtils
import java.util.Arrays
import javax.crypto.Cipher

open class DebugEncryptFragment : DebugCommonFragment() {

    private val TAG = "DebugEncryptFragment"

    private val KEY = "1234567890ASCDEF1234567890ASCDEF"
    private val AES_KEY_ALIAS = "aes"
    private val RSA_KEY_ALIAS = "rsa"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("通用加解密方案"))
            add(DebugItemData("通用完整性校验（MD5、SHA256）", View.OnClickListener { testMessageDigest() }))
            add(DebugItemData("使用 AES 加密解密", View.OnClickListener { aesEncrypt() }))
            add(DebugItemData("使用 RSA 加密解密", View.OnClickListener { rsaEncrypt() }))
            add(DebugItemData("使用系统生成秘钥RSA加密解密", View.OnClickListener { androidRSAEncrypt() }))
            add(DebugItemData("使用系统生成秘钥系统AES加密解密", View.OnClickListener { androidAESEncrypt() }))
            add(DebugTipsData("具体应用场景加解密方案"))
            add(DebugItemData("使用系统生成秘钥AES加密解密", View.OnClickListener { saveAES() }))
            add(DebugItemData("使用本地公钥RSA加解密秘钥后AES加密解密", View.OnClickListener { saveData() }))
            add(DebugItemData("数据公私钥签名验证", View.OnClickListener { checkSig() }))
        }
    }

    private fun rsaEncrypt() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            p0?.let { data ->
                rsaEncrypt(data)
                var default = TextFactoryUtils.getRandomString(256)
                rsaEncrypt(default)
            }
        }
    }

    private fun rsaEncrypt(shortData: String) {
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "RSA 加密原始数据 ：$shortData")
        val encryptResult = RSAUtils.encrypt(
            context!!,
            AAFEncrypt.getRSAPublicKeyFormAssets(context!!, AAFEncryptConstants.RSA_PUB_KEY_NAME),
            shortData.toByteArray(),
        )
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "RSA 内容加密 ：${Arrays.toString(encryptResult)}")
        ZLog.d(TAG, "RSA 内容加密：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(
            TAG,
            "RSA 内容直接解密 ：${
                String(
                    RSAUtils.decrypt(
                        context,
                        AAFEncrypt.getRSAPrivateKeyFormAssets(context!!, AAFEncryptConstants.RSA_PRI_KEY_NAME),
                        encryptResult,
                    ),
                )
            }",
        )
        Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT).let {
            ZLog.d(
                TAG,
                "RSA 内容模拟解密 ：${
                    String(
                        RSAUtils.decrypt(
                            context,
                            AAFEncrypt.getRSAPrivateKeyFormAssets(context!!, AAFEncryptConstants.RSA_PRI_KEY_NAME),
                            it,
                        ),
                    )
                }",
            )
        }
        ZLog.d(TAG, "-------------------------------------------")
    }

    private fun testMessageDigest() {
        "123456".let {
            MD5.getMd5(it).let { data ->
                ZLog.d(TAG, "$it MD5 is: $data")
                ZLog.d(TAG, "$it MD5 length is: ${data.length}")
            }
            MessageDigestUtils.getDigestData(it, "MD5").let { data ->
                ZLog.d(TAG, "$it MD5 is: $data")
                ZLog.d(TAG, "$it MD5 length is: ${data.length}")
            }
            SHA256.getSHA256(it).let { data ->
                ZLog.d(TAG, "$it SHA256 is: $data")
                ZLog.d(TAG, "$it SHA256 length is: ${data.length}")
            }
            MessageDigestUtils.getDigestData(it, "SHA-256").let { data ->
                ZLog.d(TAG, "$it SHA256 is: $data")
                ZLog.d(TAG, "$it SHA256 length is: ${data.length}")
            }
        }
    }

    fun aesEncrypt() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            p0?.let { data ->
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 加密原始数据 ：$data")
                val encryptResult = AESUtils.encrypt(KEY.toByteArray(), data)
                val encryptIVResult = AESUtils.doAESEncryptWithIV(
                    KEY.toByteArray(),
                    AAFEncryptConstants.IV_BYTES,
                    Cipher.ENCRYPT_MODE,
                    data.toByteArray(),
                )
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容加密 ：${Arrays.toString(encryptResult)}")
                ZLog.d(TAG, "AES 内容加密 ：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容直接解密 ：${String(AESUtils.decrypt(KEY.toByteArray(), encryptResult))}")
                Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT).let {
                    ZLog.d(TAG, "AES 内容模拟解密 ：${Arrays.toString(it)}")
                    ZLog.d(TAG, "AES 内容模拟解密 ：${String(AESUtils.decrypt(KEY.toByteArray(), it))}")
                }
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容向量加密 ：${Arrays.toString(encryptIVResult)}")
                ZLog.d(TAG, "AES 内容向量加密 ：${String(Base64.encode(encryptIVResult, Base64.DEFAULT))}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")

                ZLog.d(
                    TAG,
                    "AES 内容向量直接解密 ：${
                        String(
                            AESUtils.doAESEncryptWithIV(
                                KEY.toByteArray(),
                                AAFEncryptConstants.IV_BYTES,
                                Cipher.DECRYPT_MODE,
                                encryptIVResult,
                            ),
                        )
                    }",
                )
                Base64.decode(Base64.encode(encryptIVResult, Base64.DEFAULT), Base64.DEFAULT).let {
                    ZLog.d(TAG, "AES 内容向量模拟解密 ：${Arrays.toString(it)}")
                    ZLog.d(
                        TAG,
                        "AES 内容向量模拟解密 ：${
                            String(
                                AESUtils.doAESEncryptWithIV(
                                    KEY.toByteArray(),
                                    AAFEncryptConstants.IV_BYTES,
                                    Cipher.DECRYPT_MODE,
                                    it,
                                ),
                            )
                        }",
                    )
                }
                ZLog.d(TAG, "-------------------------------------------")
            }
        }
    }

    fun androidRSAEncrypt() {
        val data = TextFactoryUtils.getRandomString(16)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "系统自带秘钥 RSA 加密原始数据 ：$data")
        val encryptResult = RSAKeyStoreUtils.encrypt(context, RSA_KEY_ALIAS, data.toByteArray())
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "系统自带秘钥 RSA 内容加密 ：${Arrays.toString(encryptResult)}")
        ZLog.d(TAG, "系统自带秘钥 RSA 内容加密 ：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(
            TAG,
            "系统自带秘钥 RSA 内容直接解密 ：${
                String(
                    RSAKeyStoreUtils.decrypt(
                        context,
                        RSA_KEY_ALIAS,
                        encryptResult,
                    ),
                )
            }",
        )
        ZLog.d(
            TAG,
            "系统自带秘钥 RSA 内容模拟解密 ：${
                String(
                    RSAKeyStoreUtils.decrypt(
                        context,
                        RSA_KEY_ALIAS,
                        Base64.decode(Base64.encode(encryptResult, Base64.DEFAULT), Base64.DEFAULT),
                    ),
                )
            }",
        )
        ZLog.d(TAG, "-------------------------------------------")
    }

    private fun androidAESEncrypt() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            p0?.let { data ->
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "系统自带秘钥 AES 加密原始数据 ：$data")
                val encryptResult =
                    AESKeyStoreUtils.encrypt(context, AES_KEY_ALIAS, data.toByteArray())
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "系统自带秘钥 AES 内容加密 ：${Arrays.toString(encryptResult.result)}")
                ZLog.d(TAG, "系统自带秘钥 AES 内容加密 ：${String(Base64.encode(encryptResult.result, Base64.DEFAULT))}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(
                    TAG,
                    "系统自带秘钥 AES 内容直接解密 ：${
                        String(
                            AESKeyStoreUtils.decrypt(
                                context,
                                AES_KEY_ALIAS,
                                encryptResult.iv,
                                encryptResult.result,
                            ),
                        )
                    }",
                )
                ZLog.d(
                    TAG,
                    "系统自带秘钥 AES 内容模拟解密 ：${
                        String(
                            AESKeyStoreUtils.decrypt(
                                context,
                                AES_KEY_ALIAS,
                                encryptResult.iv,
                                Base64.decode(Base64.encode(encryptResult.result, Base64.DEFAULT), Base64.DEFAULT),
                            ),
                        )
                    }",
                )
                ZLog.d(TAG, "-------------------------------------------")
            }
        }
    }

    private fun saveAES() {
        val data = TextFactoryUtils.getRandomString(16)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "AES 内容加密原始数据 ：$data")
        var encryptResult = AAFEncrypt.aesEncrypt(context, AES_KEY_ALIAS, data)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "AES 内容加密向量 ：${Arrays.toString(encryptResult.iv)}")
        ZLog.d(TAG, "AES 内容加密结果 ：${String(Base64.encode(encryptResult.result, Base64.DEFAULT))}")
        ZLog.d(TAG, "-------------------------------------------")
        encryptResult?.let {
            ZLog.d(
                TAG,
                "AES 内容模拟解密 ：${
                    AAFEncrypt.aesDecrypt(
                        context!!,
                        AES_KEY_ALIAS,
                        encryptResult.iv,
                        encryptResult.result,
                    )
                }",
            )
            ZLog.d(TAG, "-------------------------------------------")
        }
    }

    private fun saveData() {
        val data = TextFactoryUtils.getRandomString(16)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "内容加密 原始数据 ：$data")
        var encryptResult =
            AAFEncrypt.encryptDataWithKeyStore(context, AAFEncryptConstants.RSA_PUB_KEY_NAME, data)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "内容加密 Data：${String(Base64.encode(encryptResult.result, Base64.DEFAULT))}")
        ZLog.d(TAG, "内容加密 IV：${String(Base64.encode(encryptResult.iv, Base64.DEFAULT))}")
        ZLog.d(TAG, "内容加密 KEY：${String(Base64.encode(encryptResult.keyEncryptData, Base64.DEFAULT))}")
        ZLog.d(TAG, "-------------------------------------------")
        val key = RSAUtils.decrypt(
            context,
            AAFEncrypt.getRSAPrivateKeyFormAssets(context, AAFEncryptConstants.RSA_PRI_KEY_NAME),
            encryptResult.keyEncryptData,
        )
        val result = AESUtils.doAESEncryptWithIV(key, encryptResult.iv, Cipher.DECRYPT_MODE, encryptResult.result)
        ZLog.d(TAG, "内容解密 Data：${String(result)}")
        ZLog.d(TAG, "-------------------------------------------")
    }

    private fun checkSig() {
        var data = TextFactoryUtils.getRandomString(16)
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "数据公私钥签名验证 原始数据 ：\"$data\"")
        var encryptResult = RSAUtils.signDataWithRSAPrivateKey(
            AAFEncrypt.getRSAPrivateKeyFormAssets(
                context!!,
                AAFEncryptConstants.RSA_PRI_KEY_NAME,
            ),
            data,
        )
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(TAG, "数据公私钥签名验证 签名结果：${String(Base64.encode(encryptResult, Base64.DEFAULT))}")
        ZLog.d(TAG, "-------------------------------------------")
        ZLog.d(
            TAG,
            "数据公私钥签名验证 原始数据验证：${
                RSAUtils.verifySignatureWithRSAPublicKey(
                    AAFEncrypt.getRSAPublicKeyFormAssets(context, AAFEncryptConstants.RSA_PUB_KEY_NAME),
                    data,
                    encryptResult,
                )
            }",
        )
        data = "$data "
        ZLog.d(TAG, "数据公私钥签名验证 修改后数据 ：\"$data\"")
        ZLog.d(
            TAG,
            "数据公私钥签名验证 修改后数据验证：${
                RSAUtils.verifySignatureWithRSAPublicKey(
                    AAFEncrypt.getRSAPublicKeyFormAssets(context, AAFEncryptConstants.RSA_PUB_KEY_NAME),
                    data,
                    encryptResult,
                )
            }",
        )
        ZLog.d(TAG, "-------------------------------------------")
    }
}
