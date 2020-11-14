package  com.bihe0832.android.framework.ui.list

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager
import kotlinx.android.synthetic.main.common_activity_list.*

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class CommonListActivity : BaseActivity() {

    //提供数据交互的liveData
    abstract fun getDataLiveData(): CommonListLiveData

    abstract fun getTitleText(): String


    open fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf()
    }

    open fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeLinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }


    private val mHeadView by lazy {
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private val mDataLiveData by lazy {
        getDataLiveData()
    }

    private val mAdapter: CardBaseAdapter by lazy {
        object : CardBaseAdapter(this, mutableListOf<CardBaseModule>()) {
            init {
                getCardList()?.forEach { item ->
                    item.getmCardItemClass()?.let {
                        addItemToAdapter(it, item.isHeader)
                    }

                }
            }
        }.apply {
            emptyView = LayoutInflater.from(applicationContext).inflate(R.layout.common_view_list_empty, null, false)
            emptyView.findViewById<TextView>(R.id.common_view_list_empty_content_tips).text = mDataLiveData.getEmptyText()
            setHeaderFooterEmpty(true, false)
            setHeaderView(getListHeader())
            bindToRecyclerView(common_activity_list_info_list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //透明导航栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        setContentView(R.layout.common_activity_list)
        mToolbar = findViewById(R.id.common_activity_list_toolbar)
        mToolbar?.setNavigationOnClickListener { onBackPressedSupport() }
        mToolbar?.title = getTitleText()
        initView()
    }

    private fun initView() {

        common_activity_list_info_list.apply {
            this.layoutManager = getLayoutManagerForList()
            setHasFixedSize(true)
            adapter = mAdapter
            isFocusableInTouchMode = false
        }

        mDataLiveData.observe(::getLifecycle) {
            it?.let {
                updateData(it)
            }
        }

        common_activity_list_refresh.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                common_activity_list_refresh.loadMoreModel = LoadModel.COMMON_MODEL
                mDataLiveData.clearData()
                mDataLiveData.fetchData()
                common_activity_list_refresh.refreshComplete()
            }

            override fun onLoadMore() {
                mDataLiveData.loadMore()
            }
        })
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
            common_activity_list_info_list.post {
                this.data.apply {
                    clear()
                    addAll(data)
                    notifyDataSetChanged()
                }
            }
            common_activity_list_refresh.closeLoadView()
            if (mDataLiveData.hasMore()) { // 没有下一页数据了，设置为加载结束
                common_activity_list_refresh.loadMoreModel = LoadModel.COMMON_MODEL
            } else {
                common_activity_list_refresh.loadMoreModel = LoadModel.NONE
            }
            setEnableLoadMore(mDataLiveData.hasMore())
        }
    }
}
