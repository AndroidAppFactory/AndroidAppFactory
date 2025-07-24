package com.bihe0832.android.base.compose.debug.list.model

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.log.ZLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/21.
 * Description: Description
 *
 */
class DebugPageListViewModel : CommonActionViewModel() {

    private val _deletedEmailIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _readEmailIds = MutableStateFlow<Set<Int>>(emptySet())

    private val cachedPagingData = Pager(PagingConfig(pageSize = 10, prefetchDistance = 3)) {
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
                    id = System.currentTimeMillis().toInt()
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
                    return LoadResult.Error(AAFException(it.exception.toString()))
                }

                return LoadResult.Error(AAFException("bad data"))
            }
        }
    }.flow.cachedIn(viewModelScope)

    val dataList: Flow<PagingData<DataItem>> =
        cachedPagingData.flatMapLatest { pagingData ->
            combine(_deletedEmailIds, _readEmailIds) { deletedIds, readIds ->
                pagingData.map { dateItem ->
                    dateItem.copy(
                        isRead = if (readIds.contains(dateItem.id)) {
                            true
                        } else {
                            dateItem.isRead
                        }
                    )
                }.filter { dateItem ->
                    // 过滤掉已删除的项目
                    !deletedIds.contains(dateItem.id)
                }
            }
        }.cachedIn(viewModelScope)

    // 标记邮件已读
    fun markAsRead(emailId: Int) {
        viewModelScope.launch {
            try {
                // 更新本地状态映射
                // 更新已读状态
                val currentReadIds = _readEmailIds.value.toMutableSet()
                currentReadIds.add(emailId)
                _readEmailIds.value = currentReadIds
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 删除邮件
    fun delete(emailId: Int) {
        viewModelScope.launch {
            try {
                // 调用 API 删除服务器上的邮件
                // 更新本地状态映射
                // 将邮件ID添加到删除集合中
                val currentDeletedIds = _deletedEmailIds.value.toMutableSet()
                currentDeletedIds.add(emailId)
                _deletedEmailIds.value = currentDeletedIds
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}