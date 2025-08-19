package com.bihe0832.android.common.compose.common

import android.content.Context
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.mvi.ViewEvent
import com.bihe0832.android.common.compose.mvi.ViewSideEffect
import com.bihe0832.android.common.compose.mvi.ViewState

open class CommonActionEvent : ViewEvent {
    object InitData : CommonActionEvent()
    object Refresh : CommonActionEvent()
    object ClickLoading : CommonActionEvent()
}


open class CommonViewSideEffect : ViewSideEffect {
    object ClickLoading : CommonViewSideEffect()
}


data class CommonActionState(
    val canRefresh: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshLoading: Boolean = false,
    val loadingMsg: String = "",
    val errorMsg: String = "",
) : ViewState

fun getCommonActionState(context: Context): CommonActionState {
    return CommonActionState(
        canRefresh = true,
        isLoading = false,
        isRefreshLoading = false,
        loadingMsg = context.getString(R.string.com_bihe0832_loading),
        errorMsg = ""
    )
}