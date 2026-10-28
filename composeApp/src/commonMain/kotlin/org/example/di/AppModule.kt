package org.example.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.example.NetworkClient
import org.example.repository.GameRepository
import org.example.repository.api.GameApi
import org.example.project.ui.screens.viewModel.GameViewModel
import org.example.project.ui.screens.viewModel.SettingsViewModel
import org.example.project.Constants
import org.example.storage.GameStorage

val appModule = module {
    // Provide HttpClient from NetworkClient
    single { NetworkClient.httpClient }

    // Storage
    singleOf(::GameStorage)
    
    // API
    single { GameApi(get(), Constants.BASE_URL) }
    
    // Repository
    singleOf(::GameRepository)
    
    // ViewModels
    viewModelOf(::GameViewModel)
    viewModelOf(::SettingsViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}
