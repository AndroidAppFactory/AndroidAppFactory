package com.bihe0832.android.base.debug.encrypt

import android.util.Base64
import android.view.View
import com.bihe0832.android.app.encrypt.AAFEncryptConstants
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.utils.keystore.AESKeyStoreUtils
import com.bihe0832.android.lib.utils.keystore.RSAKeyStoreUtils
import java.util.Arrays

open class DebugEncryptWithKeystoreFragment : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("通用加解密方案"))
            add(DebugItemData("使用系统生成秘钥RSA加密解密", View.OnClickListener { androidRSAEncrypt() }))
            add(DebugItemData("使用系统生成秘钥系统AES加密解密", View.OnClickListener { androidAESEncrypt() }))
        }
    }

    fun androidRSAEncrypt() {
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

    private fun androidAESEncrypt() {
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
}
