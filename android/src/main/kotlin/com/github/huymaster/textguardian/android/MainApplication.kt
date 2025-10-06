package com.github.huymaster.textguardian.android

import android.app.Application
import com.github.huymaster.textguardian.android.di.Module
import com.github.huymaster.textguardian.core.di.SharedModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    companion object {
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
            modules(Module.application, Module.viewModel)
        }
        FirebaseApp.initializeApp(this)
    }
}