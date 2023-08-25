package com.bihe0832.android.base.debug.encrypt

import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.utils.encrypt.AESUtils
import com.bihe0832.android.lib.utils.encrypt.HexUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.SHA256
import com.bihe0832.android.lib.utils.keystore.AndroidKeyStoreUtils
import java.util.Arrays
import javax.crypto.Cipher

open class DebugEncryptFragment : DebugCommonFragment() {

    private val TAG = "DebugEncryptFragment"

    private val KEY = "1234567890ASCDEF1234567890ASCDEF"
    private val IV = "1234567890ASCDEF"
    private val KEY_ALIAS = "zixie"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("加解密方案"))
            add(DebugItemData("使用AES 加密解密", View.OnClickListener { aesEncrypt() }))
            add(DebugItemData("使用系统自带加密解密", View.OnClickListener { androidEncrypt() }))
            add(DebugItemData("通用完整性校验（MD5、SHA256）", View.OnClickListener { testMessageDigest() }))
        }
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
                val encryptResult = AESUtils.encrypt(KEY.toByteArray(), data)
                val encryptIVResult = AESUtils.doAESEncryptWithIV(
                    data.toByteArray(),
                    KEY.toByteArray(),
                    IV.toByteArray(),
                    Cipher.ENCRYPT_MODE,
                )
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容加密 ：${Arrays.toString(encryptResult)}")
                ZLog.d(TAG, "AES 内容加密 ：${HexUtils.bytes2HexStr(encryptResult)}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容解密 ：${String(AESUtils.decrypt(KEY.toByteArray(), encryptResult))}")
                HexUtils.hexStr2Bytes(HexUtils.bytes2HexStr(encryptResult)).let {
                    ZLog.d(TAG, "AES 内容解密 ：${Arrays.toString(it)}")
                    ZLog.d(TAG, "AES 内容解密 ：${String(AESUtils.decrypt(KEY.toByteArray(), it))}")
                }
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "AES 内容加密 ：${Arrays.toString(encryptIVResult)}")
                ZLog.d(TAG, "AES 内容加密 ：${HexUtils.bytes2HexStr(encryptIVResult)}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")

                ZLog.d(
                    TAG,
                    "AES 内容解密 ：${
                        String(
                            AESUtils.doAESEncryptWithIV(
                                encryptIVResult,
                                KEY.toByteArray(),
                                IV.toByteArray(),
                                Cipher.DECRYPT_MODE,
                            ),
                        )
                    }",
                )
                HexUtils.hexStr2Bytes(HexUtils.bytes2HexStr(encryptIVResult)).let {
                    ZLog.d(TAG, "AES 内容解密 ：${Arrays.toString(it)}")
                    ZLog.d(
                        TAG,
                        "AES 内容解密 ：${
                            String(
                                AESUtils.doAESEncryptWithIV(
                                    it,
                                    KEY.toByteArray(),
                                    IV.toByteArray(),
                                    Cipher.DECRYPT_MODE,
                                ),
                            )
                        }",
                    )
                }
                ZLog.d(TAG, "-------------------------------------------")
            }
        }
    }

    fun androidEncrypt() {
        DialogUtils.showInputDialog(
            context!!,
            "要加密的内容",
            "1234567890ABCDEF",
        ) { p0 ->
            p0?.let { data ->
                val encryptResult = AndroidKeyStoreUtils.encrypt(context, KEY_ALIAS, data.toByteArray())
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "系统自带内容加密 ：${Arrays.toString(encryptResult)}")
                ZLog.d(TAG, "系统自带内容加密 ：${HexUtils.bytes2HexStr(encryptResult)}")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(TAG, "-------------------------------------------")
                ZLog.d(
                    TAG,
                    "系统自带内容解密 ：${String(AndroidKeyStoreUtils.decrypt(context, KEY_ALIAS, encryptResult))}",
                )
                ZLog.d(
                    TAG,
                    "系统自带内容解密 ：${
                        String(
                            AndroidKeyStoreUtils.decrypt(
                                context,
                                KEY_ALIAS,
                                HexUtils.hexStr2Bytes(HexUtils.bytes2HexStr(encryptResult)),
                            ),
                        )
                    }",
                )
                ZLog.d(TAG, "-------------------------------------------")
            }
        }
    }
}
