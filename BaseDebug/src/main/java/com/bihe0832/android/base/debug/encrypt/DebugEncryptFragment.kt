package com.bihe0832.android.base.debug.encrypt

import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils

open class DebugEncryptFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("通用AES、RSA、MD5、SHA256验证等"))
            add(getDebugItem("通用完整性校验（MD5、SHA256）", View.OnClickListener { testMessageDigest() }))
            add(getDebugItem("RSA加解密调试", View.OnClickListener { rsaEncryptDebug() }))
            add(
                getDebugItem(
                    "使用 RSA ${RSAUtils.MOD_OAEP} 加密解密",
                    View.OnClickListener { rsaEncrypt(RSAUtils.MOD_OAEP) },
                ),
            )
            add(
                getDebugItem(
                    "使用 RSA ${RSAUtils.MOD_PKCS_1} 加密解密",
                    View.OnClickListener { rsaEncrypt(RSAUtils.MOD_PKCS_1) },
                ),
            )

            add(getDebugItem("AES加解密调试", View.OnClickListener { aesEncryptDebug() }))
            add(getDebugItem("使用 AES 加密解密", View.OnClickListener { aesEncrypt() }))
            add(getTipsItem("基于 Keystore 的加解密方案"))
            add(getDebugItem("使用系统生成秘钥RSA加密解密", View.OnClickListener { androidRSAEncrypt() }))
            add(getDebugItem("使用系统生成秘钥系统AES加密解密", View.OnClickListener { androidAESEncrypt() }))
            add(getTipsItem("具体应用场景加解密方案"))
            add(getDebugItem("使用系统生成秘钥AES加密解密", View.OnClickListener { saveAES() }))
            add(getDebugItem("使用本地公钥RSA加解密秘钥后AES加密解密", View.OnClickListener { saveData() }))
            add(getDebugItem("数据公私钥签名验证", View.OnClickListener { checkSig() }))
        }
    }
}
