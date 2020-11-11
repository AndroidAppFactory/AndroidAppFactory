package  com.bihe0832.android.framework.ui.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager
import kotlinx.android.synthetic.main.common_fragment_list.*

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class CommonListFragment : BaseFragment() {

    //提供数据交互的liveData
    abstract fun getDataLiveData(): CommonListLiveData

    abstract fun getCardList(): List<Class<out CardBaseModule>>

    private val mHeadView by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private val mDataLiveData by lazy {
        getDataLiveData()
    }

    private val mAdapter: CardBaseAdapter by lazy {
        object : CardBaseAdapter(context, mutableListOf<CardBaseModule>()) {
            init {
                getCardList().forEach {
                    addItemToAdapter(it)
                }
            }
        }.apply {
            emptyView = LayoutInflater.from(context).inflate(R.layout.common_view_list_empty, null, false)
            emptyView.findViewById<TextView>(R.id.common_view_list_empty_content_tips).text = mDataLiveData.getEmptyText()

            setHeaderFooterEmpty(true, false)
            setHeaderView(getListHeader())
            bindToRecyclerView(common_fragment_list_info_list)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.common_fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val layoutManager = SafeLinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        common_fragment_list_info_list.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            adapter = mAdapter
            isFocusableInTouchMode = false
        }

        mDataLiveData.observe(::getLifecycle) {
            it?.let {
                updateData(it)
            }
        }

        common_fragment_list_refresh.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                common_fragment_list_refresh.loadMoreModel = LoadModel.COMMON_MODEL
                mDataLiveData.clearData()
                mDataLiveData.fetchData()
                common_fragment_list_refresh.refreshComplete()
            }

            override fun onLoadMore() {
                mDataLiveData.loadMore()
            }
        })
        common_fragment_list_refresh.isEnablePullToRefresh = mDataLiveData.canRefresh()


        mDataLiveData.fetchData()
    }

    fun getListHeader(): LinearLayout {
        return mHeadView
    }

    fun getAdapter(): CardBaseAdapter {
        return mAdapter
    }

    private fun updateData(data: List<CardBaseModule>) {
        mAdapter.apply {
            common_fragment_list_info_list.post {
                this.data.apply {
                    clear()
                    addAll(data)
                    notifyDataSetChanged()
                }
            }
            common_fragment_list_refresh.closeLoadView()
            if (mDataLiveData.hasMore()) { // 没有下一页数据了，设置为加载结束
                common_fragment_list_refresh.loadMoreModel = LoadModel.COMMON_MODEL
            } else {
                common_fragment_list_refresh.loadMoreModel = LoadModel.NONE
            }
            setEnableLoadMore(mDataLiveData.hasMore())
        }
    }
}
