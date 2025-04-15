package com.bihe0832.android.base.debug.card

import android.graphics.Color
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bihe0832.android.base.debug.card.section.SectionDataContentTest
import com.bihe0832.android.base.debug.card.section.SectionDataContent3
import com.bihe0832.android.base.debug.card.section.SettingsDataSwitchForDebug
import com.bihe0832.android.base.debug.card.section.SettingsHolderSwitchForDebug
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
            add(
                CardItemForCommonList(
                    SettingsDataSwitchForDebug::class.java,
                    SettingsHolderSwitchForDebug::class.java,
                    true
                )
            )

        }
    }

    override fun initView(view: View) {
        super.initView(view)
        mRecyclerView?.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addItemDecoration(
                GridDividerItemDecoration.Builder(context).apply {
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
            add(SettingsDataSwitchForDebug().apply {
                mItemIconRes = R.mipmap.icon
                mAutoGenerateColorFilter = false
                title = "fdsfsf"
                description = "fsdfsdfsdfsfsdfsdfs"
                tips = "已开启"
                onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
            })
            add(SettingsDataSwitchForDebug().apply {
                mItemIconRes = R.mipmap.icon
                title = "fdsfsf"
                description = "fsdfsdfsdfsfsdfsdfs"
                tips = "已开启"
                onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
            })
            add(SettingsDataSwitchForDebug().apply {
                mItemIconRes = R.drawable.icon_help
                mAutoGenerateColorFilter = false
                title = "fdsfsf"
                description = "fsdfsdfsdfsfsdfsdfs"
                tips = "已开启"
                onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
            })
            add(SettingsDataSwitchForDebug().apply {
                mItemIconRes = R.drawable.icon_share
                mItemIconResColorFilter = Color.RED
                title = "fdsfsf"
                description = "fsdfsdfsdfsfsdfsdfs"
                tips = "已开启"
                onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
            })
            add(SettingsDataSwitchForDebug().apply {
                mItemIconRes = R.drawable.icon_help
                title = "fdsfsf"
                description = "fsdfsdfsdfsfsdfsdfs"
                tips = "已开启"
                onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
            })
            for (i in 0..2) {
//                add(if (i < 2) {
//                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
//                } else {
//                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
//                })
                for (j in 0..3) {
                    if (i < 2) {
                        add(SectionDataContent("内容1:${System.currentTimeMillis()}", "",false))
                    } else {
                        add(
                            SectionDataContentTest(
                                "内容2:${System.currentTimeMillis()}"
                            )
                        )
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