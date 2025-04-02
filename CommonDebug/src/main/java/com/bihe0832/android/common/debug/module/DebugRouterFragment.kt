package com.bihe0832.android.common.debug.module

import android.view.View
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.lib.adapter.CardBaseModule

open class DebugRouterFragment : BaseDebugListFragment() {

    final override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                getTipsItem(
                    "可以在PC打开下面链接生成二维码后测试：<small>https://microdemo.bihe0832.com/MyJS/router/</small> ",
                    View.OnClickListener {
                        showInfo(
                            "路由测试工具链接分享",
                            "路由测试工具链接：\n https://microdemo.bihe0832.com/MyJS/router/"
                        )
                    })
            )

            addAll(getRouterList())
        }
    }

    open fun getRouterList(): ArrayList<ContentItemData> {
        return ArrayList<ContentItemData>()
    }
}