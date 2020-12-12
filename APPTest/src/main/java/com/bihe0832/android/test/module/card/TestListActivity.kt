package com.bihe0832.android.test.module.card

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.list.CardItemForCommonList
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.framework.ui.list.swiperefresh.CommonListActivity
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager
import com.bihe0832.android.test.module.card.section.SectionDataContent
import com.bihe0832.android.test.module.card.section.SectionDataContent2
import com.bihe0832.android.test.module.card.section.SectionDataHeader
import com.bihe0832.android.test.module.card.section.SectionDataHeader2

const val ROUTRT_NAME_TEST_SECTION = "testlist"

@Module(ROUTRT_NAME_TEST_SECTION)
class TestListActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    var num = 0
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 4)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
        }
    }

    override fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.transparent)
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
                num ++
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun hasMore(): Boolean {
                return num < 5;
            }

            override fun canRefresh(): Boolean {
                return true
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    private fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            for (i in 0..2) {
                add(if (i < 2) {
                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
                } else {
                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
                })
                for (j in 0..3) {
                    add(if (i < 2) {
                        SectionDataContent("内容1:${System.currentTimeMillis()}")
                    } else {
                        SectionDataContent2("内容2:${System.currentTimeMillis()}")
                    })
                }
            }
        }
    }

    override fun getTitleText(): String {
        return "Section测试"
    }
}