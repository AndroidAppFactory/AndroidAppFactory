package com.bihe0832.android.common.compose.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

abstract class BaseViewModel<Event : ViewEvent, State : ViewState, Effect : ViewSideEffect> :
    ViewModel() {

    private val initialState: State by lazy { setInitialState() }
    abstract fun setInitialState(): State

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)

    // make it read only
    val uiState: StateFlow<State> = _uiState

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()

    // make it read only
    val event = _event.asSharedFlow()

    private val _effect: Channel<Effect> = Channel()

    // make it read only
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    @OptIn(ExperimentalTime::class)
    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                measureTimedValue {
                    handleEventsWithinScope(it)
                }.let { timedValue ->
                    reportEventWithinScope(
                        it, timedValue.value, timedValue.duration.inWholeMilliseconds
                    )
                }
            }
        }
    }

    /**
     * 处理View层发出的事件，统一输入源
     */
    abstract suspend fun handleEventsWithinScope(event: Event): Int

    /**
     * 上报View层发出的事件，以供数据分析
     * 独立出来可以起到强提醒的作用
     */
    abstract suspend fun reportEventWithinScope(event: Event, errCode: Int, useTimeMills: Long)

    fun sendEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    fun setState(reducer: State.() -> State) {
        val newState = uiState.value.reducer()
        _uiState.value = newState
    }

    fun getCurrentState(): State {
        return uiState.value
    }

    fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

}
