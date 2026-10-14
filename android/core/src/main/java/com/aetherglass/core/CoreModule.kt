package com.aetherglass.core

import android.content.Context
import com.aetherglass.core.security.BiometricAuth
import com.aetherglass.core.security.KeystoreManager
import com.aetherglass.core.security.SecurityConfig
import com.aetherglass.core.util.TimberTree
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber

/**
 * Initializes process-wide core utilities.
 */
object CoreModule {
    @Volatile
    private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        Timber.plant(TimberTree())
        SecurityConfig.initialize(context.applicationContext)
        initialized = true
        Timber.tag("CoreModule").i("Core module initialized")
    }

    fun isInitialized(): Boolean = initialized
}

val coreKoinModule = module {
    single { BiometricAuth(androidContext()) }
    single { KeystoreManager(androidContext()) }
    single { SecurityConfig }
}

/**
 * Placeholder hook for a future crash-reporting integration.
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Integrate a crash reporter only after choosing a provider and reviewing its data policy.
    }
}
