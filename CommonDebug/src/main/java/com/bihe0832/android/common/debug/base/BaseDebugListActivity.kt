package com.bihe0832.android.common.debug.base

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.easyrefresh.CommonListActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import kotlinx.android.synthetic.main.com_bihe0832_fragment_debug_tab.*

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
            override fun fetchData() {
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
        DialogUtils.showInputDialog(this, titleName, msg, defaultValue, listener)
    }


    protected fun startActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    fun openWeb(url: String) {
        openZixieWeb(url)
    }
}