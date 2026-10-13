package com.aetherglass.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.aetherglass.core.GlassTheme
import com.aetherglass.presentation.navigation.AppNavHost
import com.aetherglass.presentation.screen.setup.SetupViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    private val setupViewModel: SetupViewModel by getViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GlassTheme {
                AppNavHost(
                    startRoute = if (setupViewModel.demoMode) "home" else "setup"
                ) { route ->
                    setupViewModel.setActiveDevice(java.util.UUID.fromString(route))
                }
            }
        }
    }
}