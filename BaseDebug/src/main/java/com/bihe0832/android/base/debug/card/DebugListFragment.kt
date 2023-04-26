package com.bihe0832.android.base.debug.card

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bihe0832.android.base.debug.card.section.SectionDataContent2
import com.bihe0832.android.base.debug.card.section.SectionDataContent3
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.log.SectionDataContent
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.GridDividerItemDecoration
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 * 列表测试专用，非通用逻辑
 */

class DebugListFragment : CommonListFragment() {

    val mDataList = ArrayList<CardBaseModule>()
    var num = 0

    private val mListLiveData = object : CommonListLiveData() {
        override fun initData() {
            mDataList.addAll(getTempData())
            postValue(mDataList)
        }

        override fun refresh() {
            mDataList.clear()
            initData()
        }

        override fun loadMore() {
            mDataList.addAll(getTempData())
            postValue(mDataList)
        }

        override fun hasMore(): Boolean {
            return num < 4
        }

        override fun canRefresh(): Boolean {
            return true
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mListLiveData
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataContent3::class.java, true))
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        mRecyclerView?.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addItemDecoration(GridDividerItemDecoration.Builder(context).apply {
                setShowLastLine(false)
                setColor(R.color.divider)
                setHorizontalSpan(DisplayUtil.dip2px(context!!, 1f).toFloat())
            }.build()
            )
        }
    }

    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
//        return SafeGridLayoutManager(context, 3)
        return getLinearLayoutManagerForList()
    }

    override fun getEmptyText(): String {
        return "当前数据为空，请稍候再试"
    }

    override fun hasHeaderView(): Boolean {
        return false
    }

    private fun getTempData(): List<CardBaseModule> {
        num++
        mutableListOf<CardBaseModule>().apply {
            for (i in 0..2) {
//                add(if (i < 2) {
//                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
//                } else {
//                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
//                })
                for (j in 0..3) {
                    if (i < 2) {
                        add(SectionDataContent("内容1:${System.currentTimeMillis()}", ""))
                    } else {
                        add(SectionDataContent2("内容2:${System.currentTimeMillis()}"))
                        add(SectionDataContent3("内容3:${System.currentTimeMillis()}"))
                    }
                }
            }
        }.let {
            loadDataFinished()
            return it
        }
    }

}