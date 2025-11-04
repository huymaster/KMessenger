package com.github.huymaster.textguardian.android.app

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ApplicationEvents private constructor() {
    companion object {
        val INSTANCE = ApplicationEvents()
        operator fun invoke() = INSTANCE
    }

    private val _sessionExpired = MutableSharedFlow<Boolean>(1)
    val sessionExpired get() = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(true)
    }

    fun resetSessionExpired() {
        _sessionExpired.tryEmit(false)
    }
}