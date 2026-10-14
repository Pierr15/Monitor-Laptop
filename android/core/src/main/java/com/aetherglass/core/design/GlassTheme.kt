package com.aetherglass.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aether Glass complete theme configuration.
 * Dark monochrome glassmorphism only - no light theme support.
 */
@Immutable
data class GlassThemeConfig(
    val colors: GlassColorScheme = GlassColorScheme.Default,
    val typography: GlassTypographyScheme = GlassTypographyScheme.Default,
    val shapes: GlassShapeScheme = GlassShapeScheme.Default,
    val spacing: GlassSpacing = GlassSpacing,
    val elevation: GlassElevation = GlassElevation.Default,
    val motion: GlassMotion = GlassMotion.Default,
) {
    companion object {
        val Default = GlassThemeConfig()
    }
}

/**
 * Material 3 ColorScheme derived from GlassColorScheme for interop.
 */
internal fun GlassColorScheme.toMaterialColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = PrimaryText,
        onPrimary = Obsidian,
        primaryContainer = Graphite,
        onPrimaryContainer = PrimaryText,
        secondary = SecondaryText,
        onSecondary = Obsidian,
        secondaryContainer = Panel,
        onSecondaryContainer = PrimaryText,
        tertiary = Success,
        onTertiary = Obsidian,
        tertiaryContainer = Graphite,
        onTertiaryContainer = Success,
        error = Danger,
        onError = PrimaryText,
        errorContainer = Danger.copy(alpha = 0.2f),
        onErrorContainer = Danger,
        background = Obsidian,
        onBackground = PrimaryText,
        surface = Panel,
        onSurface = PrimaryText,
        surfaceVariant = Graphite,
        onSurfaceVariant = SecondaryText,
        outline = GlassBorder,
        outlineVariant = GlassBorder.copy(alpha = 0.08f),
        shadow = ShadowAmbient,
        scrim = Scrim,
        inverseSurface = PrimaryText,
        inverseOnSurface = Obsidian,
        inversePrimary = PrimaryText,
    )
}

/**
 * Elevation system for glass surfaces.
 */
@Immutable
data class GlassElevation(
    val level0: Shadow = Shadow.Elevation0,           // Flat
    val level1: Shadow = Shadow(0.dp, 1.dp, GlassColors.ShadowAmbient),   // Subtle
    val level2: Shadow = Shadow(0.dp, 4.dp, GlassColors.ShadowAmbient),   // Card
    val level3: Shadow = Shadow(0.dp, 8.dp, GlassColors.ShadowAmbient),   // Elevated
    val level4: Shadow = Shadow(0.dp, 16.dp, GlassColors.ShadowKey),      // Modal
    val level5: Shadow = Shadow(0.dp, 24.dp, GlassColors.ShadowKey),      // Overlay
) {
    companion object {
        val Default = GlassElevation()
    }
}

/**
 * Motion system for consistent animations.
 */
@Immutable
data class GlassMotion(
    val fast: Int = 150,      // ms - micro interactions
    val medium: Int = 250,    // ms - standard transitions
    val slow: Int = 350,      // ms - complex transitions
    val springDamping: Float = 0.8f,
    val springStiffness: Float = 400f,
) {
    companion object {
        val Default = GlassMotion()
    }
}

/**
 * CompositionLocal for GlassThemeConfig.
 */
val LocalGlassTheme = staticCompositionLocalOf { GlassThemeConfig.Default }

/**
 * Main GlassTheme composable - wraps Material3 theme with our custom tokens.
 */
@Composable
fun GlassTheme(
    config: GlassThemeConfig = GlassThemeConfig.Default,
    content: @Composable () -> Unit
) {
    val materialColors = config.colors.toMaterialColorScheme()

    MaterialTheme(
        colorScheme = materialColors,
        typography = androidx.compose.material3.Typography(
            displayLarge = config.typography.displayLarge,
            displayMedium = config.typography.displayMedium,
            displaySmall = config.typography.displaySmall,
            headlineLarge = config.typography.headlineLarge,
            headlineMedium = config.typography.headlineMedium,
            headlineSmall = config.typography.headlineSmall,
            titleLarge = config.typography.titleLarge,
            titleMedium = config.typography.titleMedium,
            titleSmall = config.typography.titleSmall,
            bodyLarge = config.typography.bodyLarge,
            bodyMedium = config.typography.bodyMedium,
            bodySmall = config.typography.bodySmall,
            labelLarge = config.typography.labelLarge,
            labelMedium = config.typography.labelMedium,
            labelSmall = config.typography.labelSmall,
        ),
        shapes = androidx.compose.material3.Shapes(
            extraSmall = config.shapes.extraSmall,
            small = config.shapes.small,
            medium = config.shapes.medium,
            large = config.shapes.large,
            extraLarge = config.shapes.extraLarge,
        ),
        content = {
            CompositionLocalProvider(
                LocalGlassColors provides config.colors,
                LocalGlassTypography provides config.typography,
                LocalGlassShapes provides config.shapes,
                LocalGlassTheme provides config,
                LocalGlassElevation provides config.elevation,
                LocalGlassMotion provides config.motion,
            ) {
                content()
            }
        }
    )
}

/**
 * CompositionLocals for elevation and motion.
 */
val LocalGlassElevation = staticCompositionLocalOf { GlassElevation.Default }
val LocalGlassMotion = staticCompositionLocalOf { GlassMotion.Default }

/**
 * Extension functions for easy access in composables.
 */
@Composable
fun glassColors(): GlassColorScheme = LocalGlassColors.current

@Composable
fun glassTypography(): GlassTypographyScheme = LocalGlassTypography.current

@Composable
fun glassShapes(): GlassShapeScheme = LocalGlassShapes.current

@Composable
fun glassSpacing(): GlassSpacing = LocalGlassTheme.current.spacing

@Composable
fun glassElevation(): GlassElevation = LocalGlassElevation.current

@Composable
fun glassMotion(): GlassMotion = LocalGlassMotion.current

@Composable
fun glassTheme(): GlassThemeConfig = LocalGlassTheme.current