package com.bihe0832.android.test.module

import com.bihe0832.android.lib.request.URLUtils

class AAFDebugRouterFragment : com.bihe0832.android.common.debug.module.DebugRouterFragment() {

    override fun getRouterList(): ArrayList<RouterItem> {
        return ArrayList<RouterItem>().apply {
            add(RouterItem("zixie://test"))
            add(RouterItem("zixie://about"))
            add(RouterItem("zixie://zshare?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(RouterItem("zixie://zfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(RouterItem("zixie://zweb?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(RouterItem("zixie://zfeedback?url=" + URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")))
            add(RouterItem("zixie://zweb?url=" + URLUtils.encode("https://play.google.com/store/apps/details?id=com.pubg.newstate")))
        }
    }
}