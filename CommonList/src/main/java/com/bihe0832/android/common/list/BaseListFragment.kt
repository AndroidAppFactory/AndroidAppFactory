package  com.bihe0832.android.common.list

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.custom.view.PlaceholderView
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager

/**
 * @author code@bihe0832.com
 * Created on 2019-09-12.
 * Description: 一款通用的包含一个recycleView的列表页，目前支持：下拉刷新、加载更多、添加header、列表为空展示空页面等
 */
abstract class BaseListFragment : BaseFragment() {

    //提供数据交互的liveData
    abstract fun getDataLiveData(): CommonListLiveData

    abstract fun getResID(): Int

    abstract fun getLayoutManagerForList(): RecyclerView.LayoutManager

    open fun hasHeaderView(): Boolean {
        return false
    }

    fun getLinearLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeLinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    open fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf()
    }

    protected var mRecyclerView: RecyclerView? = null;


    private val mHeadView by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    protected val mDataLiveData by lazy {
        getDataLiveData()
    }

    protected val mAdapter: CardBaseAdapter by lazy {
        object : CardBaseAdapter(context, mutableListOf<CardBaseModule>()) {
            init {
                getCardList()?.forEach { item ->
                    item.cardItemClass?.let {
                        if (item.cardHolderClass != null) {
                            addItemToAdapter(it, item.cardHolderClass, item.isHeader)
                        } else {
                            addItemToAdapter(it, item.isHeader)
                        }
                    }
                }
            }
        }.apply {
            emptyView = getBaseEmptyView()
            setHeaderFooterEmpty(true, false)
            isUseEmpty(true)
            if (hasHeaderView()) {
                setHeaderView(getListHeader())
            }
            bindToRecyclerView(mRecyclerView)
        }
    }

    protected open fun getEmptyText(): String {
        return ""
    }

    protected open fun getBaseEmptyView(): View {
        return PlaceholderView(context!!).apply {
            if (getLayoutManagerForList() is GridLayoutManager) {
                ThemeResourcesManager.getString(R.string.bad_layoutmanager_empty_tips)
            } else {
                getEmptyText()
            }.let {
                setEmptyTips(it)
            }
        }
    }

    override fun getLayoutID(): Int {
        return getResID()
    }

    override fun initView(view: View) {
        super.initView(view)
        mRecyclerView = view.findViewById(R.id.fragment_list_info_list)
        if (null == mRecyclerView) {
            throw AAFException("please check recyclerview id name is : fragment_list_info_list")
        }
        mRecyclerView?.apply {
            layoutManager = getLayoutManagerForList()
            setHasFixedSize(true)
            adapter = mAdapter
            isFocusableInTouchMode = false
        }

        mDataLiveData.observe(this) {
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
