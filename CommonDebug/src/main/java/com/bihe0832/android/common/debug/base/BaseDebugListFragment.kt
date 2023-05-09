package com.bihe0832.android.common.debug.base

import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.debug.DebugUtils
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.easyrefresh.CommonListFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback

open class BaseDebugListFragment : CommonListFragment() {

    private var mLoadingDialog: LoadingDialog? = null

    private val mTestDataLiveData by lazy {
        object : DebugListLiveData() {
            override fun initData() {
                ThreadManager.getInstance().start {
                    getDataList().let {
                        ThreadManager.getInstance().runOnUIThread {
                            if (showLoadIng()) {
                                mLoadingDialog?.dismiss()
                            }
                            postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun showLoadIng(): Boolean {
        return false
    }

    override fun getResID(): Int {
        return R.layout.com_bihe0832_fragment_debug_tab
    }

    override fun initView(view: View) {
        super.initView(view)
        if (showLoadIng()) {
            mLoadingDialog = LoadingDialog(view.context)
            mLoadingDialog?.show()
        }
        CardInfoHelper.getInstance().setAutoAddItem(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLoadingDialog = null
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(DebugItemData::class.java, true))
        }
    }

    fun getDebugFragmentItemData(content: String, clazz: Class<*>): DebugItemData {
        return DebugItemData(content) { startDebugActivity(clazz, content) }

    }

    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("test"))
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mTestDataLiveData
    }

    override fun getEmptyText(): String {
        return ThemeResourcesManager.getString(R.string.common_debug_empty_tips) ?: ""
    }

    protected fun sendInfo(title: String, content: String) {
        DebugUtils.sendInfo(context, title, content)
    }

    protected fun showInfo(title: String, content: List<String>) {
        DebugUtils.showInfo(context, title, content)
    }

    protected fun showInfoWithHTML(title: String, content: List<String>) {
        DebugUtils.showInfoWithHTML(context, title, content)
    }

    protected fun showInfo(title: String, content: String) {
        DebugUtils.showInfo(context, title, content)
    }

    fun showInputDialog(titleName: String, msg: String, defaultValue: String, listener: InputDialogCompletedCallback) {
        DebugUtils.showInputDialog(context, titleName, msg, defaultValue, listener)
    }


    protected fun startDebugActivity(cls: Class<*>) {
        startDebugActivity(cls, "")
    }

    protected fun startDebugActivity(cls: Class<*>, titleName: String) {
        DebugUtils.startDebugActivity(context, cls, titleName)
    }

    protected open fun startActivityWithException(cls: String) {
        DebugUtils.startActivityWithException(context, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>) {
        DebugUtils.startActivityWithException(context, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>, data: Map<String, String>?) {
        DebugUtils.startActivityWithException(context, cls, data)
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