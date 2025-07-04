package com.bihe0832.android.common.list.compose.mvi

import android.app.Application
import com.bihe0832.android.common.compose.mvi.BaseViewModel


class CommonListViewModel(application: Application) :
    BaseViewModel<CommonListEvent, CommonListState, CommonListEffect>(application) {
    override fun setInitialState(): CommonListState {
        return CommonListState(canRefresh = true, hasMore = false)
    }

    override suspend fun reportEventWithinScope(
        event: CommonListEvent,
        errCode: Int,
        useTimeMills: Long
    ) {
    }

    override suspend fun handleEventsWithinScope(event: CommonListEvent): Int {
        when (event) {
            CommonListEvent.Refresh -> {
                setState { copy(canRefresh = false, isRefreshing = true) }
                refresh()
            }

            CommonListEvent.InitData -> {
                fetchData()
            }

            CommonListEvent.LoadMore -> {
                loadMore()
            }
        }
        return 0
    }

    open suspend fun fetchData() {
        setState { copy(canRefresh = false, isRefreshing = false) }
    }

    open suspend fun loadMore() {
        fetchData()
    }

    open suspend fun refresh() {
        fetchData()
    }

}