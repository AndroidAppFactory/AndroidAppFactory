package  com.bihe0832.android.framework.ui.list.swiperefresh

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.list.BaseListFragment
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class CommonListFragment : BaseListFragment() {

    private var mRefresh: SwipeRefreshLayout? = null


    override fun getResID(): Int {
        return R.layout.common_fragment_list_swipe
    }

    override fun initView(view: View) {
        super.initView(view)
        mRefresh = view.findViewById(R.id.fragment_list_refresh)
        mRefresh?.apply {
            setOnRefreshListener {
                mDataLiveData.clearData()
                mDataLiveData.fetchData()
            }
            setOnChildScrollUpCallback { _, _ ->
                if (mDataLiveData.canRefresh()) {
                    var headerNum = mAdapter.headerLayoutCount
                    (mRecyclerView?.childCount ?: 0 > 0
                            && ((mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() > headerNum || mRecyclerView?.getChildAt(0)
                            ?.top ?: 0 < mRecyclerView?.paddingTop ?: 0))
                } else {
                    true
                }
            }
        }
        mRefresh?.isEnabled = mDataLiveData.canRefresh()
        
        mDataLiveData.fetchData()

        mAdapter.setOnLoadMoreListener({
            mDataLiveData.loadMore()
        }, mRecyclerView)
    }

    override fun updateData(data: List<CardBaseModule>) {
        super.updateData(data)
        mRefresh?.isRefreshing = false
        mAdapter.loadMoreComplete()
        mAdapter.setEnableLoadMore(mDataLiveData.hasMore())
    }

    fun loadDataFinished() {
        mRefresh?.isRefreshing = false
        mAdapter.loadMoreComplete()
    }
}
