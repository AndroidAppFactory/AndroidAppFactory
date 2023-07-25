package com.bihe0832.android.base.debug.message


import android.view.View
import com.bihe0832.android.app.message.AAFMessageListFragment
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.list.card.MessageItemData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.gson.JsonHelper

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
            add(DebugItemData("测试消息样式", View.OnClickListener {
                testMsg()
            }))


        }
    }

    private fun testTrace() {
        AAFMessageManager.updateMsg()
    }

    private fun testMsg() {
        val messageData = "{" +
                "        \"id\": \"1\"," +
                "        \"type\": \"text\"," +
                "        \"title\": \"置顶应用下载公告带通知栏通知\"," +
                "        \"content\": \"1. 修复下载期间偶现Crash，提升稳定性 <BR> 2. 优化下载及使用用户体验\"," +
                "        \"should_top\": \"1\"," +
                "        \"expire_date\": \"-1\"," +
                "        \"action\": \"https://android.bihe0832.com/doc/summary/links.html\"," +
                "        \"isNotify\": \"1\"," +
                "        \"showFace\": 2," +
                "        \"create_date\": \"202307200600\"" +
                "    }"
        JsonHelper.fromJson(messageData, MessageInfoItem::class.java)?.let {
            AAFMessageManager.showMessage(activity!!, it, false)
        }

    }

}