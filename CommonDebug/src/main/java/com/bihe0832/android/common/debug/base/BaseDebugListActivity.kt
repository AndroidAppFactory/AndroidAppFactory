package com.bihe0832.android.common.debug.base

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bihe0832.android.common.debug.DebugUtils
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.easyrefresh.CommonListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.utils.os.BuildUtils

abstract class BaseDebugListActivity : CommonListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }
        CardInfoHelper.getInstance().setAutoAddItem(true)
    }


    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("test"))
        }
    }

    private val mTestDataLiveData by lazy {
        object : DebugListLiveData() {
            override fun initData() {
                postValue(getDataList())
            }
        }
    }

    override fun getEmptyText(): String {
        return ZixieContext.applicationContext?.getString(R.string.common_debug_empty_tips) ?: ""
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mTestDataLiveData
    }

    protected fun sendInfo(title: String, content: String) {
        DebugUtils.sendInfo(this, title, content)
    }

    protected fun showInfo(title: String, content: List<String>) {
        DebugUtils.showInfo(this, title, content)
    }

    protected fun showInfoWithHTML(title: String, content: List<String>) {
        DebugUtils.showInfoWithHTML(this, title, content)
    }

    protected fun showInfo(title: String, content: String) {
        DebugUtils.showInfo(this, title, content)
    }

    fun showInputDialog(
            titleName: String,
            msg: String,
            defaultValue: String,
            listener: InputDialogCompletedCallback
    ) {
        DebugUtils.showInputDialog(this, titleName, msg, defaultValue, listener)
    }


    protected fun startDebugActivity(cls: Class<*>) {
        startDebugActivity(cls, "")
    }

    protected fun startDebugActivity(cls: Class<*>, titleName: String) {
        DebugUtils.startDebugActivity(this, cls, titleName)
    }

    protected open fun startActivityWithException(cls: String) {
        DebugUtils.startActivityWithException(this, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>) {
        DebugUtils.startActivityWithException(this, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>, data: Map<String, String>?) {
        DebugUtils.startActivityWithException(this, cls, data)
    }


    fun openWeb(url: String) {
        openZixieWeb(url)
    }
}