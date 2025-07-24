package com.bihe0832.android.common.list.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.bihe0832.android.common.compose.ui.EmptyView
import com.bihe0832.android.common.compose.ui.ErrorView
import com.bihe0832.android.common.compose.ui.LoadingView
import com.bihe0832.android.common.compose.ui.RefreshView
import com.bihe0832.android.common.list.compose.placeholder.ListMoreDataEmptyView
import com.bihe0832.android.common.list.compose.placeholder.ListMoreDataErrorView
import com.bihe0832.android.common.list.compose.placeholder.ListMoreDataLoadingView

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> CommonRefreshList(
    enableRefresh: Boolean,
    enableLoadMore: Boolean,
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: LazyListScope.() -> Unit,
    onRefresh: () -> Unit = {
        lazyPagingItems.refresh()
    },
) {
    val isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    // 优先处理全局错误状态（最高优先级）
    if (lazyPagingItems.loadState.refresh is LoadState.Error) {
        ErrorView(onRetry = lazyPagingItems::retry)
        return
    }
    val pullRefreshState = rememberPullToRefreshState()
    Box(
        modifier = Modifier
            .pullToRefresh(isRefreshing = isRefreshing,
                state = pullRefreshState,
                enabled = enableRefresh,
                onRefresh = { onRefresh.invoke() })
            .fillMaxSize()
    ) {
        // 1. 首次加载（无数据时全屏加载）
        if (lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount < 1) {
            LoadingView()
            return
        }

        // 2. 列表内容区域
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            itemContent()
            if (enableLoadMore && !isRefreshing) {
                item(key = "load_more_state") { // 添加唯一 key 避免无效重组
                    when (val appendState = lazyPagingItems.loadState.append) {
                        is LoadState.Loading -> ListMoreDataLoadingView()
                        is LoadState.Error -> ListMoreDataErrorView(onRetry = lazyPagingItems::retry)
                        is LoadState.NotLoading -> {
                            if (appendState.endOfPaginationReached) {
                                ListMoreDataEmptyView()
                            }
                        }
                    }
                }
            }
        }

        if (lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount > 0) {
            RefreshView()
        }

        if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.NotLoading && !isRefreshing) {
            EmptyView()
        }
    }
}