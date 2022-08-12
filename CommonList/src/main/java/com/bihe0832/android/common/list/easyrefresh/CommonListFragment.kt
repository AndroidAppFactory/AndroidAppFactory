package  com.bihe0832.android.common.list.easyrefresh

import android.support.v7.widget.RecyclerView
import android.view.View
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.bihe0832.android.common.list.BaseListFragment
import com.bihe0832.android.common.list.R
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.MyEasyRefreshLayout

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class CommonListFragment : BaseListFragment() {

    protected var mRefresh: MyEasyRefreshLayout? = null

    override fun getResID(): Int {
        return R.layout.common_fragment_list
    }

    override fun initView(view: View) {
        super.initView(view)
        mRefresh = view.findViewById(R.id.fragment_list_refresh)
        mRefresh?.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                mRefresh?.loadMoreModel = LoadModel.COMMON_MODEL
                mDataLiveData.refresh()
            }

            override fun onLoadMore() {
                mDataLiveData.loadMore()
            }
        })
        mRefresh?.isEnablePullToRefresh = mDataLiveData.canRefresh()

    }

    override fun initData() {
        mDataLiveData.initData()
    }

    override fun updateData(data: List<CardBaseModule>) {
        super.updateData(data)
        mRefresh?.closeLoadView()
        if (mDataLiveData.hasMore()) { // 没有下一页数据了，设置为加载结束
            mRefresh?.loadMoreModel = LoadModel.COMMON_MODEL
        } else {
            mRefresh?.loadMoreModel = LoadModel.NONE
        }
        mAdapter.setEnableLoadMore(mDataLiveData.hasMore())
    }

    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return getLinearLayoutManagerForList()
    }
}
