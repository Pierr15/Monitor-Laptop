package com.aetherglass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aetherglass.core.design.components.DockDestination
import com.aetherglass.core.design.components.GlassDockScaffold
import com.aetherglass.presentation.screen.control.ControlScreen
import com.aetherglass.presentation.screen.files.FilesScreen
import com.aetherglass.presentation.screen.home.HomeScreen
import com.aetherglass.presentation.screen.performance.PerformanceScreen
import com.aetherglass.presentation.screen.processes.ProcessesScreen
import com.aetherglass.presentation.screen.setup.SetupScreen
import org.koin.androidx.compose.getViewModel

/**
 * Main navigation host with floating glass dock.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startRoute: String = "setup"
) {
    val selectedDestination by remember { mutableStateOf(DockDestination.Home) }

    GlassDockScaffold(
        selectedDestination = selectedDestination,
        onDestinationClick = { destination ->
            selectedDestination = destination
            navController.navigate(destination.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = startRoute) {
            composable("setup") {
                SetupScreen(viewModel = getViewModel()) { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            composable(DockDestination.Home.route) {
                HomeScreen(viewModel = getViewModel(), modifier = androidx.compose.foundation.layout.Modifier.padding(innerPadding))
            }
            composable(DockDestination.Performance.route) {
                PerformanceScreen(viewModel = getViewModel(), modifier = androidx.compose.foundation.layout.Modifier.padding(innerPadding))
            }
            composable(DockDestination.Processes.route) {
                ProcessesScreen(viewModel = getViewModel(), modifier = androidx.compose.foundation.layout.Modifier.padding(innerPadding))
            }
            composable(DockDestination.Files.route) {
                FilesScreen(viewModel = getViewModel(), modifier = androidx.compose.foundation.layout.Modifier.padding(innerPadding))
            }
            composable(DockDestination.Control.route) {
                ControlScreen(viewModel = getViewModel(), modifier = androidx.compose.foundation.layout.Modifier.padding(innerPadding))
            }
        }
    }
}