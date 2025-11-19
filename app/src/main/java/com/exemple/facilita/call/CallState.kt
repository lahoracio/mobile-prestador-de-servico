package com.exemple.facilita.call

sealed class CallState {
    object Idle : CallState()
    object Outgoing : CallState()
    object Incoming : CallState()
    object Active : CallState()
    object Ended : CallState()
}