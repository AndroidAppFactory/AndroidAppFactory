package com.bihe0832.android.common.list.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bihe0832.android.common.list.compose.mvi.CommonListEvent
import com.bihe0832.android.common.list.compose.mvi.CommonListViewModel

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonRefreshList(viewModel: CommonListViewModel, content: @Composable () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val isRefreshing by remember { mutableStateOf(uiState.isRefreshing) }
    val pullRefreshState = rememberPullToRefreshState()
    Box(
        Modifier
            .pullToRefresh(isRefreshing = isRefreshing,
                state = pullRefreshState,
                enabled = uiState.canRefresh,
                onRefresh = {
                    viewModel.sendEvent(CommonListEvent.Refresh)
                })
            .fillMaxSize()
    ) {
        content()
    }
}