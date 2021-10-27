package com.bihe0832.android.test.module

import com.bihe0832.android.common.test.module.TestDebugRouterFragment
import com.bihe0832.android.lib.request.URLUtils

class TestRouterFragment : TestDebugRouterFragment() {

    override fun getRouterList(): ArrayList<TestDebugRouterFragment.RouterItem> {
        return ArrayList<TestDebugRouterFragment.RouterItem>().apply {
            add(RouterItem("zixie://test"))
            add(RouterItem("zixie://about"))
            add(RouterItem("zixie://testhttp"))
            add(RouterItem("zixie://zfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(RouterItem("zixie://testlist"))
            add(RouterItem("zixie://testweb"))
            add(RouterItem("zixie://zweb?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(RouterItem("zixie://zfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(RouterItem("zixie://zweb?url=" + URLUtils.encode("https://play.google.com/store/apps/details?id=com.pubg.newstate")))
            add(RouterItem("zixie://zweb?url=" + URLUtils.encode("https://v.qq.com")))

        }
    }
}