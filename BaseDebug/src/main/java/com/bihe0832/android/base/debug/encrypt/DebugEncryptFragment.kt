package com.bihe0832.android.base.debug.encrypt

import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.lib.adapter.CardBaseModule

open class DebugEncryptFragment : DebugCommonFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("各种加解密、数据完整性验证等"))
            add(getDebugFragmentItemData("通用AES、RSA、MD5、SHA256验证等", DebugEncryptCommonFragment::class.java))
            add(
                getDebugFragmentItemData(
                    "基于Android keystore 的AES、RSA",
                    DebugEncryptWithKeystoreFragment::class.java,
                ),
            )
            add(getDebugFragmentItemData("基于具体应用场景的加解密方案", DebugEncryptSceneFragment::class.java))
        }
    }
}
