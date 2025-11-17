package com.github.huymaster.textguardian.android

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.huymaster.textguardian.android.di.Module
import com.github.huymaster.textguardian.core.di.SharedModule
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.qualifier.qualifier

class MainApplication : Application() {
    object ApplicationState : DefaultLifecycleObserver {
        private val _isForegroundState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isForegroundState: StateFlow<Boolean> = _isForegroundState.asStateFlow()
        val isForeground: Boolean
            get() = _isForegroundState.value

        val isForegroundStateQualifier = qualifier("isForegroundState")
        val isForegroundQualifier = qualifier("isForeground")

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            _isForegroundState.update { true }
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            _isForegroundState.update { false }
        }
    }

    companion object {
        fun KoinApplication.init(context: Context) {
            androidLogger()
            androidContext(context)
            modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
            modules(Module.application, Module.repository, Module.viewModel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val handler = CoroutineExceptionHandler { context, throwable ->
            Log.w("MainApplication", "Failed to initialize app", throwable)
        }
        startKoin { init(this@MainApplication) }
        val initJob = CoroutineScope(
            context = Dispatchers.Main + SupervisorJob(),
        ).launch(start = CoroutineStart.LAZY) {
            launch(handler) { ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationState) }
            launch(handler) { FirebaseApp.initializeApp(this@MainApplication) }
        }
        initJob.start()
    }
}