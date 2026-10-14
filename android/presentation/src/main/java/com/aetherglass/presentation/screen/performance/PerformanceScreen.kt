package com.aetherglass.presentation.screen.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aetherglass.core.design.components.GlassCard
import com.aetherglass.core.design.components.GlassCardSectioned
import com.aetherglass.core.design.components.GlassMetricCard
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

@Composable
fun PerformanceScreen(
    viewModel: PerformanceViewModel,
    modifier: Modifier = Modifier
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    when {
        device == null -> {
            EmptyState(
                modifier = modifier.fillMaxSize(),
                icon = {
                    androidx.compose.material.icons.outlined.MonitorHeart(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                        tint = colors.warning
                    )
                },
                title = "No Device Selected",
                message = "Select a device from setup to view performance metrics"
            )
        }
        metrics == null -> {
            LoadingState(
                modifier = modifier.fillMaxSize(),
                message = "Loading performance data…"
            )
        }
        metrics is Result.Failure -> {
            ErrorState(
                modifier = modifier.fillMaxSize(),
                title = "Error Loading Data",
                message = metrics.error?.message ?? "Unknown error",
                retryLabel = "Retry",
                onRetry = { viewModel.refresh() }
            )
        }
        metrics is Result.Success -> {
            val systemMetrics = metrics.value
            PerformanceContent(
                modifier = modifier,
                device = device,
                metrics = systemMetrics,
                connectionStatus = connectionStatus
            )
        }
    }
}

@Composable
private fun PerformanceContent(
    modifier: Modifier,
    device: Device,
    metrics: SystemMetrics,
    connectionStatus: DomainConnectionStatus
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.CardGap)
    ) {
        // Connection status
        val connectionStatusUi = when (connectionStatus) {
            DomainConnectionStatus.Connected -> ConnectionStatus.Connected
            DomainConnectionStatus.Connecting -> ConnectionStatus.Connecting
            DomainConnectionStatus.Disconnected -> ConnectionStatus.Disconnected
            DomainConnectionStatus.Reconnecting -> ConnectionStatus.Reconnecting
            DomainConnectionStatus.Unknown -> ConnectionStatus.Unknown
        }
        ConnectionStatusCard(
            modifier = Modifier.fillMaxWidth(),
            status = connectionStatusUi,
            label = device.name,
            subtitle = "Performance metrics • Updated ${metrics.timestamp.formatRelativeTime()}"
        )

        // CPU Details
        GlassCardSectioned(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CPU", style = typography.headlineMedium, color = colors.primaryText)
                    Text(text = "${metrics.cpu.cores} cores", style = typography.bodySmall, color = colors.mutedText)
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    MetricRow("Usage", metrics.cpu.usagePercent.formatPercent(1), metrics.cpu.status)
                    metrics.cpu.frequencyMHz?.let { MetricRow("Frequency", "${it} MHz", com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.cpu.temperatureCelsius?.let { MetricRow("Temperature", "${it.formatPercent(1)}°C", if (it > 80) com.aetherglass.domain.model.MetricStatus.Warning else if (it > 90) com.aetherglass.domain.model.MetricStatus.Danger else com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.cpu.loadAverage1m?.let { MetricRow("Load (1m)", it.formatPercent(2), com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.cpu.loadAverage5m?.let { MetricRow("Load (5m)", it.formatPercent(2), com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.cpu.loadAverage15m?.let { MetricRow("Load (15m)", it.formatPercent(2), com.aetherglass.domain.model.MetricStatus.Normal) }
                }
            }
        )

        // Memory Details
        GlassCardSectioned(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Memory", style = typography.headlineMedium, color = colors.primaryText)
                    Text(text = "${(metrics.memory.totalBytes / (1024*1024*1024))} GB total", style = typography.bodySmall, color = colors.mutedText)
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    MetricRow("Used", "${metrics.memory.usedBytes.formatBytes()} / ${metrics.memory.totalBytes.formatBytes()}", metrics.memory.status)
                    MetricRow("Available", metrics.memory.availableBytes.formatBytes(), com.aetherglass.domain.model.MetricStatus.Normal)
                    if (metrics.memory.swapTotalBytes > 0) {
                        MetricRow("Swap Used", "${metrics.memory.swapUsedBytes.formatBytes()} / ${metrics.memory.swapTotalBytes.formatBytes()}", com.aetherglass.domain.model.MetricStatus.Normal)
                    }
                }
            }
        )

        // Sensors
        GlassCardSectioned(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Text(text = "Sensors", style = typography.headlineMedium, color = colors.primaryText)
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    metrics.sensors.cpuTemperatureCelsius?.let { MetricRow("CPU Temp", "${it.formatPercent(1)}°C", if (it > 80) com.aetherglass.domain.model.MetricStatus.Warning else if (it > 90) com.aetherglass.domain.model.MetricStatus.Danger else com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.sensors.gpuTemperatureCelsius?.let { MetricRow("GPU Temp", "${it.formatPercent(1)}°C", if (it > 80) com.aetherglass.domain.model.MetricStatus.Warning else if (it > 90) com.aetherglass.domain.model.MetricStatus.Danger else com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.sensors.fanSpeedRpm?.let { MetricRow("Fan Speed", "$it RPM", com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.sensors.gpuUtilizationPercent?.let { MetricRow("GPU Usage", it.formatPercent(1), com.aetherglass.domain.model.MetricStatus.Normal) }
                    metrics.sensors.gpuMemoryUsedBytes?.let { MetricRow("GPU Memory", "${it.formatBytes()} / ${metrics.sensors.gpuMemoryTotalBytes?.formatBytes() ?: "N/A"}", com.aetherglass.domain.model.MetricStatus.Normal) }
                }
            }
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String, status: com.aetherglass.domain.model.MetricStatus) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    val statusColor = when (status) {
        com.aetherglass.domain.model.MetricStatus.Normal -> colors.success
        com.aetherglass.domain.model.MetricStatus.Warning -> colors.warning
        com.aetherglass.domain.model.MetricStatus.Danger -> colors.danger
        com.aetherglass.domain.model.MetricStatus.Unknown -> colors.mutedText
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = typography.bodyMedium, color = colors.secondaryText)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.Xs), verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = typography.monoMedium, color = colors.primaryText, fontWeight = FontWeight(500))
            androidx.compose.ui.graphics.Color.mutedText?.let { }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(8.dp)
                    .background(statusColor, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }
    }
}