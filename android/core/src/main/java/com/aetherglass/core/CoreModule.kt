package com.aetherglass.core

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aetherglass.core.design.GlassTheme
import com.aetherglass.core.design.GlassThemeConfig
import com.aetherglass.core.design.components.GlassBottomSheet
import com.aetherglass.core.design.components.GlassCard
import com.aetherglass.core.design.components.GlassCardSectioned
import com.aetherglass.core.design.components.GlassChip
import com.aetherglass.core.design.components.GlassDialog
import com.aetherglass.core.design.components.GlassDock
import com.aetherglass.core.design.components.GlassDockItem
import com.aetherglass.core.design.components.GlassDockItemWithLabel
import com.aetherglass.core.design.components.GlassDockScaffold
import com.aetherglass.core.design.components.GlassDockWithLabels
import com.aetherglass.core.design.components.DockDestination
import com.aetherglass.core.design.components.DockVisibilityController
import com.aetherglass.core.design.components.GlassMetricCard
import com.aetherglass.core.design.components.GlassSurface
import com.aetherglass.core.design.components.GlassSurfaceBlur
import com.aetherglass.core.design.components.GlassToolbar
import com.aetherglass.core.design.components.LoadingState
import com.aetherglass.core.design.components.EmptyState
import com.aetherglass.core.design.components.ErrorState
import com.aetherglass.core.design.components.OfflineState
import com.aetherglass.core.design.components.StaleDataBanner
import com.aetherglass.core.design.components.ConnectionStatusCard
import com.aetherglass.core.design.components.StatusIndicator
import com.aetherglass.core.design.components.GlassButton
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassElevation
import com.aetherglass.core.design.glassMotion
import com.aetherglass.core.design.glassShapes
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTheme
import com.aetherglass.core.design.glassTypography
import com.aetherglass.core.security.BiometricAuth
import com.aetherglass.core.security.KeystoreManager
import com.aetherglass.core.security.SecurityConfig
import com.aetherglass.core.util.ExtensionsKt
import com.aetherglass.core.util.TimberTree
import timber.log.Timber
import org.koin.dsl.module

/**
 * Core module initialization and exports.
 * Call `CoreModule.initialize(context)` in Application.onCreate()
 */
object CoreModule {

    private var _initialized = false

    fun initialize(context: Context) {
        if (_initialized) return

        // Initialize Timber logging
        Timber.plant(TimberTree())

        // Initialize security
        SecurityConfig.initialize(context)

        _initialized = true
        Timber.tag("CoreModule").i("Core module initialized")
    }

    fun isInitialized(): Boolean = _initialized
}

/**
 * Koin module for core components.
 */
val coreKoinModule = module {
    single { BiometricAuth(androidContext()) }
    single { KeystoreManager(androidContext()) }
    single { SecurityConfig }
}

/**
 * Production crash reporting tree (placeholder - integrate with your crash reporter).
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // TODO: Integrate with Firebase Crashlytics, Sentry, etc.
        if (priority >= android.util.Log.WARN) {
            // Report to crash tracking service
        }
    }
}

/**
 * Extension functions for ViewModel access.
 */
@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> viewModel(
    key: String? = null,
    viewModelStoreOwner: androidx.lifecycle.ViewModelStoreOwner = androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner.current,
    factory: ViewModelProvider.Factory? = null
): T = viewModel(key, viewModelStoreOwner, factory)

/**
 * Re-export all design system components for convenience.
 * Usage: import com.aetherglass.core.GlassCard
 */
@Composable
fun GlassTheme(
    config: GlassThemeConfig = GlassThemeConfig.Default,
    content: @Composable () -> Unit
) = com.aetherglass.core.design.GlassTheme(config, content)

// Design tokens
export com.aetherglass.core.design.GlassColors
export com.aetherglass.core.design.GlassColorScheme
export com.aetherglass.core.design.GlassTypography
export com.aetherglass.core.design.GlassTypographyScheme
export com.aetherglass.core.design.GlassSpacing
export com.aetherglass.core.design.GlassShapes
export com.aetherglass.core.design.GlassShapeScheme
export com.aetherglass.core.design.GlassElevation
export com.aetherglass.core.design.GlassMotion
export com.aetherglass.core.design.GlassThemeConfig

// Components
export com.aetherglass.core.design.components.GlassSurface
export com.aetherglass.core.design.components.GlassSurfaceBlur
export com.aetherglass.core.design.components.GlassCard
export com.aetherglass.core.design.components.GlassCardSectioned
export com.aetherglass.core.design.components.GlassChip
export com.aetherglass.core.design.components.GlassDock
export com.aetherglass.core.design.components.GlassDockItem
export com.aetherglass.core.design.components.GlassDockItemWithLabel
export com.aetherglass.core.design.components.GlassDockWithLabels
export com.aetherglass.core.design.components.GlassDockScaffold
export com.aetherglass.core.design.components.DockDestination
export com.aetherglass.core.design.components.DockVisibilityController
export com.aetherglass.core.design.components.GlassMetricCard
export com.aetherglass.core.design.components.GlassToolbar
export com.aetherglass.core.design.components.GlassDialog
export com.aetherglass.core.design.components.GlassBottomSheet
export com.aetherglass.core.design.components.GlassButton
export com.aetherglass.core.design.components.ConnectionStatusCard
export com.aetherglass.core.design.components.StatusIndicator
export com.aetherglass.core.design.components.LoadingState
export com.aetherglass.core.design.components.EmptyState
export com.aetherglass.core.design.components.ErrorState
export com.aetherglass.core.design.components.OfflineState
export com.aetherglass.core.design.components.StaleDataBanner
export com.aetherglass.core.design.components.LoadingSpinner
export com.aetherglass.core.design.components.ConnectionIndicator

// State types
export com.aetherglass.core.design.components.ConnectionStatus
export com.aetherglass.core.design.components.StatusState
export com.aetherglass.core.design.components.TrendDirection
export com.aetherglass.core.design.components.MetricStatus
export com.aetherglass.core.design.components.Variant

// Security
export com.aetherglass.core.security.BiometricAuth
export com.aetherglass.core.security.KeystoreManager
export com.aetherglass.core.security.SecurityConfig

// Utilities
export com.aetherglass.core.util.ExtensionsKt
export com.aetherglass.core.util.Result
export com.aetherglass.core.util.runCatching
export com.aetherglass.core.util.safeLaunch

// Logging
export timber.log.Timber