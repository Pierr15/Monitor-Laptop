package com.aetherglass.data

import com.aetherglass.data.api.apiModule
import com.aetherglass.data.demo.DemoDataProvider
import com.aetherglass.data.mapper.DeviceMapper
import com.aetherglass.data.mapper.MetricMapper
import com.aetherglass.data.mapper.ProcessMapper
import com.aetherglass.data.repository.AuthRepositoryImpl
import com.aetherglass.data.repository.DeviceRepositoryImpl
import com.aetherglass.data.repository.NotificationRepositoryImpl
import com.aetherglass.data.repository.PreferencesRepositoryImpl
import com.aetherglass.data.repository.SecureStorageRepositoryImpl
import com.aetherglass.domain.repository.AuthRepository
import com.aetherglass.domain.repository.DeviceRepository
import com.aetherglass.domain.repository.NotificationRepository
import com.aetherglass.domain.repository.PreferencesRepository
import com.aetherglass.domain.repository.SecureStorageRepository
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

/**
 * Koin module for data layer - repositories and implementations.
 */
val DataModule = module {
    // Include API module
    includes(apiModule)

    // Mappers
    single { DeviceMapper }
    single { MetricMapper }
    single { ProcessMapper }

    // Demo data provider
    single { DemoDataProvider() }

    // Repository implementations
    single<PreferencesRepository> { PreferencesRepositoryImpl(androidContext()) }
    single<SecureStorageRepository> { SecureStorageRepositoryImpl(androidContext()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(androidContext()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get(), get(), get()) }
}