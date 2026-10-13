package com.aetherglass.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import com.aetherglass.core.design.GlassColors
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassElevation
import com.aetherglass.core.design.glassShapes

/**
 * Base glass surface with translucent background, subtle border, and optional blur.
 * All glassmorphism components build on this.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = glassShapes().card,
    elevation: Int = 1, // 0-5
    tint: Color = GlassColors.Transparent,
    borderOpacity: Float = 1.0f,
    enableBlur: Boolean = true,
    content: @Composable (Modifier) -> Unit
) {
    val colors = glassColors()
    val elevationTokens = glassElevation()
    val effectiveShape = shape
    val effectiveElevation = elevation.coerceIn(0, 5)

    Box(
        modifier = modifier
            .background(
                color = colors.surface.copy(alpha = 0.85f).compositeOver(tint),
                shape = effectiveShape
            )
            .border(
                width = 1.dp,
                color = colors.border.copy(alpha = borderOpacity),
                shape = effectiveShape
            )
            .shadow(elevationTokens["level$effectiveElevation"]!!)
            .clip(effectiveShape)
            .then(if (enableBlur) {
                // Apply background blur where supported (Android 12+)
                // Falls back gracefully on unsupported devices
                Modifier.graphicsLayer {
                    // RenderEffect.createBlurEffect requires API 31+
                    // This is handled at runtime in the actual implementation
                }
            } else Modifier)
    ) {
        // Inner highlight for glass effect
        Box(
            modifier = Modifier
                .size(Modifier.fillMaxSize())
                .clip(effectiveShape)
                .background(
                    color = colors.highlight,
                    shape = effectiveShape
                )
                .padding(1.dp)
        ) {
            content(Modifier.padding(1.dp))
        }
    }
}

/**
 * GlassSurface with configurable blur (API 31+).
 * Use GlassSurfaceBlur for explicit blur control.
 */
@Composable
fun GlassSurfaceBlur(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = glassShapes().card,
    elevation: Int = 1,
    tint: Color = GlassColors.Transparent,
    blurRadius: Float = 20f,
    content: @Composable (Modifier) -> Unit
) {
    val colors = glassColors()
    val elevationTokens = glassElevation()

    // Note: Actual blur implementation requires API 31+ and RenderEffect
    // This is a placeholder that applies the visual style without real blur
    // Real implementation would use:
    // Modifier.graphicsLayer { renderEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP) }
    GlassSurface(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        tint = tint,
        enableBlur = false,
        content = content
    )
}

/**
 * GlassCard - elevated card with consistent styling.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Int = 2,
    padding: androidx.compose.ui.unit.Dp = com.aetherglass.core.design.GlassSpacing.CardPadding,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = glassColors()
    val shapes = glassShapes()

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = null
                )
            else Modifier),
        shape = shapes.card,
        elevation = elevation,
        content = { innerModifier ->
            Box(modifier = innerModifier.padding(padding)) {
                content()
            }
        }
    )
}

/**
 * GlassCard with header, content, and optional actions.
 */
@Composable
fun GlassCardSectioned(
    modifier: Modifier = Modifier,
    elevation: Int = 2,
    header: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
    actions: @Composable (() -> Unit)? = null,
    divider: Boolean = true
) {
    GlassCard(modifier = modifier, elevation = elevation) {
        androidx.compose.foundation.layout.Column {
            header?.let {
                Box(modifier = Modifier.fillMaxWidth()) {
                    it()
                }
            }
            if (divider && (header != null || actions != null)) {
                Divider()
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
            if (actions != null) {
                if (divider) Divider()
                Box(modifier = Modifier.fillMaxWidth()) {
                    actions()
                }
            }
        }
    }
}

/**
 * Thin divider for glass cards.
 */
@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = glassColors().divider,
    thickness: androidx.compose.ui.unit.Dp = 0.5.dp
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

/**
 * GlassToolbar - top app bar with glassmorphism.
 */
@Composable
fun GlassToolbar(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    elevation: Int = 1
) {
    val spacing = glassSpacing()
    val typography = glassTypography()

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = spacing.ScreenPadding),
        shape = glassShapes().extraLarge,
        elevation = elevation,
        content = { innerModifier ->
            androidx.compose.foundation.layout.Row(
                modifier = innerModifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationIcon?.let {
                    Box(modifier = Modifier
                        .size(40.dp)
                        .wrapContentSize(Alignment.Center)
                        .clickable { }
                    ) {
                        it()
                    }
                }
                title?.let {
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = spacing.Md)
                        .wrapContentSize(Alignment.Center)
                    ) {
                        it()
                    }
                }
                actions?.let {
                    Box(modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                    ) {
                        it()
                    }
                }
            }
        }
    )
}

