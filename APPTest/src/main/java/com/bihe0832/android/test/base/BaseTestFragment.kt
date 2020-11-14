package com.bihe0832.android.test.base

import android.content.Intent
import android.widget.TextView
import com.bihe0832.android.app.router.APPFactoryRouter
import com.bihe0832.android.app.router.openWebPage
import com.bihe0832.android.framework.ui.list.CommonListFragment
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.http.common.HttpBasicRequest
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.DebugTools
import com.bihe0832.android.lib.text.InputDialogCompletedCallback
import com.bihe0832.android.test.R
import com.bihe0832.android.test.base.item.TestItemData

open class BaseTestFragment : CommonListFragment() {

    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("test"))
        }
    }

    val mDataList by lazy {
        ArrayList<CardBaseModule>().apply {
            addAll(getDataList())
        }
    }


    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                postValue(mDataList)
            }

            override fun clearData() {
                mDataList.clear()
            }

            override fun loadMore() {

            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return false
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    protected fun sendInfo(title: String, content: String) {
        DebugTools.sendInfo(context, title, content, false)
    }


    protected fun showInfo(title: String, content: String) {
        DebugTools.showInfo(context, title, content, "发送到第三方应用")
    }

    fun showInputDialog(titleName: String, msg: String, defaultValue: String, listener: InputDialogCompletedCallback) {
        DebugTools.showInputDialog(context, titleName, msg, defaultValue, listener)
    }

    protected fun openWeb(url: String) {
        APPFactoryRouter.openWebPage(url)
    }

    protected fun startActivity(cls: Class<*>) {
        val intent = Intent(context, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    protected fun showResult(s: String?) {
        s?.let {
            ZLog.d(HttpBasicRequest.LOG_TAG, "showResult:$s")
            val textView = view?.findViewById<TextView>(R.id.test_tips)
            textView?.text = "Result: $s"
        }
    }
}