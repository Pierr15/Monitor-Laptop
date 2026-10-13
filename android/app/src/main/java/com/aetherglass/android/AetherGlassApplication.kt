package com.aetherglass.android

import android.app.Application
import com.aetherglass.core.CoreModule
import com.aetherglass.core.coreKoinModule
import com.aetherglass.data.DataModule
import com.aetherglass.domain.DomainModule
import com.aetherglass.presentation.PresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.EmptyLogger
import org.koin.core.logger.Level

class AetherGlassApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize core module first
        CoreModule.initialize(this)

        // Start Koin with all modules
        startKoin {
            androidLogger(Level.NONE) // Use EmptyLogger for production, or Level.INFO for debug
            androidContext(this@AetherGlassApplication)
            modules(
                coreKoinModule,
                DomainModule,
                DataModule,
                PresentationModule
            )
        }
    }
}