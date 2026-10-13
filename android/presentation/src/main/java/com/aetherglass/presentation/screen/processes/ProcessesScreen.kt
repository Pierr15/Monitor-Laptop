package com.aetherglass.presentation.screen.processes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aetherglass.core.GlassCard
import com.aetherglass.core.GlassCardSectioned
import com.aetherglass.core.design.GlassSpacing
import com.aetherglass.core.design.components.ConnectionStatusCard
import com.aetherglass.core.design.components.ConnectionStatus
import com.aetherglass.core.design.components.LoadingState
import com.aetherglass.core.design.components.EmptyState
import com.aetherglass.core.design.components.ErrorState
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTypography
import com.aetherglass.domain.model.ConnectionStatus as DomainConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.ProcessInfo
import com.aetherglass.util.Result

@Composable
fun ProcessesScreen(
    viewModel: ProcessesViewModel,
    modifier: Modifier = Modifier
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val processes by viewModel.processes.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    when {
        device == null -> {
            EmptyState(
                modifier = modifier.fillMaxSize(),
                icon = {
                    androidx.compose.material.icons.outlined.Apps(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                        tint = colors.warning
                    )
                },
                title = "No Device Selected",
                message = "Select a device from setup to view processes"
            )
        }
        processes == null -> {
            LoadingState(
                modifier = modifier.fillMaxSize(),
                message = "Loading processes…"
            )
        }
        processes is Result.Failure -> {
            ErrorState(
                modifier = modifier.fillMaxSize(),
                title = "Error Loading Processes",
                message = processes.error?.message ?? "Unknown error",
                retryLabel = "Retry",
                onRetry = { viewModel.refresh() }
            )
        }
        processes is Result.Success -> {
            val processList = processes.value
            ProcessesContent(
                modifier = modifier,
                device = device,
                processes = processList,
                connectionStatus = connectionStatus,
                onTerminate = { pid, force -> viewModel.terminateProcess(pid, force) }
            )
        }
    }
}

@Composable
private fun ProcessesContent(
    modifier: Modifier,
    device: Device,
    processes: List<ProcessInfo>,
    connectionStatus: DomainConnectionStatus,
    onTerminate: (Int, Boolean) -> Unit
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    val sortedProcesses = processes.sortedByDescending { it.cpuPercent }

    GlassCardSectioned(
        modifier = modifier.fillMaxSize().padding(spacing.ScreenPadding),
        header = {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material.icons.outlined.Apps(
                        modifier = Modifier.size(spacing.IconMd),
                        tint = colors.primaryText
                    )
                    Text(text = "Processes (${processes.size})", style = typography.headlineMedium, color = colors.primaryText)
                }
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Xs)
                ) {
                    items(sortedProcesses) { process ->
                        ProcessRow(
                            process = process,
                            colors = colors,
                            spacing = spacing,
                            typography = typography,
                            onTerminate = { onTerminate(process.pid, it) }
                        )
                    }
                }
            }
        )
}

@Composable
private fun ProcessRow(
    process: ProcessInfo,
    colors: com.aetherglass.core.design.GlassColorScheme,
    spacing: GlassSpacing,
    typography: com.aetherglass.core.design.GlassTypographyScheme,
    onTerminate: (Boolean) -> Unit
) {
    val canTerminate = process.isTerminatable

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0,
        padding = spacing.Sm
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(spacing.IconSm)) {
                    androidx.compose.material.icons.outlined.Memory(
                        modifier = Modifier.size(spacing.IconSm),
                        tint = if (process.isSystem) colors.mutedText else colors.primaryText
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = process.name, style = typography.bodyMedium, color = colors.primaryText, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                    Text(text = "PID: ${process.pid} • ${process.user ?: "Unknown"}", style = typography.bodySmall, color = colors.mutedText)
                }
            }
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = process.cpuPercent.formatPercent(1), style = typography.monoMedium, color = colors.primaryText)
                Text(text = process.memoryBytes.formatBytes(), style = typography.monoMedium, color = colors.secondaryText)
                if (canTerminate) {
                    com.aetherglass.core.design.components.GlassButton(
                        modifier = Modifier.width(80.dp).height(32.dp),
                        label = { Text(text = "End", style = typography.labelSmall, color = colors.Obsidian) },
                        onClick = { onTerminate(false) },
                        variant = com.aetherglass.core.design.components.Variant.Danger
                    )
                } else {
                    Box(modifier = Modifier.width(80.dp).height(32.dp)) {
                        Text(text = "System", style = typography.labelSmall, color = colors.mutedText)
                    }
                }
            }
        }
    }
}