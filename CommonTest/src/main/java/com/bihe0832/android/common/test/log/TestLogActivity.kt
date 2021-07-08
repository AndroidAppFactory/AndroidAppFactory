package com.bihe0832.android.common.test.log;

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.ui.list.CardItemForCommonList
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.framework.ui.list.swiperefresh.CommonListActivity
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment


open class TestLogActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    var num = 0
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 3)
    }

    override fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.transparent)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
            add(CardItemForCommonList(TestItemData::class.java, true))
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun clearData() {
                mDataList.clear()
                num = 0
            }

            override fun loadMore() {
                num++
                mDataList.addAll(getTempData())
                postValue(mDataList)
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

    open fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(TestItemData("选择并发送单个日志") { FileSelectTools.openFileSelect(this@TestLogActivity, ZixieContext.getLogFolder()) })
            add(SectionDataHeader("日志测试"))
            add(SectionDataContent("日志测试", LoggerFile.getZixieFileLogPathByModule("Test")))
        }
    }

    override fun getTitleText(): String {
        return "日志功能汇总"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == SwipeBackFragment.RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                FileUtils.sendFile(this@TestLogActivity, filePath, "*/*").let {
                    if (!it) {
                        ZixieContext.showToast("分享文件:$filePath 失败")
                    }
                }
            }
        }
    }
}