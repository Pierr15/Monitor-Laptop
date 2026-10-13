package com.aetherglass.core.design

import androidx.compose.ui.graphics.Color

/**
 * Aether Glass color tokens for dark monochrome glassmorphism.
 * All colors are defined for dark theme only - this app does not support light theme.
 */
object GlassColors {

    // Base surfaces
    val Obsidian = Color(0xFF08090B)       // Base background - deepest black
    val Graphite = Color(0xFF17181B)       // Elevated surfaces, selected states
    val Panel = Color(0xFF202226)          // Glass surface base

    // Glass borders
    val GlassBorder = Color.White.copy(alpha = 0.16f)  // 16% opacity white border
    val GlassBorderStrong = Color.White.copy(alpha = 0.22f) // 22% for emphasis

    // Text hierarchy
    val PrimaryText = Color(0xFFF4F4F5)    // Headlines, key metrics
    val SecondaryText = Color(0xFFA8A9AD)  // Labels, descriptions
    val MutedText = Color(0xFF777A80)      // Timestamps, hints, disabled

    // Status colors (restrained, not neon)
    val Success = Color(0xFF22C55E)        // Connected, healthy
    val Warning = Color(0xFFF59E0B)        // Degraded, caution
    val Danger = Color(0xFFEF4444)         // Critical, offline

    // Overlay scrim
    val Scrim = Color.Black.copy(alpha = 0.5f)
    val ScrimStrong = Color.Black.copy(alpha = 0.7f)

    // Highlight for glass inner edge
    val GlassHighlight = Color.White.copy(alpha = 0.08f)

    // Shadow colors
    val ShadowAmbient = Color.Black.copy(alpha = 0.3f)
    val ShadowKey = Color.Black.copy(alpha = 0.2f)

    // Interactive states
    val PressedOverlay = Color.White.copy(alpha = 0.06f)
    val HoverOverlay = Color.White.copy(alpha = 0.04f)
    val FocusOverlay = Color.White.copy(alpha = 0.12f)
    val SelectedOverlay = Color.White.copy(alpha = 0.10f)

    // Divider
    val Divider = Color.White.copy(alpha = 0.08f)

    // Input/Field
    val FieldBackground = Graphite
    val FieldBorder = GlassBorder
    val FieldBorderFocus = GlassBorderStrong
    val PlaceholderText = MutedText

    // Special
    val Transparent = Color.Transparent
}

/**
 * Semantic color roles mapped to tokens.
 * Use these in components instead of raw tokens.
 */
@Immutable
data class GlassColorScheme(
    val background: Color = GlassColors.Obsidian,
    val surface: Color = GlassColors.Panel,
    val surfaceElevated: Color = GlassColors.Graphite,
    val surfaceContainer: Color = GlassColors.Graphite,
    val border: Color = GlassColors.GlassBorder,
    val borderStrong: Color = GlassColors.GlassBorderStrong,
    val highlight: Color = GlassColors.GlassHighlight,
    val primaryText: Color = GlassColors.PrimaryText,
    val secondaryText: Color = GlassColors.SecondaryText,
    val mutedText: Color = GlassColors.MutedText,
    val success: Color = GlassColors.Success,
    val warning: Color = GlassColors.Warning,
    val danger: Color = GlassColors.Danger,
    val scrim: Color = GlassColors.Scrim,
    val pressedOverlay: Color = GlassColors.PressedOverlay,
    val hoverOverlay: Color = GlassColors.HoverOverlay,
    val focusOverlay: Color = GlassColors.FocusOverlay,
    val selectedOverlay: Color = GlassColors.SelectedOverlay,
    val divider: Color = GlassColors.Divider,
    val fieldBackground: Color = GlassColors.FieldBackground,
    val fieldBorder: Color = GlassColors.FieldBorder,
    val fieldBorderFocus: Color = GlassColors.FieldBorderFocus,
    val placeholderText: Color = GlassColors.PlaceholderText,
    val shadowAmbient: Color = GlassColors.ShadowAmbient,
    val shadowKey: Color = GlassColors.ShadowKey,
) {
    companion object {
        val Default = GlassColorScheme()
    }
}

/**
 * CompositionLocal for GlassColorScheme to allow theming overrides.
 */
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalGlassColors = staticCompositionLocalOf { GlassColorScheme.Default }