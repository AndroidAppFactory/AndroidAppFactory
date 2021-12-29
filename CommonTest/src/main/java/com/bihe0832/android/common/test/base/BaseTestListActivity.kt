package com.bihe0832.android.common.test.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.easyrefresh.CommonListActivity
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.framework.router.openWebPage
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.debug.InputDialogCompletedCallback
import com.bihe0832.android.lib.utils.os.BuildUtils

abstract class BaseTestListActivity : CommonListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }
        CardInfoHelper.getInstance().setAutoAddItem(true)
    }


    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("test"))
        }
    }

    private val mTestDataLiveData by lazy {
        object : TestListLiveData() {
            override fun fetchData() {
                postValue(getDataList())
            }
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mTestDataLiveData
    }

    protected fun sendInfo(title: String, content: String) {
        DebugTools.sendInfo(this, title, content, false)
    }


    protected fun showInfo(title: String, content: String) {
        DebugTools.showInfo(this, title, content, "发送到第三方应用")
    }

    fun showInputDialog(
        titleName: String,
        msg: String,
        defaultValue: String,
        listener: InputDialogCompletedCallback
    ) {
        DebugTools.showInputDialog(this, titleName, msg, defaultValue, listener)
    }


    protected fun startActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    fun openWeb(url: String) {
        openWebPage(url)
    }
}