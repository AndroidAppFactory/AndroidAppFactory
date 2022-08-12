package com.bihe0832.android.common.debug.base

import android.content.Intent
import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.easyrefresh.CommonListFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback

open class BaseDebugListFragment : CommonListFragment() {

    private val mTestDataLiveData by lazy {
        object : DebugListLiveData() {
            override fun initData() {
                postValue(getDataList())
            }
        }
    }

    override fun getResID(): Int {
        return R.layout.com_bihe0832_fragment_debug_tab
    }

    override fun initView(view: View) {
        super.initView(view)
        CardInfoHelper.getInstance().setAutoAddItem(true)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(DebugTipsData::class.java, true))
            add(CardItemForCommonList(DebugItemData::class.java, true))
        }
    }

    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("test"))
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mTestDataLiveData
    }

    protected fun sendInfo(title: String, content: String) {
        DebugTools.sendInfo(context, title, content, false)
    }

    override fun getEmptyText(): String {
        return ZixieContext.applicationContext?.getString(R.string.common_debug_empty_tips) ?: ""
    }

    protected fun showInfo(title: String, content: String) {
        DebugTools.showInfo(context, title, content, "发送到第三方应用")
    }

    fun showInputDialog(
            titleName: String,
            msg: String,
            defaultValue: String,
            listener: InputDialogCompletedCallback
    ) {
        DialogUtils.showInputDialog(context!!, titleName, msg, defaultValue, listener)
    }


    protected open fun startActivity(cls: Class<*>) {
        val intent = Intent(context, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    protected open fun showResult(s: String?) {
        s?.let {
            ThreadManager.getInstance().runOnUIThread {
                ZLog.d(HTTPServer.LOG_TAG, "showResult:$s")
                view?.findViewById<TextView>(R.id.test_tips)?.apply {
                    this.text = TextFactoryUtils.getSpannedTextByHtml("<B>提示信息</b>:<BR> $s")
                    visibility = View.VISIBLE
                }
                ZixieContext.showDebug(it)
            }
        }
    }
}