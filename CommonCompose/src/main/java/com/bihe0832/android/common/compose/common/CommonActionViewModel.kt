package com.bihe0832.android.common.compose.common

import com.bihe0832.android.common.compose.mvi.BaseViewModel
import com.bihe0832.android.common.compose.mvi.ViewSideEffect
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog

open class CommonActionViewModel :
    BaseViewModel<CommonActionEvent, CommonActionState, ViewSideEffect>() {

    override fun setInitialState(): CommonActionState {
        return getCommonActionState(ZixieContext.applicationContext!!)
    }


    open fun refresh() {

    }

    open fun fetchData() {

    }

    fun loadFinished() {
        setState { copy(canRefresh = true, isLoading = false, errorMsg = "") }
    }

    fun loadError(msg: String) {
        setState { copy(canRefresh = true, isLoading = false, errorMsg = msg) }
    }

    override suspend fun handleEventsWithinScope(event: CommonActionEvent): Int {
        ZLog.d("hardy", "handleEventsWithinScope:$event")
        when (event) {
            CommonActionEvent.Refresh -> {
                setState { copy(canRefresh = false, isLoading = true, errorMsg = "") }
                refresh()
            }

            CommonActionEvent.InitData -> {
                setState { copy(canRefresh = false, isLoading = true, errorMsg = "") }
                fetchData()
            }
        }
        return 0
    }

    override suspend fun reportEventWithinScope(
        event: CommonActionEvent,
        errCode: Int,
        useTimeMills: Long
    ) {

    }

}
