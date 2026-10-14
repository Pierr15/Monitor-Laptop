package com.aetherglass.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.aetherglass.core.GlassTheme
import com.aetherglass.presentation.navigation.AppNavHost
import com.aetherglass.presentation.screen.setup.SetupViewModel
import org.koin.android.ext.android.inject
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val setupViewModel: SetupViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GlassTheme {
                AppNavHost(
                    startRoute = if (setupViewModel.demoMode) "home" else "setup"
                ) { route ->
                    runCatching { UUID.fromString(route) }
                        .getOrNull()
                        ?.let(setupViewModel::setActiveDevice)
                }
            }
        }
    }
}
