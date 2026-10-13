package com.aetherglass.presentation

import com.aetherglass.presentation.navigation.AppNavHost
import com.aetherglass.presentation.screen.control.ControlScreen
import com.aetherglass.presentation.screen.control.ControlViewModel
import com.aetherglass.presentation.screen.files.FilesScreen
import com.aetherglass.presentation.screen.files.FilesViewModel
import com.aetherglass.presentation.screen.home.HomeScreen
import com.aetherglass.presentation.screen.home.HomeViewModel
import com.aetherglass.presentation.screen.performance.PerformanceScreen
import com.aetherglass.presentation.screen.performance.PerformanceViewModel
import com.aetherglass.presentation.screen.processes.ProcessesScreen
import com.aetherglass.presentation.screen.processes.ProcessesViewModel
import com.aetherglass.presentation.screen.setup.SetupScreen
import com.aetherglass.presentation.screen.setup.SetupViewModel
import org.koin.dsl.module

/**
 * Koin module for presentation layer - ViewModels and navigation.
 */
val PresentationModule = module {
    // ViewModels
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PerformanceViewModel(get(), get()) }
    viewModel { ProcessesViewModel(get()) }
    viewModel { FilesViewModel(get()) }
    viewModel { ControlViewModel(get(), get(), get(), get()) }
    viewModel { SetupViewModel(get(), get(), get(), get()) }

    // Navigation
    single { AppNavHost() }
}