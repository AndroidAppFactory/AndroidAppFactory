package com.bihe0832.android.test.module.card

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.bihe0832.android.app.router.APPFactoryRouter.openPageByRouter
import com.bihe0832.android.framework.ui.list.CardItemForCommonList
import com.bihe0832.android.framework.ui.list.CommonListFragment
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.test.base.item.TestTipsData
import com.bihe0832.android.test.module.card.section.SectionDataContent
import com.bihe0832.android.test.module.card.section.SectionDataContent2
import com.bihe0832.android.test.module.card.section.SectionDataHeader
import com.bihe0832.android.test.module.card.section.SectionDataHeader2

class TestListFragment : CommonListFragment() {
    val mDataList = ArrayList<CardBaseModule>()

    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 3)
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
                mDataList.add(
                        TestTipsData("点击打开List 测试Activity") { openPageByRouter(ROUTRT_NAME_TEST_SECTION) }
                )
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
                return true
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

}