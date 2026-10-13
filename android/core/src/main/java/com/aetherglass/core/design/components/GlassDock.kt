package com.aetherglass.core.design.components

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.compose.ui.unit.sp
import com.aetherglass.core.design.GlassColors
import com.aetherglass.core.design.GlassSpacing
import com.aetherglass.core.design.glassColors
import com.aetherglass.core.design.glassElevation
import com.aetherglass.core.design.glassShapes
import com.aetherglass.core.design.glassSpacing
import com.aetherglass.core.design.glassTypography

/**
 * Navigation destinations for the floating dock.
 */
sealed interface DockDestination {
    val route: String
    val icon: @Composable () -> Unit
    val label: String
    val contentDescription: String

    @Composable
    fun Icon()
}

object DockDestination {
    @Composable
    fun HomeIcon() {
        androidx.compose.material.icons.outlined.Home(
            modifier = androidx.compose.ui.Modifier.size(GlassSpacing.IconMd),
            tint = GlassColors.PrimaryText
        )
    }

    @Composable
    fun PerformanceIcon() {
        androidx.compose.material.icons.outlined.ShowChart(
            modifier = androidx.compose.ui.Modifier.size(GlassSpacing.IconMd),
            tint = GlassColors.PrimaryText
        )
    }

    @Composable
    fun ProcessesIcon() {
        androidx.compose.material.icons.outlined.Memory(
            modifier = androidx.compose.ui.Modifier.size(GlassSpacing.IconMd),
            tint = GlassColors.PrimaryText
        )
    }

    @Composable
    fun FilesIcon() {
        androidx.compose.material.icons.outlined.FolderOpen(
            modifier = androidx.compose.ui.Modifier.size(GlassSpacing.IconMd),
            tint = GlassColors.PrimaryText
        )
    }

    @Composable
    fun ControlIcon() {
        androidx.compose.material.icons.outlined.Settings(
            modifier = androidx.compose.ui.Modifier.size(GlassSpacing.IconMd),
            tint = GlassColors.PrimaryText
        )
    }

    data object Home : DockDestination {
        override val route = "home"
        override val icon = DockDestination::HomeIcon
        override val label = "Home"
        override val contentDescription = "Home screen"
        @Composable override fun Icon() = HomeIcon()
    }

    data object Performance : DockDestination {
        override val route = "performance"
        override val icon = DockDestination::PerformanceIcon
        override val label = "Performance"
        override val contentDescription = "Performance metrics"
        @Composable override fun Icon() = PerformanceIcon()
    }

    data object Processes : DockDestination {
        override val route = "processes"
        override val icon = DockDestination::ProcessesIcon
        override val label = "Processes"
        override val contentDescription = "Process list"
        @Composable override fun Icon() = ProcessesIcon()
    }

    data object Files : DockDestination {
        override val route = "files"
        override val icon = DockDestination::FilesIcon
        override val label = "Files"
        override val contentDescription = "File browser"
        @Composable override fun Icon() = FilesIcon()
    }

    data object Control : DockDestination {
        override val route = "control"
        override val icon = DockDestination::ControlIcon
        override val label = "Control"
        override val contentDescription = "Control and settings"
        @Composable override fun Icon() = ControlIcon()
    }

    val all = listOf(Home, Performance, Processes, Files, Control)
}

/**
 * Floating glass dock - macOS-inspired bottom navigation.
 * Floats above content with capsule shape, subtle glassmorphism.
 */
@Composable
fun GlassDock(
    modifier: Modifier = Modifier,
    selectedDestination: DockDestination,
    onDestinationClick: (DockDestination) -> Unit,
    visible: Boolean = true
) {
    val spacing = glassSpacing()
    val colors = glassColors()
    val shapes = glassShapes()
    val elevation = glassElevation()

    // Animate dock visibility
    val visibilityAlpha by androidx.compose.animation.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    val visibilityY by androidx.compose.animation.animateFloatAsState(
        targetValue = if (visible) 0f else spacing.DockHeight.toPx(),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )

    if (!visible && visibilityAlpha < 0.01f) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.InsetBottomDock)
            .graphicsLayer {
                alpha = visibilityAlpha
                translationY = visibilityY
            }
    ) {
        // Dock background with glassmorphism
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = spacing.Xl),
            shape = shapes.dock,
            elevation = 3,
            borderOpacity = 1.2f,
            content = { innerModifier ->
                Row(
                    modifier = innerModifier
                        .height(spacing.DockHeight)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockDestination.all.forEach { destination ->
                        GlassDockItem(
                            destination = destination,
                            isSelected = destination == selectedDestination,
                            onClick = { onDestinationClick(destination) }
                        )
                    }
                }
            }
        )
    }
}