/**
 * GlassChip - small interactive chip.
 */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val shapes = glassShapes()

    val backgroundColor = if (selected) colors.selectedOverlay else GlassColors.Transparent
    val textColor = if (selected) colors.primaryText else colors.secondaryText

    GlassSurface(
        modifier = modifier
            .padding(horizontal = spacing.Xs, vertical = spacing.Xs)
            .then(if (onClick != null && enabled) Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
            else Modifier),
        shape = shapes.chip,
        elevation = 0,
        tint = backgroundColor,
        borderOpacity = if (selected) 1.5f else 1.0f,
        content = { innerModifier ->
            androidx.compose.foundation.layout.Row(
                modifier = innerModifier
                    .padding(horizontal = spacing.Sm, vertical = spacing.Xs)
                    .wrapContentSize(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.Xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Box(modifier = Modifier.size(spacing.IconSm)) {
                        androidx.compose.ui.graphics.Color.mutedText?.let { }
                        it()
                    }
                }
                Box(modifier = Modifier.wrapContentSize()) {
                    label()
                }
                trailingIcon?.let {
                    Box(modifier = Modifier.size(spacing.IconSm)) {
                        it()
                    }
                }
            }
        }
    )
}

/**
 * GlassMetricCard - specialized card for displaying a metric value with label.
 */
@Composable
fun GlassMetricCard(
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
    label: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    trend: TrendDirection = TrendDirection.Neutral,
    trendValue: String? = null,
    status: MetricStatus = MetricStatus.Normal,
    onClick: (() -> Unit)? = null
) {
    val colors = glassColors()
    val spacing = glassSpacing()
    val typography = glassTypography()
    val shapes = glassShapes()

    val statusColor = when (status) {
        MetricStatus.Normal -> colors.success
        MetricStatus.Warning -> colors.warning
        MetricStatus.Danger -> colors.danger
        MetricStatus.Unknown -> colors.mutedText
    }

    GlassCard(
        modifier = modifier
            .weight(1f)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        elevation = 1,
        padding = spacing.CardPadding
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            // Header row: icon + label
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(modifier = Modifier
                        .size(spacing.IconMd)
                        .wrapContentSize(Alignment.Center)
                    ) {
                        icon()
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    label()
                }
                if (trend != TrendDirection.Neutral) {
                    TrendIndicator(trend = trend, value = trendValue, color = statusColor)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(spacing.Xs))
            // Value
            Box(modifier = Modifier.fillMaxWidth()) {
                value()
            }
        }
    }
}

enum class TrendDirection { Up, Down, Neutral }
enum class MetricStatus { Normal, Warning, Danger, Unknown }

@Composable
fun TrendIndicator(
    trend: TrendDirection,
    value: String?,
    color: Color = glassColors().success
) {
    val typography = glassTypography()
    val spacing = glassSpacing()

    val (icon, rotation) = when (trend) {
        TrendDirection.Up -> ("▲", 0f)
        TrendDirection.Down -> ("▲", 180f)
        TrendDirection.Neutral -> ("●", 0f)
    }

    Box(
        modifier = Modifier
            .padding(start = spacing.Xs)
            .graphicsLayer { rotationZ = rotation }
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.Xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = icon,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            value?.let {
                androidx.compose.material3.Text(
                    text = it,
                    color = color,
                    style = typography.monoSmall
                )
            }
        }
    }
}

/**
 * GlassDialog - dialog with glassmorphism styling.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null
) {
    // Implementation uses Dialog composable with GlassSurface content
    // Placeholder for actual implementation
    androidx.compose.material3.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassSurface(
            modifier = Modifier
                .width(320.dp)
                .padding(24.dp),
            shape = glassShapes().dialog,
            elevation = 4
        ) { innerModifier ->
            androidx.compose.foundation.layout.Column(
                modifier = innerModifier.padding(24.dp)
            ) {
                title?.let {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        it()
                    }
                }
                content()
                if (confirmButton != null || dismissButton != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        dismissButton?.let {
                            Box(modifier = Modifier.padding(end = 12.dp)) { it() }
                        }
                        confirmButton?.let { it() }
                    }
                }
            }
        }
    }
}

/**
 * GlassBottomSheet - bottom sheet with glassmorphism.
 */
@Composable
fun GlassBottomSheet(
    sheetState: androidx.compose.material3.BottomSheetState,
    sheetContent: @Composable () -> Unit
) {
    androidx.compose.material3.ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                ),
                elevation = 4
            ) { innerModifier ->
                sheetContent()
            }
        },
        sheetShape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp
        )
    ) { contentModifier ->
        // Main content goes here
    }
}