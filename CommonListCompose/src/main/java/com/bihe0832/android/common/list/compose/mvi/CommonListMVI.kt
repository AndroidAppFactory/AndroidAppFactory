package com.bihe0832.android.common.list.compose.mvi

import com.bihe0832.android.common.compose.mvi.ViewEvent
import com.bihe0832.android.common.compose.mvi.ViewSideEffect
import com.bihe0832.android.common.compose.mvi.ViewState

sealed class CommonListEvent : ViewEvent {
    object InitData : CommonListEvent()
    object Refresh : CommonListEvent()
    object LoadMore : CommonListEvent()
}

data class CommonListState(
    val isRefreshing: Boolean = false,
    val canRefresh: Boolean = true, val hasMore: Boolean = false
) : ViewState


sealed class CommonListEffect : ViewSideEffect {
    object Loading : CommonListEffect()
    object LoadingSuccess : CommonListEffect()
    data class LoadingFailed(val errorCode: Int, val errorMsg: String) : CommonListEffect()
}