package com.github.huymaster.textguardian.android.di

import com.github.huymaster.textguardian.android.MainApplication
import com.github.huymaster.textguardian.android.data.repository.AppSettingsManager
import com.github.huymaster.textguardian.android.ui.model.AuthenticationViewModel
import com.github.huymaster.textguardian.android.ui.model.ServerHealthModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object Module {
    val application = module {
        single<MainApplication> { androidApplication() as MainApplication }
        single { AppSettingsManager(get()) }
    }
    val viewModel = module {
        viewModelOf(::ServerHealthModel)
        viewModelOf(::AuthenticationViewModel)
    }
}