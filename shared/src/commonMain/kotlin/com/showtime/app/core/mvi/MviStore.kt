package com.showtime.app.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviStore<S : ViewState, I : Intent, M : Mutation, E : Effect>(
    initialState: S,
    private val reducer: Reducer<S, M>
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    // Effects are one-shot — a Channel, NOT StateFlow (so they don't replay on rotation)
    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    // The ONLY public entry point. UI calls this with an Intent.
    abstract fun onIntent(intent: I)

    // Subclasses call this to apply a state change through the pure reducer.
    protected fun mutate(mutation: M) {
        _state.update { current -> reducer.reduce(current, mutation) }
    }

    // Subclasses call this to fire a one-off effect.
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
