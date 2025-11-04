package com.github.huymaster.textguardian.android.di

import com.github.huymaster.textguardian.android.MainApplication
import com.github.huymaster.textguardian.android.app.AppSettingsManager
import com.github.huymaster.textguardian.android.app.ApplicationEvents
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.AuthenticationRepository
import com.github.huymaster.textguardian.android.data.repository.GenericRepository
import com.github.huymaster.textguardian.android.viewmodel.ChatListViewModel
import com.github.huymaster.textguardian.android.viewmodel.InitViewModel
import com.github.huymaster.textguardian.android.viewmodel.LoginViewModel
import com.github.huymaster.textguardian.android.viewmodel.RegisterViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object Module {
    val application = module {
        single<MainApplication> { androidApplication() as MainApplication }
        single { AppSettingsManager.INSTANCE }
        single { ApplicationEvents.INSTANCE }
        single { JWTTokenManager(get()) }
        single(qualifier = MainApplication.ApplicationState.isForegroundStateQualifier) { MainApplication.ApplicationState.isForegroundState }
        single(qualifier = MainApplication.ApplicationState.isForegroundQualifier) { MainApplication.ApplicationState.isForeground }
    }
    val repository = module {
        single { GenericRepository(get()) }
        single { AuthenticationRepository(get(), get()) }
    }
    val viewModel = module {
        viewModelOf(::InitViewModel)
        viewModelOf(::RegisterViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::ChatListViewModel)
    }
}