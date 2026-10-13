package com.aetherglass.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aetherglass.core.design.GlassColors
import com.aetherglass.core.design.GlassSpacing
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassShapes
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTypography

/**
 * Connection status indicator with colored dot and label.
 */
@Composable
fun ConnectionStatusCard(
    modifier: Modifier = Modifier,
    status: ConnectionStatus,
    label: String? = null,
    subtitle: String? = null,
    showIndicator: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()
    val shapes = glassShapes()

    val (indicatorColor, statusText) = when (status) {
        ConnectionStatus.Connected -> colors.success to "Connected"
        ConnectionStatus.Connecting -> colors.warning to "Connecting…"
        ConnectionStatus.Disconnected -> colors.danger to "Disconnected"
        ConnectionStatus.Reconnecting -> colors.warning to "Reconnecting…"
        ConnectionStatus.Unknown -> colors.mutedText to "Unknown"
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shapes.card,
        elevation = 1,
        padding = spacing.CardPadding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIndicator) {
                Box(
                    modifier = Modifier.size(spacing.IconMd).wrapContentSize(Alignment.Center)
                ) {
                    ConnectionIndicator(
                        color = indicatorColor,
                        animating = status == ConnectionStatus.Connecting || status == ConnectionStatus.Reconnecting
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = label ?: statusText,
                        color = colors.primaryText,
                        style = typography.titleMedium
                    )
                    if (status == ConnectionStatus.Connecting || status == ConnectionStatus.Reconnecting) {
                        LoadingSpinner(size = 12.dp, color = indicatorColor)
                    }
                }
                subtitle?.let {
                    androidx.compose.material3.Text(
                        text = it,
                        color = colors.secondaryText,
                        style = typography.bodySmall
                    )
                }
            }

            androidx.compose.material3.Text(
                text = statusText,
                color = indicatorColor,
                style = typography.labelMedium,
                fontWeight = FontWeight(500)
            )
        }
    }
}

enum class ConnectionStatus {
    Connected, Connecting, Disconnected, Reconnecting, Unknown
}

@Composable
fun ConnectionIndicator(
    color: Color = glassColors().success,
    size: androidx.compose.ui.unit.Dp = 12.dp,
    animating: Boolean = false
) {
    val alpha by androidx.compose.animation.animateFloatAsState(
        targetValue = if (animating) 0.4f else 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { this.alpha = alpha }
            .background(color, RoundedCornerShape(GlassSpacing.RadiusFull))
    )
}

@Composable
fun LoadingSpinner(
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: Color = glassColors().primaryText,
    strokeWidth: androidx.compose.ui.unit.Dp = 2.dp
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(size)
    ) {
        val center = size.toPx() / 2f
        val radius = (size.toPx() - strokeWidth.toPx()) / 2f
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx()),
            style = androidx.compose.ui.draw.Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            rotationZ = rotation
        )
    }
}

/**
 * Status indicator dot - simple colored circle.
 */
@Composable
fun StatusIndicator(
    modifier: Modifier = Modifier,
    status: StatusState,
    size: androidx.compose.ui.unit.Dp = 10.dp,
    showLabel: Boolean = false,
    label: String? = null
) {
    val colors = glassColors()
    val typography = glassTypography()

    val indicatorColor = when (status) {
        StatusState.Healthy -> colors.success
        StatusState.Degraded -> colors.warning
        StatusState.Critical -> colors.danger
        StatusState.Unknown -> colors.mutedText
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(indicatorColor, RoundedCornerShape(GlassSpacing.RadiusFull))
        )
        if (showLabel) {
            androidx.compose.material3.Text(
                text = label ?: status.label,
                color = colors.secondaryText,
                style = typography.bodySmall
            )
        }
    }
}

enum class StatusState(val label: String) {
    Healthy("Healthy"),
    Degraded("Degraded"),
    Critical("Critical"),
    Unknown("Unknown")
}

/**
 * Empty state with illustration and action.
 */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit),
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    GlassSurface(
        modifier = modifier.fillMaxSize(),
        shape = glassShapes().card,
        elevation = 0,
        borderOpacity = 0.5f,
        content = { innerModifier ->
            Column(
                modifier = innerModifier
                    .fillMaxSize()
                    .padding(spacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.IconXxl * 2)
                        .wrapContentSize(Alignment.Center)
                ) {
                    icon()
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Lg))
                androidx.compose.material3.Text(
                    text = title,
                    color = colors.primaryText,
                    style = typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Xs))
                androidx.compose.material3.Text(
                    text = message,
                    color = colors.secondaryText,
                    style = typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
                if (actionLabel != null && onAction != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Lg))
                    GlassButton(
                        label = { androidx.compose.material3.Text(text = actionLabel) },
                        onClick = onAction
                    )
                }
            }
        }
    )
}

