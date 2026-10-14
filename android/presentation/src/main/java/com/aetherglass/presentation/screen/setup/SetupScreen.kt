package com.aetherglass.presentation.screen.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.aetherglass.core.design.components.GlassCard
import com.aetherglass.core.design.components.GlassCardSectioned
import com.aetherglass.core.design.GlassSpacing
import com.aetherglass.core.design.components.ConnectionStatusCard
import com.aetherglass.core.design.components.ConnectionStatus
import com.aetherglass.core.design.components.EmptyState
import com.aetherglass.core.design.components.GlassButton
import com.aetherglass.core.design.components.LoadingState
import com.aetherglass.core.design.components.ErrorState
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTypography
import com.aetherglass.domain.model.Device
import com.aetherglass.util.Result

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    modifier: Modifier = Modifier,
    onDeviceSelected: (String) -> Unit
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val demoMode by remember { mutableStateOf(viewModel.demoMode) }
    val lastRegistered by viewModel.lastRegisteredDevice.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newDeviceName by remember { mutableStateOf("") }
    var newDevicePlatform by remember { mutableStateOf("Windows") }
    var newDeviceAgentVersion by remember { mutableStateOf("1.0.0") }

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.CardGap)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.ScreenPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Device Setup", style = typography.headlineLarge, color = colors.primaryText)
                Text(text = "Manage your monitored Windows devices", style = typography.bodyMedium, color = colors.secondaryText)
            }
        }

        // Demo mode toggle
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.ScreenPadding),
            elevation = 0
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Demo Mode", style = typography.bodyLarge, color = colors.primaryText)
                    Text(text = "Use synthetic data for testing without a real device", style = typography.bodySmall, color = colors.secondaryText)
                }
                androidx.compose.material3.Switch(
                    checked = demoMode,
                    onCheckedChange = { checked ->
                        demoMode = checked
                        viewModel.setDemoMode(checked)
                    },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = colors.success,
                        checkedTrackColor = colors.success.copy(alpha = 0.3f),
                        uncheckedThumbColor = colors.mutedText,
                        uncheckedTrackColor = colors.mutedText.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Error display
        error?.let { err ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.ScreenPadding),
                elevation = 0,
                tint = colors.danger.copy(alpha = 0.1f),
                borderOpacity = 1.5f
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Error: $err", style = typography.bodyMedium, color = colors.danger)
                    GlassButton(
                        label = { Text(text = "Retry", style = typography.labelMedium, color = colors.primaryText) },
                        onClick = { viewModel.loadDevices() },
                        variant = com.aetherglass.core.design.components.Variant.Ghost
                    )
                }
            }
        }

        // Device list
        when {
            isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.ScreenPadding),
                    message = "Loading devices…"
                )
            }
            devices is Result.Success -> {
                val deviceList = devices.value
                if (deviceList.isEmpty()) {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.ScreenPadding),
                        icon = {
                            androidx.compose.material.icons.outlined.AddCircleOutline(
                                modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                                tint = colors.warning
                            )
                        },
                        title = "No Devices Registered",
                        message = "Add your first Windows device to start monitoring",
                        actionLabel = "Add Device",
                        onAction = { showAddDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(spacing.CardGap)
                    ) {
                        items(deviceList) { device ->
                            DeviceCard(
                                device = device,
                                isLastRegistered = lastRegistered?.id == device.id,
                                onClick = { onDeviceSelected(device.id.toString()) },
                                onRemove = { viewModel.removeDevice(device.id) },
                                colors = colors,
                                spacing = spacing,
                                typography = typography
                            )
                        }
                        // Add device button
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.ScreenPadding)
                                .clickable { showAddDialog = true },
                            elevation = 0,
                            padding = spacing.CardPadding
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material.icons.outlined.Add(
                                    modifier = Modifier.size(spacing.IconMd),
                                    tint = colors.success
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(spacing.Sm))
                                Text(text = "Add New Device", style = typography.bodyLarge, color = colors.success)
                            }
                        }
                    }
                }
            }
            devices is Result.Failure -> {
                ErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.ScreenPadding),
                    title = "Failed to Load Devices",
                    message = devices.error?.message ?? "Unknown error",
                    retryLabel = "Retry",
                    onRetry = { viewModel.loadDevices() }
                )
            }
        }
    }

    // Add device dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "Add Device", style = typography.headlineMedium, color = colors.primaryText) },
            text = {
                Column(
                    modifier = Modifier.padding(spacing.CardGap),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(spacing.Sm)
                ) {
                    TextField(
                        value = newDeviceName,
                        onValueChange = { newDeviceName = it },
                        label = { Text(text = "Device Name", style = typography.bodyMedium, color = colors.secondaryText) },
                        placeholder = { Text(text = "My Laptop", style = typography.bodyMedium, color = colors.mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextField(
                        value = newDevicePlatform,
                        onValueChange = { newDevicePlatform = it },
                        label = { Text(text = "Platform", style = typography.bodyMedium, color = colors.secondaryText) },
                        placeholder = { Text(text = "Windows", style = typography.bodyMedium, color = colors.mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextField(
                        value = newDeviceAgentVersion,
                        onValueChange = { newDeviceAgentVersion = it },
                        label = { Text(text = "Agent Version", style = typography.bodyMedium, color = colors.secondaryText) },
                        placeholder = { Text(text = "1.0.0", style = typography.bodyMedium, color = colors.mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    label = { Text(text = "Add", style = typography.labelLarge, color = colors.Obsidian) },
                    onClick = {
                        if (newDeviceName.isNotBlank()) {
                            viewModel.registerDevice(newDeviceName, newDevicePlatform, newDeviceAgentVersion)
                            newDeviceName = ""
                            showAddDialog = false
                        }
                    },
                    variant = com.aetherglass.core.design.components.Variant.Primary
                )
            },
            dismissButton = {
                GlassButton(
                    label = { Text(text = "Cancel", style = typography.labelLarge, color = colors.primaryText) },
                    onClick = { showAddDialog = false },
                    variant = com.aetherglass.core.design.components.Variant.Secondary
                )
            }
        )
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    isLastRegistered: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    colors: com.aetherglass.core.design.GlassColorScheme,
    spacing: GlassSpacing,
    typography: com.aetherglass.core.design.GlassTypographyScheme
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.ScreenPadding),
        elevation = if (isLastRegistered) 2 else 1,
        borderOpacity = if (isLastRegistered) 1.5f else 1.0f
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(spacing.IconLg).wrapContentSize(Alignment.Center)) {
                    androidx.compose.material.icons.outlined.Computer(
                        modifier = Modifier.size(spacing.IconLg),
                        tint = if (device.isOnline) colors.success else colors.danger
                    )
                }
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = device.name, style = typography.titleLarge, color = colors.primaryText)
                        if (device.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colors.success, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Text(text = "${device.platform} • ${device.agentVersion ?: "Unknown"}", style = typography.bodySmall, color = colors.secondaryText)
                    Text(text = "Last seen: ${device.lastSeenAt?.formatRelativeTime() ?: "Never"}", style = typography.bodySmall, color = colors.mutedText)
                }
            }
            androidx.compose.material.icons.outlined.DeleteOutline(
                modifier = Modifier
                    .size(spacing.IconMd)
                    .clickable(onClick = onRemove)
                    .wrapContentSize(Alignment.Center),
                tint = colors.danger
            )
        }
    }
}