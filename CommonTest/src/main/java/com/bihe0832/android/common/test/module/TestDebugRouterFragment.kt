package com.bihe0832.android.common.test.module

import android.view.View
import com.bihe0832.android.common.test.base.BaseTestListFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.common.test.item.TestTipsData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.router.Routers

open class TestDebugRouterFragment : BaseTestListFragment() {

    inner class RouterItem(url: String) : TestItemData(
            url,
            View.OnClickListener { Routers.open(context, url) },
            View.OnLongClickListener {
                showInfo("复制并分享路由地址", url)
                true
            }
    )

    final override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestTipsData(
                    "可以在PC打开下面链接生成二维码后测试：<small>https://microdemo.bihe0832.com/MyJS/router/</small> ",
                    View.OnClickListener {
                        showInfo("路由测试工具链接分享", "路由测试工具链接：\n https://microdemo.bihe0832.com/MyJS/router/")
                    })
            )

            addAll(getRouterList())
        }
    }

    open fun getRouterList(): ArrayList<RouterItem> {
        return ArrayList<RouterItem>()
    }
}