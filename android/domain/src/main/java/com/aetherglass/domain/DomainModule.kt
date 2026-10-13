package com.aetherglass.domain

import com.aetherglass.domain.usecase.AuthUseCase
import com.aetherglass.domain.usecase.CommandUseCase
import com.aetherglass.domain.usecase.ConnectionUseCase
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.FileUseCase
import com.aetherglass.domain.usecase.MetricsUseCase
import com.aetherglass.domain.usecase.NotificationUseCase
import com.aetherglass.domain.usecase.PowerUseCase
import com.aetherglass.domain.usecase.PreferencesUseCase
import com.aetherglass.domain.usecase.ProcessUseCase
import org.koin.dsl.module

/**
 * Koin module for domain layer - use cases.
 */
val DomainModule = module {
    // Use cases
    single { AuthUseCase(get(), get()) }
    single { DeviceUseCase(get(), get()) }
    single { MetricsUseCase(get()) }
    single { ProcessUseCase(get()) }
    single { FileUseCase(get()) }
    single { PowerUseCase(get()) }
    single { CommandUseCase(get()) }
    single { ConnectionUseCase(get()) }
    single { NotificationUseCase(get()) }
    single { PreferencesUseCase(get()) }
}