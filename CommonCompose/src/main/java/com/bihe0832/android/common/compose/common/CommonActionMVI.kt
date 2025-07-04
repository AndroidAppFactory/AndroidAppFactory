package com.bihe0832.android.common.compose.common

import android.app.Application
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.mvi.ViewEvent
import com.bihe0832.android.common.compose.mvi.ViewSideEffect
import com.bihe0832.android.common.compose.mvi.ViewState

sealed class CommonActionEvent : ViewEvent {
    class LoadingStart(val text: String) : CommonActionEvent()
    object SimpleLoadingStart : CommonActionEvent()
    object LoadingFinished : CommonActionEvent()
}

data class CommonActionState(
    val loadingMsg: String, val loadingSuccess: String, val loadingFailedMsg: String
) : ViewState


fun getCommonActionState(application: Application): CommonActionState {
    return CommonActionState(
        loadingMsg = application.getString(R.string.com_bihe0832_loading),
        loadingSuccess = application.getString(R.string.com_bihe0832_loading_completed),
        loadingFailedMsg = application.getString(R.string.com_bihe0832_load_failed),
    )
}

sealed class CommonActionEffect : ViewSideEffect {
    data class Loading(val text: String) : CommonActionEffect()
    object LoadingFinished : CommonActionEffect()
    data class LoadingSuccess(val text: String) : CommonActionEffect()
    data class LoadingFailed(val text: String, val errorCode: Int, val errorMsg: String) :
        CommonActionEffect()
}