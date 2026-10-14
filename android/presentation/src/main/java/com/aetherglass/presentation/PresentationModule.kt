package com.aetherglass.presentation

import com.aetherglass.presentation.screen.control.ControlViewModel
import com.aetherglass.presentation.screen.files.FilesViewModel
import com.aetherglass.presentation.screen.home.HomeViewModel
import com.aetherglass.presentation.screen.performance.PerformanceViewModel
import com.aetherglass.presentation.screen.processes.ProcessesViewModel
import com.aetherglass.presentation.screen.setup.SetupViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for presentation layer - ViewModels and navigation.
 */
val PresentationModule = module {
    // ViewModels
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PerformanceViewModel(get(), get(), get()) }
    viewModel { ProcessesViewModel(get(), get(), get()) }
    viewModel { FilesViewModel(get(), get(), get()) }
    viewModel { ControlViewModel(get(), get(), get(), get()) }
    viewModel { SetupViewModel(get(), get(), get()) }

}