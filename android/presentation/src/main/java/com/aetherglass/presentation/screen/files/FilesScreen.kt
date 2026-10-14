package com.aetherglass.presentation.screen.files

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
import com.aetherglass.core.design.components.GlassCard
import com.aetherglass.core.design.components.GlassCardSectioned
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
import com.aetherglass.domain.model.FileEntry
import com.aetherglass.domain.model.FileListing
import com.aetherglass.util.Result

@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    modifier: Modifier = Modifier
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val listing by viewModel.listing.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()

    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    when {
        device == null -> {
            EmptyState(
                modifier = modifier.fillMaxSize(),
                icon = {
                    androidx.compose.material.icons.outlined.FolderOpen(
                        modifier = androidx.compose.ui.Modifier.size(spacing.IconXxl * 2),
                        tint = colors.warning
                    )
                },
                title = "No Device Selected",
                message = "Select a device from setup to browse files"
            )
        }
        listing == null -> {
            LoadingState(
                modifier = modifier.fillMaxSize(),
                message = "Loading files…"
            )
        }
        listing is Result.Failure -> {
            ErrorState(
                modifier = modifier.fillMaxSize(),
                title = "Error Loading Files",
                message = listing.error?.message ?? "Unknown error",
                retryLabel = "Retry",
                onRetry = { viewModel.refresh() }
            )
        }
        listing is Result.Success -> {
            val fileListing = listing.value
            FilesContent(
                modifier = modifier,
                device = device,
                listing = fileListing,
                connectionStatus = connectionStatus,
                onNavigate = { path -> viewModel.browsePath(path) },
                onUp = { viewModel.browseUp() },
                onDownload = { file -> viewModel.downloadFile(file) }
            )
        }
    }
}

@Composable
private fun FilesContent(
    modifier: Modifier,
    device: Device,
    listing: FileListing,
    connectionStatus: DomainConnectionStatus,
    onNavigate: (String) -> Unit,
    onUp: () -> Unit,
    onDownload: (FileEntry) -> Unit
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
                    androidx.compose.material.icons.outlined.FolderOpen(
                        modifier = Modifier.size(spacing.IconMd),
                        tint = colors.primaryText
                    )
                    Text(text = "Files", style = typography.headlineMedium, color = colors.primaryText)
                }
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (listing.parentPath != null) {
                        com.aetherglass.core.design.components.GlassButton(
                            label = { androidx.compose.material.icons.outlined.ArrowUpward(modifier = Modifier.size(spacing.IconSm), tint = colors.primaryText) },
                            onClick = onUp,
                            variant = com.aetherglass.core.design.components.Variant.Ghost
                        )
                    }
                    Text(text = currentPath, style = typography.bodySmall, color = colors.mutedText)
                }
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(spacing.CardPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.Xs)
            ) {
                if (listing.entries.isEmpty()) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.fillMaxWidth().height(100.dp))
                } else {
                    items(listing.entries) { entry ->
                        FileRow(
                            entry = entry,
                            colors = colors,
                            spacing = spacing,
                            typography = typography,
                            onClick = { onNavigate(entry.path) },
                            onDownload = { onDownload(entry) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun FileRow(
    entry: FileEntry,
    colors: com.aetherglass.core.design.GlassColorScheme,
    spacing: GlassSpacing,
    typography: com.aetherglass.core.design.GlassTypographyScheme,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    val isDir = entry.isDirectory

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isDir) onClick else { })
            .padding(spacing.Xs),
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
                Box(modifier = Modifier.size(spacing.IconMd)) {
                    if (isDir) {
                        androidx.compose.material.icons.outlined.Folder(
                            modifier = Modifier.size(spacing.IconMd),
                            tint = colors.warning
                        )
                    } else {
                        androidx.compose.material.icons.outlined.InsertDriveFile(
                            modifier = Modifier.size(spacing.IconMd),
                            tint = colors.primaryText
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = entry.name, style = typography.bodyMedium, color = colors.primaryText, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                    Text(text = entry.modifiedAt.formatAbsoluteDate(), style = typography.bodySmall, color = colors.mutedText)
                }
            }
            if (!isDir) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = entry.size.formatBytes(), style = typography.monoMedium, color = colors.secondaryText)
                    com.aetherglass.core.design.components.GlassButton(
                        modifier = Modifier.width(80.dp).height(32.dp),
                        label = { Text(text = "Download", style = typography.labelSmall, color = colors.Obsidian) },
                        onClick = onDownload,
                        variant = com.aetherglass.core.design.components.Variant.Primary
                    )
                }
            }
        }
    }
}