package com.bihe0832.android.base.debug.message


import android.view.View
import androidx.lifecycle.Observer
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.lib.adapter.CardBaseModule

class DebugMessageFragment : BaseDebugListFragment() {
    val LOG_TAG = this.javaClass.simpleName


    override fun initView(view: View) {
        super.initView(view)
        AAFMessageManager.getMessageLiveData().observe(this) { t ->
            t?.filter { it.isNotExpired && !it.hasDelete() }?.forEach {
                AAFMessageManager.showNotice(activity!!, it,false)
            }
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("初始化并拉取公告", View.OnClickListener { testTrace() }))
        }
    }

    private fun testTrace() {
        AAFMessageManager.updateMsg()
    }

}