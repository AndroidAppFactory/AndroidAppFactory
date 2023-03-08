package com.bihe0832.android.base.debug.message


import android.view.View
import com.bihe0832.android.app.message.AAFMessageListFragment
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.lib.adapter.CardBaseModule

class DebugMessageFragment : BaseDebugListFragment() {
    val LOG_TAG = this.javaClass.simpleName


    override fun initView(view: View) {
        super.initView(view)
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("初始化并拉取公告", View.OnClickListener { testTrace() }))
            add(DebugItemData("打开消息详情页(Fragment)", View.OnClickListener {
                startDebugActivity(AAFMessageListFragment::class.java)
            }))
            add(DebugItemData("打开消息详情页(Activity)", View.OnClickListener {
                RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
            }))

        }
    }

    private fun testTrace() {
        AAFMessageManager.updateMsg()
    }

}