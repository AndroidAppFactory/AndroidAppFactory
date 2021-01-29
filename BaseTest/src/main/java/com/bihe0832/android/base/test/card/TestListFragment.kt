package com.bihe0832.android.base.test.card

import android.support.v7.widget.RecyclerView
import com.bihe0832.android.base.test.card.section.SectionDataContent
import com.bihe0832.android.base.test.card.section.SectionDataContent2
import com.bihe0832.android.base.test.card.section.SectionDataHeader
import com.bihe0832.android.base.test.card.section.SectionDataHeader2
import com.bihe0832.android.common.test.item.TestTipsData
import com.bihe0832.android.framework.ui.list.CardItemForCommonList
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.framework.ui.list.swiperefresh.CommonListFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager

class TestListFragment : CommonListFragment() {
    val mDataList = ArrayList<CardBaseModule>()

    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(context, 3)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(TestTipsData::class.java, true))
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
            add(CardItemForCommonList(SectionDataHeader2::class.java, true))
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
//                mDataList.add(
//                        TestTipsData("点击打开List 测试Activity") { RouterHelper.openPageByRouter(ROUTRT_NAME_TEST_SECTION) }
//                )
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun clearData() {
                mDataList.clear()
            }

            override fun loadMore() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return true
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    override fun hasHeaderView(): Boolean {
        return false
    }

    private fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            for (i in 0..2) {
//                add(if (i < 2) {
//                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
//                } else {
//                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
//                })
                for (j in 0..3) {
                    add(if (i < 2) {
                        SectionDataContent("内容11:${System.currentTimeMillis()}")
                    } else {
                        SectionDataContent2("内容22:${System.currentTimeMillis()}")
                    })
                }
            }
        }
    }

}