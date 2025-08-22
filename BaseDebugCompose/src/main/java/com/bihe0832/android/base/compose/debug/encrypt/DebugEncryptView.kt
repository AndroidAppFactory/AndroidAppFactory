package com.bihe0832.android.base.compose.debug.encrypt;

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.utils.encrypt.rsa.RSAUtils

@Preview
@Composable
fun DebugEncryptView() {
    DebugContent {
        DebugTips("通用AES、RSA、MD5、SHA256验证等")
        DebugItem("通用完整性校验（MD5、SHA256）") { testMessageDigest(it) }
        DebugItem("RSA加解密调试") { rsaEncryptDebug(it) }
        DebugItem("使用 RSA ${RSAUtils.MOD_OAEP} 加密解密") { rsaEncrypt(it, RSAUtils.MOD_OAEP) }
        DebugItem("使用 RSA ${RSAUtils.MOD_PKCS_1} 加密解密") {
            rsaEncrypt(it, RSAUtils.MOD_PKCS_1)
        }
        DebugItem("AES加解密调试") { aesEncryptDebug() }
        DebugItem("使用 AES 加密解密") { aesEncrypt(it) }
        DebugTips("基于 Keystore 的加解密方案")
        DebugItem("使用系统生成秘钥RSA加密解密") { androidRSAEncrypt(it) }
        DebugItem("使用系统生成秘钥系统AES加密解密") { androidAESEncrypt(it) }
        DebugTips("具体应用场景加解密方案")
        DebugItem("使用系统生成秘钥AES加密解密") { saveAES(it) }
        DebugItem("使用本地公钥RSA加解密秘钥后AES加密解密") { saveData(it) }
        DebugItem("数据公私钥签名验证") { checkSig(it) }
    }
}
