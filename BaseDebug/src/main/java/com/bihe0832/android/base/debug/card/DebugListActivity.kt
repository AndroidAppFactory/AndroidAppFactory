package com.bihe0832.android.base.debug.card

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.card.section.SectionDataContentTest
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.media.image.loadImage
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.custom.view.PlaceholderView
import com.bihe0832.android.lib.ui.recycleview.ext.GridDividerItemDecoration
import com.bihe0832.android.lib.ui.recycleview.ext.RecyclerViewItemActiveHelper
import com.bihe0832.android.lib.utils.os.DisplayUtil

class TestListActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    var num = 0


    override fun initView() {
        super.initView()
        mRecyclerView?.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addItemDecoration(
                    GridDividerItemDecoration.Builder(context).apply {
                        setShowLastLine(true)
                        setColor(R.color.md_theme_outline)
                        setHorizontalSpan(DisplayUtil.dip2px(context!!, 10f).toFloat())
//                    setVerticalSpan(DisplayUtil.dip2px(context!!, 10f).toFloat())
                    }.build()
            )

            recyclerViewItemActiveHelper?.let {
                addOnScrollListener(it)
            }
        }
        mAdapter.apply {
            isUpFetchEnable = true
            setUpFetchListener {
                num++
                if (num < 5) {
                    ThreadManager.getInstance().start(
                            {
                                var data = getTempData(true)
                                mAdapter.data.addAll(0, data)
                                mAdapter.notifyItemRangeInserted(0, data.size)
                            }, 500L
                    )
                }
            }

            emptyView = PlaceholderView(this@TestListActivity).apply {
                getHeadIcon().apply {
                    loadImage(R.mipmap.icon)
                    visibility = View.VISIBLE
                }
                setEmptyTips("这是一个测试消息")
                getActionText().apply {
                    setText("点击更新")
                    setTextColor(resources.getColor(R.color.colorAccent))
                    strokeColor = resources.getColor(R.color.colorAccent)
                    strokeWidth = DisplayUtil.dip2px(this@TestListActivity, 1f)
                    backgroundColor = resources.getColor(R.color.windowBackground)
                    visibility = View.VISIBLE
                    setOnClickListener {
                        RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT)
                    }
                }
            }
        }
    }

    protected val recyclerViewItemActiveHelper by lazy {
        mRecyclerView?.let {
            RecyclerViewItemActiveHelper(it, object : RecyclerViewItemActiveHelper.ActiveCallback() {
                override fun onActive(recyclerView: RecyclerView, position: Int) {
                    val newposition = if (getListHeader() == null) {
                        position
                    } else {
                        position - 1
                    }
                    super.onActive(recyclerView, newposition)
                    ZixieContext.showDebug("Test onActive:" + newposition)
                    it.findViewHolderForAdapterPosition(newposition)?.let { viewHolder ->
                        (mAdapter.data[newposition] as CardBaseModule).apply {

                        }.let { newData ->
                            (viewHolder as CardBaseHolder).initData(newData)
                        }
                    }
                    if (num > 4) {
                        mRefresh?.isEnabled = true
                    }

                }

                override fun onDeactive(recyclerView: RecyclerView, position: Int) {
                    super.onDeactive(recyclerView, position)
                    ZixieContext.showDebug("Test onDeactive:" + position)

                }
            })
        }
    }
//
//    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
//        return SafeGridLayoutManager(this, 4)
//    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataContentTest::class.java, true))
        }
    }

    override fun getNavigationBarColor(): Int {
        return ThemeResourcesManager.getColor(R.color.transparent)!!
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun initData() {
                mDataList.addAll(getTempData(false))
                postValue(mDataList)
            }

            override fun refresh() {
                mDataList.clear()
                num = 0
                initData()
            }

            override fun loadMore() {
                num++
                mDataList.addAll(getTempData(false))
                postValue(mDataList)
            }

            override fun hasMore(): Boolean {
                return num < 5
            }

            override fun canRefresh(): Boolean {
                return num > 4
            }
        }
    }

    private fun getTempData(isUpper: Boolean): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
//            for (i in 0..19) {
//                add(
////                        if (i < 2) {
////                            SectionDataHeader("标题1 - $i:${System.currentTimeMillis()}")
////                        } else {
//                        SectionDataHeader2("标题2 - $i $isUpper:${System.currentTimeMillis()}")
////                        }
//                )
//                for (j in 0..3) {
//                    add(
//                            if (i < 2) {
//                                SectionDataContent("内容1 - $i $j:${System.currentTimeMillis()}", "")
//                            } else {
//                                SectionDataContent2("内容2 - $i $j:${System.currentTimeMillis()}")
//                            }
//                    )
//                }
//            }
        }
    }

    override fun getTitleText(): String {
        return "Section测试"
    }
}