/**
 * Error state with retry action.
 */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    retryLabel: String = "Retry",
    onRetry: () -> Unit,
    dismissLabel: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    GlassSurface(
        modifier = modifier.fillMaxSize(),
        shape = glassShapes().card,
        elevation = 0,
        borderOpacity = 0.5f,
        content = { innerModifier ->
            Column(
                modifier = innerModifier
                    .fillMaxSize()
                    .padding(spacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.IconXxl * 2)
                        .wrapContentSize(Alignment.Center)
                ) {
                    androidx.compose.material.icons.outlined.ErrorOutline(
                        modifier = Modifier.size(spacing.IconXxl * 2),
                        tint = colors.danger
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Lg))
                androidx.compose.material3.Text(
                    text = title,
                    color = colors.primaryText,
                    style = typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Xs))
                androidx.compose.material3.Text(
                    text = message,
                    color = colors.secondaryText,
                    style = typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Lg))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Md)
                ) {
                    GlassButton(
                        label = { androidx.compose.material3.Text(text = retryLabel) },
                        onClick = onRetry,
                        variant = GlassButton.Variant.Primary
                    )
                    onDismiss?.let {
                        GlassButton(
                            label = { androidx.compose.material3.Text(text = dismissLabel ?: "Dismiss") },
                            onClick = it,
                            variant = GlassButton.Variant.Secondary
                        )
                    }
                }
            }
        }
    )
}

/**
 * Offline state - specialized empty state for no connection.
 */
@Composable
fun OfflineState(
    modifier: Modifier = Modifier,
    message: String = "Device appears to be offline. Check your connection and try again.",
    onRetry: () -> Unit
) {
    EmptyState(
        modifier = modifier,
        icon = {
            androidx.compose.material.icons.outlined.CloudOff(
                modifier = Modifier.size(GlassSpacing.IconXxl * 2),
                tint = glassColors().warning
            )
        },
        title = "Offline",
        message = message,
        actionLabel = "Try Again",
        onAction = onRetry
    )
}

/**
 * Loading state with spinner.
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading…",
    showSpinner: Boolean = true
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    GlassSurface(
        modifier = modifier.fillMaxSize(),
        shape = glassShapes().card,
        elevation = 0,
        borderOpacity = 0.3f,
        content = { innerModifier ->
            Column(
                modifier = innerModifier
                    .fillMaxSize()
                    .padding(spacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (showSpinner) {
                    LoadingSpinner(size = 48.dp, color = colors.primaryText)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Lg))
                }
                androidx.compose.material3.Text(
                    text = message,
                    color = colors.secondaryText,
                    style = typography.bodyMedium
                )
            }
        }
    )
}

/**
 * Stale data indicator - shows when data is cached but not fresh.
 */
@Composable
fun StaleDataBanner(
    modifier: Modifier = Modifier,
    lastUpdated: String,
    onRefresh: () -> Unit
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = glassShapes().card,
        elevation = 0,
        tint = colors.warning.copy(alpha = 0.1f),
        borderOpacity = 0.8f,
        content = { innerModifier ->
            Row(
                modifier = innerModifier
                    .fillMaxWidth()
                    .padding(spacing.Md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material.icons.outlined.Info(
                        modifier = Modifier.size(spacing.IconSm),
                        tint = colors.warning
                    )
                    Column {
                        androidx.compose.material3.Text(
                            text = "Data may be outdated",
                            color = colors.primaryText,
                            style = typography.labelMedium,
                            fontWeight = FontWeight(500)
                        )
                        androidx.compose.material3.Text(
                            text = "Last updated: $lastUpdated",
                            color = colors.secondaryText,
                            style = typography.bodySmall
                        )
                    }
                }
                GlassButton(
                    label = { androidx.compose.material3.Text(text = "Refresh") },
                    onClick = onRefresh,
                    variant = GlassButton.Variant.Secondary
                )
            }
        }
    )
}

/**
 * GlassButton variants.
 */
@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
    variant: Variant = Variant.Primary,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val shapes = glassShapes()
    val typography = glassTypography()

    val (backgroundColor, textColor, borderOpacity) = when (variant) {
        Variant.Primary -> colors.success to colors.Obsidian to 0f
        Variant.Secondary -> colors.surfaceElevated to colors.primaryText to 1f
        Variant.Danger -> colors.danger to colors.PrimaryText to 0f
        Variant.Ghost -> GlassColors.Transparent to colors.primaryText to 0f
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = spacing.Lg)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        shape = shapes.button,
        elevation = if (variant == Variant.Primary) 1 else 0,
        tint = backgroundColor,
        borderOpacity = borderOpacity,
        content = { innerModifier ->
            Row(
                modifier = innerModifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = spacing.Lg),
                horizontalArrangement = Arrangement.spacedBy(spacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Box(modifier = Modifier.size(spacing.IconSm).wrapContentSize(Alignment.Center)) {
                        it()
                    }
                }
                Box(modifier = Modifier.weight(1f).wrapContentSize(Alignment.Center)) {
                    label()
                }
                trailingIcon?.let {
                    Box(modifier = Modifier.size(spacing.IconSm).wrapContentSize(Alignment.Center)) {
                        it()
                    }
                }
            }
        }
    )
}

enum class Variant { Primary, Secondary, Danger, Ghost }