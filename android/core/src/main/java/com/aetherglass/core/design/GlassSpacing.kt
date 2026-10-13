package com.aetherglass.core.design

import androidx.compose.ui.unit.dp

/**
 * Aether Glass spacing and sizing tokens.
 * Based on 4dp grid with consistent scale.
 */
object GlassSpacing {

    // Base unit: 4dp
    val Xxs = 2.dp
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Xxxl = 48.dp
    val Xxxxl = 64.dp

    // Component-specific
    val CardPadding = Lg           // 16dp
    val CardGap = Md               // 12dp between cards
    val SectionGap = Xl            // 24dp between sections
    val ScreenPadding = Lg         // 16dp screen edges
    val DockItemPadding = 12.dp    // Horizontal padding in dock
    val DockItemGap = 4.dp         // Gap between dock items
    val InsetBottomDock = 16.dp    // Dock float above bottom
    val DialogPadding = Xl         // 24dp dialog padding
    val BottomSheetPadding = Xl    // 24dp bottom sheet padding

    // Touch targets
    val MinTouchTarget = 48.dp     // Minimum accessible touch target
    val ComfortableTouchTarget = 56.dp

    // Icon sizes
    val IconXs = 16.dp
    val IconSm = 20.dp
    val IconMd = 24.dp
    val IconLg = 28.dp
    val IconXl = 32.dp
    val IconXxl = 48.dp

    // Dock
    val DockHeight = 64.dp
    val DockItemSize = 44.dp
    val DockSelectedScale = 1.08f

    // Border widths
    val BorderHairline = 0.5.dp
    val BorderThin = 1.dp
    val BorderMedium = 1.5.dp

    // Corner radii
    val RadiusXs = 4.dp
    val RadiusSm = 8.dp
    val RadiusMd = 12.dp
    val RadiusLg = 16.dp
    val RadiusXl = 24.dp
    val RadiusFull = 9999.dp
}

/**
 * Glass shape tokens for consistent rounded corners.
 */
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.unit.dp

object GlassShapes {

    val ExtraSmall = RoundedCornerShape(GlassSpacing.RadiusXs)
    val Small = RoundedCornerShape(GlassSpacing.RadiusSm)
    val Medium = RoundedCornerShape(GlassSpacing.RadiusMd)
    val Large = RoundedCornerShape(GlassSpacing.RadiusLg)
    val ExtraLarge = RoundedCornerShape(GlassSpacing.RadiusXl)
    val Full = RoundedCornerShape(GlassSpacing.RadiusFull)

    // Component-specific
    val Card = Large        // 16dp
    val Chip = Full         // Pill
    val Dock = ExtraLarge   // 24dp capsule
    val DockItem = Full     // Pill
    val Dialog = ExtraLarge // 24dp
    val BottomSheet = ExtraLarge // 24dp top corners
    val Button = Full       // Pill
    val Fab = Full          // Circle
    val Sheet = ExtraLarge  // 24dp
}

/**
 * CompositionLocal for GlassShapes.
 */
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class GlassShapeScheme(
    val extraSmall: Shape = GlassShapes.ExtraSmall,
    val small: Shape = GlassShapes.Small,
    val medium: Shape = GlassShapes.Medium,
    val large: Shape = GlassShapes.Large,
    val extraLarge: Shape = GlassShapes.ExtraLarge,
    val full: Shape = GlassShapes.Full,
    val card: Shape = GlassShapes.Card,
    val chip: Shape = GlassShapes.Chip,
    val dock: Shape = GlassShapes.Dock,
    val dockItem: Shape = GlassShapes.DockItem,
    val dialog: Shape = GlassShapes.Dialog,
    val bottomSheet: Shape = GlassShapes.BottomSheet,
    val button: Shape = GlassShapes.Button,
    val fab: Shape = GlassShapes.Fab,
    val sheet: Shape = GlassShapes.Sheet,
) {
    companion object {
        val Default = GlassShapeScheme()
    }
}

val LocalGlassShapes = staticCompositionLocalOf { GlassShapeScheme.Default }