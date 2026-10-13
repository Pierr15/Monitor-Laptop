package com.aetherglass.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aetherglass.core.GlassCard
import com.aetherglass.core.GlassMetricCard
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
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSetup: (() -> Unit)? = null
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    when {
        device == null -> {
            // No device selected - show setup prompt
            EmptyState(
                modifier = modifier.fillMaxSize(),
                icon = {
                    androidx.compose.material.icons.outlined.AddCircle(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                        tint = colors.warning
                    )
                },
                title = "No Device Connected",
                message = "Add a Windows device to start monitoring",
                actionLabel = "Setup Device",
                onAction = onNavigateToSetup
            )
        }
        metrics == null -> {
            // Loading metrics
            LoadingState(
                modifier = modifier.fillMaxSize(),
                message = "Connecting to ${device.name}…"
            )
        }
        metrics is Result.Failure -> {
            // Error loading metrics
            ErrorState(
                modifier = modifier.fillMaxSize(),
                title = "Connection Error",
                message = "Failed to load metrics from ${device.name}",
                retryLabel = "Retry",
                onRetry = { viewModel.refresh() },
                dismissLabel = "Setup New Device",
                onDismiss = onNavigateToSetup
            )
        }
        metrics is Result.Success -> {
            // Show metrics
            val systemMetrics = metrics.value
            HomeMetricsContent(
                modifier = modifier,
                device = device,
                metrics = systemMetrics,
                connectionStatus = connectionStatus,
                onRefresh = { viewModel.refresh() }
            )
        }
    }
}

@Composable
private fun HomeMetricsContent(
    modifier: Modifier,
    device: Device,
    metrics: SystemMetrics,
    connectionStatus: DomainConnectionStatus,
    onRefresh: () -> Unit
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    val connectionStatusUi = when (connectionStatus) {
        DomainConnectionStatus.Connected -> ConnectionStatus.Connected
        DomainConnectionStatus.Connecting -> ConnectionStatus.Connecting
        DomainConnectionStatus.Disconnected -> ConnectionStatus.Disconnected
        DomainConnectionStatus.Reconnecting -> ConnectionStatus.Reconnecting
        DomainConnectionStatus.Unknown -> ConnectionStatus.Unknown
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.CardGap)
    ) {
        // Connection status card
        ConnectionStatusCard(
            modifier = Modifier.fillMaxWidth(),
            status = connectionStatusUi,
            label = device.name,
            subtitle = "Last seen: ${metrics.timestamp.formatRelativeTime()}"
        )

        // Primary metrics grid
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.CardGap)
        ) {
            GlassMetricCard(
                modifier = Modifier.weight(1f),
                value = {
                    Text(
                        text = metrics.cpu.usagePercent.formatPercent(1),
                        color = colors.primaryText,
                        style = typography.metricValue
                    )
                },
                label = { Text(text = "CPU", style = typography.metricLabel, color = colors.secondaryText) },
                icon = {
                    androidx.compose.material.icons.outlined.Memory(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                        tint = colors.primaryText
                    )
                },
                trend = when (metrics.cpu.status) {
                    com.aetherglass.domain.model.MetricStatus.Normal -> com.aetherglass.core.design.components.TrendDirection.Neutral
                    com.aetherglass.domain.model.MetricStatus.Warning -> com.aetherglass.core.design.components.TrendDirection.Up
                    com.aetherglass.domain.model.MetricStatus.Danger -> com.aetherglass.core.design.components.TrendDirection.Up
                    else -> com.aetherglass.core.design.components.TrendDirection.Neutral
                },
                status = metrics.cpu.status
            )

            GlassMetricCard(
                modifier = Modifier.weight(1f),
                value = {
                    Text(
                        text = metrics.memory.usagePercent.formatPercent(1),
                        color = colors.primaryText,
                        style = typography.metricValue
                    )
                },
                label = { Text(text = "Memory", style = typography.metricLabel, color = colors.secondaryText) },
                icon = {
                    androidx.compose.material.icons.outlined.Storage(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                        tint = colors.primaryText
                    )
                },
                trend = when (metrics.memory.status) {
                    com.aetherglass.domain.model.MetricStatus.Normal -> com.aetherglass.core.design.components.TrendDirection.Neutral
                    com.aetherglass.domain.model.MetricStatus.Warning -> com.aetherglass.core.design.components.TrendDirection.Up
                    com.aetherglass.domain.model.MetricStatus.Danger -> com.aetherglass.core.design.components.TrendDirection.Up
                    else -> com.aetherglass.core.design.components.TrendDirection.Neutral
                },
                status = metrics.memory.status
            )
        }

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.CardGap)
        ) {
            metrics.battery?.let { battery ->
                GlassMetricCard(
                    modifier = Modifier.weight(1f),
                    value = {
                        Text(
                            text = "${battery.levelPercent}%",
                            color = colors.primaryText,
                            style = typography.metricValue
                        )
                    },
                    label = { Text(text = "Battery", style = typography.metricLabel, color = colors.secondaryText) },
                    icon = {
                        androidx.compose.material.icons.outlined.BatteryStd(
                            modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                            tint = if (battery.isCharging) colors.success else colors.primaryText
                        )
                    },
                    status = battery.status
                )
            }

            // Storage - show primary volume
            metrics.storage.firstOrNull()?.let { storage ->
                GlassMetricCard(
                    modifier = Modifier.weight(1f),
                    value = {
                        Text(
                            text = storage.usagePercent.formatPercent(1),
                            color = colors.primaryText,
                            style = typography.metricValue
                        )
                    },
                    label = { Text(text = "Storage", style = typography.metricLabel, color = colors.secondaryText) },
                    icon = {
                        androidx.compose.material.icons.outlined.HardDrive(
                            modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                            tint = colors.primaryText
                        )
                    },
                    status = storage.status
                )
            }
        }

        // Network & Agent health
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.CardGap)
        ) {
            GlassMetricCard(
                modifier = Modifier.weight(1f),
                value = {
                    Text(
                        text = metrics.network.latencyMs?.let { "${it}ms" } ?? "N/A",
                        color = colors.primaryText,
                        style = typography.metricValue
                    ),
                    label = { Text(text = "Latency", style = typography.metricLabel, color = colors.secondaryText) },
                    icon = {
                        androidx.compose.material.icons.outlined.NetworkCheck(
                            modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                            tint = if (metrics.network.isConnected) colors.success else colors.danger
                        )
                    },
                    status = if (metrics.network.isConnected) com.aetherglass.domain.model.MetricStatus.Normal else com.aetherglass.domain.model.MetricStatus.Danger
                )
            )

            GlassMetricCard(
                modifier = Modifier.weight(1f),
                value = {
                    Text(
                        text = metrics.agent.version,
                        color = colors.primaryText,
                        style = typography.metricValueSmall
                    )
                },
                label = { Text(text = "Agent", style = typography.metricLabel, color = colors.secondaryText) },
                icon = {
                    androidx.compose.material.icons.outlined.Computer(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconMd),
                        tint = if (metrics.agent.isHealthy) colors.success else colors.warning
                    ),
                    status = if (metrics.agent.isHealthy) com.aetherglass.domain.model.MetricStatus.Normal else com.aetherglass.domain.model.MetricStatus.Warning
                )
            )
        }
    }
}