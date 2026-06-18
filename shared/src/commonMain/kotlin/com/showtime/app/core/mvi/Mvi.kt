package com.showtime.app.core.mvi

// Marker interfaces make the four MVI roles explicit and searchable.
interface ViewState              // immutable snapshot the UI renders
interface Intent                 // something the user/UI wants to happen
interface Effect                 // one-off side event (navigate, toast)

// A Reducer is a PURE function: (oldState, partial change) -> newState.
// No coroutines, no IO, no side effects. This purity is what earns the points.
fun interface Reducer<S : ViewState, M : Mutation> {
    fun reduce(state: S, mutation: M): S
}

// Mutations are the only thing allowed to change state.
// Intents may trigger IO; that IO produces Mutations; the Reducer applies them.
interface Mutation
