package com.github.huymaster.textguardian.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.huymaster.textguardian.android.di.Module
import com.github.huymaster.textguardian.android.ui.activity.ErrorActivity
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
import kotlin.system.exitProcess

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
            modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper, SharedModule.utils)
            modules(Module.application, Module.repository, Module.viewModel)
        }
    }

    override fun onCreate() {
        setupExceptionHandler()
        super.onCreate()
        startKoin { init(this@MainApplication) }
        val initJob = CoroutineScope(
            context = Dispatchers.Main + SupervisorJob(),
        ).launch(start = CoroutineStart.LAZY) {
            launch { ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationState) }
            launch { FirebaseApp.initializeApp(this@MainApplication) }
        }
        initJob.start()
    }

    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                Log.e("AppCrash", "Fatal Exception", e)
                val tName = t.name
                val eString = e.stackTraceToString()
                val intent = Intent(this@MainApplication, ErrorActivity::class.java)
                    .putExtra("thread", tName)
                    .putExtra("exception", eString)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Process.killProcess(Process.myPid())
                exitProcess(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}