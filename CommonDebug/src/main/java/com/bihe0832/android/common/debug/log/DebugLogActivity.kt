package com.bihe0832.android.common.debug.log;

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.common.webview.log.WebviewLoggerFile
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment


open class DebugLogActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    var num = 0
    protected var isView = true
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 3)
    }

    override fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.transparent)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
            add(CardItemForCommonList(SectionDataContent::class.java))
            add(CardItemForCommonList(DebugItemData::class.java, true))
            add(CardItemForCommonList(DebugTipsData::class.java, true))
        }
    }

    protected fun getCommonLogList(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(DebugItemData("日志路径：<BR><small>${LoggerFile.getZixieFileLogPathByModule("*")}</small>"))
            add(DebugItemData("选择并发送日志") {
                isView = false
                FileSelectTools.openFileSelect(this@DebugLogActivity, ZixieContext.getLogFolder())
            })
            add(DebugItemData("选择并查看日志") {
                isView = true
                FileSelectTools.openFileSelect(this@DebugLogActivity, ZixieContext.getLogFolder())
            })
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun initData() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun refresh() {
            }

            override fun loadMore() {
            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return false
            }
        }
    }

    open fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            addAll(getCommonLogList())
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath()))
            add(SectionDataContent("Webview", WebviewLoggerFile.getWebviewLogPath()))
        }
    }

    override fun getTitleText(): String {
        return "日志功能汇总"
    }

    override fun initView() {
        super.initView()
        updateIcon(R.mipmap.ic_left_arrow_white, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == SwipeBackFragment.RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                if (isView) {
                    AAFFileTools.openFileWithTips(this, filePath)
                } else {
                    FileUtils.sendFile(this@DebugLogActivity, filePath).let {
                        if (!it) {
                            ZixieContext.showToast("分享文件:$filePath 失败")
                        }
                    }
                }
            }
        }
    }
}