/**
 * Individual dock item with animated selection state.
 */
@Composable
fun GlassDockItem(
    destination: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val spacing = glassSpacing()
    val colors = glassColors()
    val typography = glassTypography()
    val shapes = glassShapes()

    // Animate selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) glassSpacing().DockSelectedScale else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    val iconColor by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )

    val itemModifier = Modifier
        .size(spacing.DockItemSize)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .wrapContentSize(Alignment.Center)
        .clickable(onClick = onClick)
        .semantics {
            contentDescription = destination.contentDescription
            selected = isSelected
        }

    Box(
        modifier = itemModifier,
        contentAlignment = Alignment.Center
    ) {
        // Selection background
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(spacing.DockItemSize)
                    .background(
                        color = colors.surfaceElevated.copy(alpha = backgroundAlpha),
                        shape = shapes.dockItem
                    )
            )
        }

        // Icon
        Box(
            modifier = Modifier
                .size(spacing.IconMd)
                .graphicsLayer {
                    // Subtle color transition
                }
                .wrapContentSize(Alignment.Center)
        ) {
            destination.Icon()
        }

        // Label (only shown when selected or on larger screens)
        // Hidden on mobile to keep dock compact
    }
}

/**
 * GlassDock with labels - for tablet/landscape layouts.
 */
@Composable
fun GlassDockWithLabels(
    modifier: Modifier = Modifier,
    selectedDestination: DockDestination,
    onDestinationClick: (DockDestination) -> Unit,
    visible: Boolean = true
) {
    val spacing = glassSpacing()
    val colors = glassColors()
    val shapes = glassShapes()
    val typography = glassTypography()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.InsetBottomDock)
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = spacing.Xl),
            shape = shapes.dock,
            elevation = 3,
            borderOpacity = 1.2f,
            content = { innerModifier ->
                Row(
                    modifier = innerModifier
                        .height(spacing.DockHeight)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockDestination.all.forEach { destination ->
                        GlassDockItemWithLabel(
                            destination = destination,
                            isSelected = destination == selectedDestination,
                            onClick = { onDestinationClick(destination) }
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun GlassDockItemWithLabel(
    destination: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val spacing = glassSpacing()
    val colors = glassColors()
    val shapes = glassShapes()
    val typography = glassTypography()

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )

    val itemModifier = Modifier
        .padding(horizontal = spacing.DockItemGap)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .wrapContentSize(Alignment.Center)
        .clickable(onClick = onClick)
        .semantics {
            contentDescription = destination.contentDescription
            selected = isSelected
        }

    Box(modifier = itemModifier, contentAlignment = Alignment.Center) {
        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .background(colors.success, shapes.dockItem)
                    .align(Alignment.BottomCenter)
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = spacing.Xs)
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(spacing.IconMd).wrapContentSize(Alignment.Center)) {
                destination.Icon()
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
            androidx.compose.material3.Text(
                text = destination.label,
                color = if (isSelected) colors.primaryText else colors.mutedText,
                style = typography.labelSmall,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight(600) else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
    }
}

/**
 * Scaffold-compatible dock that reserves space for content.
 * Use with GlassScaffold for proper content inset handling.
 */
@Composable
fun GlassDockScaffold(
    scaffoldState: androidx.compose.material3.ScaffoldState = remember { androidx.compose.material3.ScaffoldState() },
    modifier: Modifier = Modifier,
    selectedDestination: DockDestination,
    onDestinationClick: (DockDestination) -> Unit,
    snackbarHost: (@Composable (androidx.compose.material3.SnackbarHostState) -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val spacing = glassSpacing()

    androidx.compose.material3.Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier,
        snackbarHost = snackbarHost,
        bottomBar = {
            // Custom dock replaces standard bottom bar
            GlassDock(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.InsetBottomDock),
                selectedDestination = selectedDestination,
                onDestinationClick = onDestinationClick
            )
        },
        content = { innerPadding ->
            content(innerPadding)
        }
    )
}

/**
 * Bottom sheet state for dock visibility control.
 * Use to hide dock during full-screen flows (auth, critical confirmations).
 */
class DockVisibilityController {
    var isVisible by mutableStateOf(true)
        private set

    fun show() { isVisible = true }
    fun hide() { isVisible = false }
    fun toggle() { isVisible = !isVisible }
}