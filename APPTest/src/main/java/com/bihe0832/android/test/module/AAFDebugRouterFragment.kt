package com.bihe0832.android.test.module

import com.bihe0832.android.common.debug.item.getRouterItem
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.lib.request.URLUtils

class AAFDebugRouterFragment : com.bihe0832.android.common.debug.module.DebugRouterFragment() {

    override fun getRouterList(): ArrayList<ContentItemData> {
        return ArrayList<ContentItemData>().apply {
            add(getRouterItem("zixie://test"))
            add(getRouterItem("zixie://about"))
            add(getRouterItem("zixie://zshare?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(getRouterItem("zixie://zfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(getRouterItem("zixie://ztbsfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(getRouterItem("zixie://zweb?redirect=https%3A%2F%2Fblog.bihe0832.com"))
            add(getRouterItem("zixie://zweb?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(getRouterItem("zixie://zfeedback?url=" + URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")))
            add(getRouterItem("zixie://ztbsfeedback?url=" + URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")))
            add(getRouterItem("zixie://zweb?url=" + URLUtils.encode("https://play.google.com/store/apps/details?id=com.pubg.newstate")))
        }
    }
}