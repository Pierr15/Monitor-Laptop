package com.aetherglass.presentation.screen.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.aetherglass.core.design.components.GlassButton
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTypography
import com.aetherglass.domain.model.Command
import com.aetherglass.domain.model.CommandStatus
import com.aetherglass.domain.model.ConnectionStatus as DomainConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.util.Result

@Composable
fun ControlScreen(
    viewModel: ControlViewModel,
    modifier: Modifier = Modifier
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastCommandResult.collectAsStateWithLifecycle()

    var showPowerDialog by remember { mutableStateOf<Command.PowerActionType?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    when {
        device == null -> {
            EmptyState(
                modifier = modifier.fillMaxSize(),
                icon = {
                    androidx.compose.material.icons.outlined.Settings(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                        tint = colors.warning
                    )
                },
                title = "No Device Selected",
                message = "Select a device from setup to access control functions"
            )
        }
        else -> {
            ControlContent(
                modifier = modifier,
                device = device,
                connectionStatus = connectionStatus,
                lastResult = lastResult,
                onSleep = { showPowerDialog = Command.PowerActionType.Sleep },
                onRestart = { showPowerDialog = Command.PowerActionType.Restart },
                onShutdown = { showPowerDialog = Command.PowerActionType.Shutdown },
                onPing = { viewModel.sendPing() },
                onPowerConfirm = { action, delay ->
                    when (action) {
                        Command.PowerActionType.Sleep -> viewModel.sleep(delay)
                        Command.PowerActionType.Restart -> viewModel.restart(delay)
                        Command.PowerActionType.Shutdown -> viewModel.shutdown(delay)
                    }
                    showPowerDialog = null
                }
            )
        }
    }

    // Power action confirmation dialog
    if (showPowerDialog != null) {
        val action = showPowerDialog!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPowerDialog = null },
            title = { Text(text = "${action.name} Device?", style = typography.headlineMedium, color = colors.primaryText) },
            text = {
                Text(
                    text = "This will ${action.name.toLowerCase()} the remote device. This action cannot be undone.",
                    style = typography.bodyMedium,
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                GlassButton(
                    label = { Text(text = action.name, style = typography.labelLarge, color = colors.Obsidian) },
                    onClick = { onPowerConfirm(action, 0) },
                    variant = if (action == Command.PowerActionType.Shutdown) com.aetherglass.core.design.components.Variant.Danger else com.aetherglass.core.design.components.Variant.Primary
                )
            },
            dismissButton = {
                GlassButton(
                    label = { Text(text = "Cancel", style = typography.labelLarge, color = colors.primaryText) },
                    onClick = { showPowerDialog = null },
                    variant = com.aetherglass.core.design.components.Variant.Secondary
                )
            }
        )
    }
}

@Composable
private fun ControlContent(
    modifier: Modifier,
    device: Device,
    connectionStatus: DomainConnectionStatus,
    lastResult: String?,
    onSleep: () -> Unit,
    onRestart: () -> Unit,
    onShutdown: () -> Unit,
    onPing: () -> Unit,
    onPowerConfirm: (Command.PowerActionType, Int) -> Unit
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
        // Connection status
        ConnectionStatusCard(
            modifier = Modifier.fillMaxWidth(),
            status = connectionStatusUi,
            label = device.name,
            subtitle = "Control panel • ${device.platform}"
        )

        // Power actions
        GlassCardSectioned(
            modifier = Modifier.fillMaxWidth(),
            header = { Text(text = "Power Actions", style = typography.headlineMedium, color = colors.primaryText) },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.Sm)
                    ) {
                        GlassButton(
                            modifier = Modifier.weight(1f),
                            label = { Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material.icons.outlined.Bedtime(modifier = Modifier.size(spacing.IconSm), tint = colors.Obsidian)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(spacing.Xs))
                                Text(text = "Sleep", style = typography.labelLarge, color = colors.Obsidian)
                            }},
                            onClick = onSleep,
                            variant = com.aetherglass.core.design.components.Variant.Primary
                        )
                        GlassButton(
                            modifier = Modifier.weight(1f),
                            label = { Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material.icons.outlined.RestartAlt(modifier = Modifier.size(spacing.IconSm), tint = colors.primaryText)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(spacing.Xs))
                                Text(text = "Restart", style = typography.labelLarge, color = colors.primaryText)
                            }},
                            onClick = onRestart,
                            variant = com.aetherglass.core.design.components.Variant.Secondary
                        )
                        GlassButton(
                            modifier = Modifier.weight(1f),
                            label = { Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material.icons.outlined.PowerSettingsNew(modifier = Modifier.size(spacing.IconSm), tint = colors.Obsidian)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(spacing.Xs))
                                Text(text = "Shutdown", style = typography.labelLarge, color = colors.Obsidian)
                            }},
                            onClick = onShutdown,
                            variant = com.aetherglass.core.design.components.Variant.Danger
                        )
                    }
                }
            }
        )

        // Quick actions
        GlassCardSectioned(
            modifier = Modifier.fillMaxWidth(),
            header = { Text(text = "Quick Actions", style = typography.headlineMedium, color = colors.primaryText) },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    GlassButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material.icons.outlined.NetworkPing(modifier = Modifier.size(spacing.IconSm), tint = colors.primaryText)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(spacing.Xs))
                            Text(text = "Ping Device", style = typography.labelLarge, color = colors.primaryText)
                        }},
                        onClick = onPing,
                        variant = com.aetherglass.core.design.components.Variant.Secondary
                    )
                }
            }
        )

        // Last command result
        lastResult?.let {
            GlassCardSectioned(
                modifier = Modifier.fillMaxWidth(),
                header = { Text(text = "Last Command Result", style = typography.headlineMedium, color = colors.primaryText) },
                content = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = it, style = typography.monoMedium, color = colors.secondaryText)
                    }
                }
            )
        }
    }
}