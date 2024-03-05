package com.bihe0832.android.base.debug.encrypt

import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils

open class DebugEncryptFragment : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("通用AES、RSA、MD5、SHA256验证等"))
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
            add(DebugTipsData("基于 Keystore 的加解密方案"))
            add(DebugItemData("使用系统生成秘钥RSA加密解密", View.OnClickListener { androidRSAEncrypt() }))
            add(DebugItemData("使用系统生成秘钥系统AES加密解密", View.OnClickListener { androidAESEncrypt() }))
            add(DebugTipsData("具体应用场景加解密方案"))
            add(DebugItemData("使用系统生成秘钥AES加密解密", View.OnClickListener { saveAES() }))
            add(DebugItemData("使用本地公钥RSA加解密秘钥后AES加密解密", View.OnClickListener { saveData() }))
            add(DebugItemData("数据公私钥签名验证", View.OnClickListener { checkSig() }))
        }
    }
}
