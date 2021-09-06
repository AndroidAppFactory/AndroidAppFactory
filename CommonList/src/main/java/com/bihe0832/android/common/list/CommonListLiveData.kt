package  com.bihe0832.android.common.list

import android.arch.lifecycle.MediatorLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 *
 * @author code@bihe0832.com
 * Created on 2019-12-06.
 * Description: Description
 *
 */
abstract class CommonListLiveData : MediatorLiveData<List<CardBaseModule>>() {
    //首次拉取数据
    abstract fun fetchData()
    //清空数据
    abstract fun clearData()
    //加载更多数据
    abstract fun loadMore()
    //是否还有更多数据待加载
    abstract fun hasMore(): Boolean
    //是否支持下拉刷新
    abstract fun canRefresh(): Boolean
    //列表为空时返回数据
    abstract fun getEmptyText() : String
}