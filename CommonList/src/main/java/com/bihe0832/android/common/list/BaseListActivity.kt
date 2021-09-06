package  com.bihe0832.android.common.list

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class BaseListActivity : BaseActivity() {

    //提供数据交互的liveData
    abstract fun getDataLiveData(): CommonListLiveData

    abstract fun getTitleText(): String

    abstract fun getResID(): Int

    abstract fun getLayoutManagerForList(): RecyclerView.LayoutManager

    open fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf()
    }

    open fun hasHeaderView(): Boolean {
        return false
    }

    fun getLinearLayoutManagerForList(): RecyclerView.LayoutManager {
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

    protected val mDataLiveData by lazy {
        getDataLiveData()
    }

    protected var mRecyclerView: RecyclerView? = null;

    val mAdapter: CardBaseAdapter by lazy {
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
            emptyView.findViewById<TextView>(R.id.common_view_list_empty_content_tips).text = if (getLayoutManagerForList() is GridLayoutManager) {
                getString(R.string.bad_layoutmanager_empty_tips)
            } else {
                mDataLiveData.getEmptyText()
            }

            bindToRecyclerView(mRecyclerView)
            setHeaderFooterEmpty(true, false)
            if (hasHeaderView()) {
                setHeaderView(getListHeader())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResID())
        mToolbar = findViewById(R.id.common_activity_list_toolbar)
        if (null == mToolbar) {
            throw AAFException("please check mToolbar id name is : common_activity_list_toolbar")
        }
        mToolbar?.apply {
            setNavigationOnClickListener { onBackPressedSupport() }
            title = getTitleText()
        }
        mRecyclerView = findViewById(R.id.activity_list_info_list)
        if (null == mRecyclerView) {
            throw AAFException("please check recyclerview id name is : activity_list_info_list")
        }
        initView()
    }

    protected open fun initView() {

        mRecyclerView?.apply {
            layoutManager = getLayoutManagerForList()
            setHasFixedSize(true)
            adapter = mAdapter
            isFocusableInTouchMode = false
        }

        mDataLiveData.observe(::getLifecycle) {
            it?.let {
                updateData(it)
            }
        }
    }

    fun getListHeader(): LinearLayout {
        return mHeadView
    }

    fun getAdapter(): CardBaseAdapter {
        return mAdapter
    }

    protected open fun updateData(data: List<CardBaseModule>) {
        mAdapter.apply {
            mRecyclerView?.post {
                this.data.apply {
                    clear()
                    addAll(data)
                    notifyDataSetChanged()
                }
            }
        }
    }
}
