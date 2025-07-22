package com.bihe0832.android.base.compose.debug.list.model

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.log.ZLog
import kotlinx.coroutines.flow.Flow

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/21.
 * Description: Description
 *
 */
class DebugPageListViewModel : CommonActionViewModel() {

    val dataList: Flow<PagingData<DataItem>> =
        Pager(PagingConfig(pageSize = 1, prefetchDistance = 1)) {
            object : PagingSource<Int, DataItem>() {
                override fun getRefreshKey(state: PagingState<Int, DataItem>): Int {
                    return 1
                }

                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataItem> {
                    val nextPage = params.key ?: 1
                    ZLog.d("hardy", "load:$params")
                    /** 网络数据请求*/
                    /** 网络数据请求*/
                    val final = mutableListOf<DataItem>()
                    ZLog.d("hardy", "load:$params")
                    final.add(DataItem().apply {
                        title = System.currentTimeMillis().toString()
                    })
                    val result = NetworkApi.getData()
                    result.data()?.let {
                        final.addAll((it.data ?: emptyList()))
                        return try {
                            LoadResult.Page(
                                data = final,
                                prevKey = if (nextPage == 1) null else nextPage - 1,
                                nextKey = if (nextPage > 3) null else nextPage + 1
                            )
                        } catch (e: Exception) {
                            LoadResult.Error(e)
                        }
                    }

                    result.error()?.let {
                        return  LoadResult.Error(AAFException(it.exception.toString()))
                    }

                    return LoadResult.Error(AAFException("bad data"))
                }
            }
        }.flow.cachedIn(viewModelScope)


}