package com.bihe0832.android.test.module

import android.view.View
import com.bihe0832.android.common.test.base.BaseTestFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.common.test.item.TestTipsData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.router.Routers

class TestRouterFragment : BaseTestFragment() {

    private inner class RouterItem(url: String) : TestItemData(url, View.OnClickListener { Routers.open(context, url) })

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestTipsData(
                    "可以在PC打开下面链接生成二维码后测试：<small>https://microdemo.bihe0832.com/MyJS/router/</small> ",
                    View.OnClickListener {
                        showInfo("路由测试工具链接分享", "路由测试工具链接：\n https://microdemo.bihe0832.com/MyJS/router/")
                    })
            )
            add(RouterItem("zixie://test"))
            add(RouterItem("zixie://babout"))
            add(RouterItem("zixie://testhttp"))
            add(RouterItem("zixie://feedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
            add(RouterItem("zixie://testlist"))
            add(RouterItem("zixie://testweb"))
            add(RouterItem("zixie://web?url=https%3A%2F%2Fblog.bihe0832.com"))
            add(RouterItem("zixie://feedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"))
        }
    }

}