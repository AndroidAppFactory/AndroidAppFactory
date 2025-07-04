package com.bihe0832.android.common.compose.common

import android.app.Application
import com.bihe0832.android.common.compose.mvi.BaseViewModel


open class CommonActionViewModel(application: Application) :
    BaseViewModel<CommonActionEvent, CommonActionState, CommonActionEffect>(application) {

    override fun setInitialState(): CommonActionState {
        return getCommonActionState(getApplication())
    }

    override suspend fun handleEventsWithinScope(event: CommonActionEvent): Int {
        when (event) {

            is CommonActionEvent.LoadingStart -> {
                setEffect {
                    CommonActionEffect.Loading(event.text)
                }
            }

            CommonActionEvent.SimpleLoadingStart -> {
                setEffect {
                    CommonActionEffect.Loading(getCurrentState().loadingMsg)
                }

            }

            is CommonActionEvent.LoadingFinished -> {
                setEffect {
                    CommonActionEffect.LoadingSuccess(getCurrentState().loadingSuccess)
                }
            }
        }
        return 0
    }

    override suspend fun reportEventWithinScope(
        event: CommonActionEvent, errCode: Int, useTimeMills: Long
    ) {

    }


}