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
import com.bihe0832.android.lib.utils.encrypt.AESUtils
import com.bihe0832.android.lib.utils.encrypt.RSAUtils
import java.util.Arrays

open class DebugEncryptSceneFragment : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("具体应用场景加解密方案"))
            add(DebugItemData("使用系统生成秘钥AES加密解密", View.OnClickListener { saveAES() }))
            add(DebugItemData("使用本地公钥RSA加解密秘钥后AES加密解密", View.OnClickListener { saveData() }))
            add(DebugItemData("数据公私钥签名验证", View.OnClickListener { checkSig() }))
        }
    }

    private fun saveAES() {
        val data = TextFactoryUtils.getRandomString(16)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "AES ${AAFEncryptConstants.AES_MOD} 内容加密原始数据 ：$data")
        var encryptResult = AAFEncrypt.aesEncrypt(context, AAFSecretEncrypt.DEMO_AES_KEY_ALIAS, data)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "AES ${AAFEncryptConstants.AES_MOD} 内容加密向量 ：${Arrays.toString(encryptResult.iv)}",
        )
        ZLog.d(
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
                AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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

    private fun saveData() {
        val data = TextFactoryUtils.getRandomString(16)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "内容加密 原始数据 ：$data")
        var encryptResult = AAFEncrypt.encryptDataWithKeyStore(context, AAFSecretEncrypt.RSA_PUB_KEY_NAME, data)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
            AAFEncrypt.getRSAPrivateKeyFormAssets(context, AAFSecretEncrypt.RSA_PRI_KEY_NAME),
            encryptResult.keyEncryptData,
        )
        val result = AESUtils.decrypt(AAFEncryptConstants.AES_MOD, key, encryptResult.iv, encryptResult.result)
        ZLog.d(AAFSecretEncrypt.TAG, "内容 AES ${AAFEncryptConstants.AES_MOD} 解密 Data：${String(result)}")
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
    }

    private fun checkSig() {
        var data = TextFactoryUtils.getRandomString(16)
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(AAFSecretEncrypt.TAG, "数据公私钥签名验证 原始数据 ：\"$data\"")
        var encryptResult = RSAUtils.signDataWithRSAPrivateKey(
            AAFEncrypt.getRSAPrivateKeyFormAssets(
                context!!,
                AAFSecretEncrypt.RSA_PRI_KEY_NAME,
            ),
            data,
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.TAG,
            "数据公私钥签名验证 签名结果：${String(Base64.encode(encryptResult, Base64.DEFAULT))}",
        )
        ZLog.d(AAFSecretEncrypt.TAG, "-------------------------------------------")
        ZLog.d(
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
            AAFSecretEncrypt.DEMO_AES_KEY_ALIAS,
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
}