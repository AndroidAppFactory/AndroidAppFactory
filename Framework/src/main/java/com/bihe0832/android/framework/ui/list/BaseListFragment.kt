package  com.bihe0832.android.framework.ui.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager
import java.lang.Exception

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class BaseListFragment : BaseFragment() {

    //提供数据交互的liveData
    abstract fun getDataLiveData(): CommonListLiveData

    abstract fun getResID(): Int


    open fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf()
    }

    open fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeLinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    protected var mRecyclerView: RecyclerView? = null;


    private val mHeadView by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    protected val mDataLiveData by lazy {
        getDataLiveData()
    }

    protected val mAdapter: CardBaseAdapter by lazy {
        object : CardBaseAdapter(context, mutableListOf<CardBaseModule>()) {
            init {
                getCardList()?.forEach { item ->
                    item.getmCardItemClass()?.let {
                        addItemToAdapter(it, item.isHeader)
                    }

                }
            }
        }.apply {
            emptyView = LayoutInflater.from(context).inflate(R.layout.common_view_list_empty, null, false)
            emptyView.findViewById<TextView>(R.id.common_view_list_empty_content_tips).text = mDataLiveData.getEmptyText()
            setHeaderFooterEmpty(true, false)
            setHeaderView(getListHeader())
            bindToRecyclerView(mRecyclerView)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getResID(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    open fun initView(view: View) {
        mRecyclerView = view.findViewById(R.id.fragment_list_info_list)
        if(null == mRecyclerView){
            throw AAFException("please check recyclerview id name is : fragment_list_info_list")
        }
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
