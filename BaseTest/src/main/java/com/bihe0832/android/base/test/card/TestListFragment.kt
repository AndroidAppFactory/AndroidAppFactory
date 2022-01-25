package com.bihe0832.android.base.test.card

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.bihe0832.android.base.test.card.section.SectionDataHeader
import com.bihe0832.android.base.test.card.section.SectionDataHeader2
import com.bihe0832.android.common.test.item.TestTipsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.test.R
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.GridDividerItemDecoration
import com.bihe0832.android.lib.utils.os.DisplayUtil

class TestListFragment : CommonListFragment() {
    val mDataList = ArrayList<CardBaseModule>()

    override fun initView(view: View) {
        super.initView(view)
        mRecyclerView?.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addItemDecoration(
                GridDividerItemDecoration.Builder(context).apply {
                    setShowLastLine(false)
                    setColor(R.color.activity_test_bg)
                    setHorizontalSpan(DisplayUtil.dip2px(context!!, 1f).toFloat())
                }.build()
            )
        }
    }
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
//        return SafeGridLayoutManager(context, 3)
        return getLinearLayoutManagerForList()

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
                return "当前数据为空，请稍候再试"
            }
        }
    }

    override fun hasHeaderView(): Boolean {
        return false
    }

    private fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
//            for (i in 0..2) {
////                add(if (i < 2) {
////                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
////                } else {
////                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
////                })
//                for (j in 0..3) {
//                    add(if (i < 2) {
//                        SectionDataContent("内容11:${System.currentTimeMillis()}")
//                    } else {
//                        SectionDataContent2("内容22:${System.currentTimeMillis()}")
//                    })
//                }
//            }
        }
    }

}