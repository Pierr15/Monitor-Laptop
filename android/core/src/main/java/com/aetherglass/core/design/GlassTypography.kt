package com.aetherglass.core.design

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aether Glass typography system.
 * Uses a modern sans-serif stack with clear hierarchy for metrics and labels.
 */
object GlassTypography {

    // Font families - using system fonts for performance
    val FontFamilyDefault = FontFamily.Default
    val FontFamilyMono = FontFamily.Monospace

    // Display styles - for large metric values
    val DisplayLarge = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(600),
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = -0.5.sp,
    )

    val DisplayMedium = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(600),
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = -0.25.sp,
    )

    val DisplaySmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    )

    // Headline styles - for section titles
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(600),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineMedium = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineSmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    )

    // Title styles - for card titles, list items
    val TitleLarge = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    )

    val TitleMedium = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

    val TitleSmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    )

    // Body styles - for descriptions, labels
    val BodyLarge = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

    val BodyMedium = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )

    val BodySmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    )

    // Label styles - for chips, buttons, small UI
    val LabelLarge = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

    val LabelMedium = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )

    val LabelSmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    )

    // Monospace for metrics, codes, timestamps
    val MonoLarge = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    val MonoMedium = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    val MonoSmall = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    // Metric value style - prominent, easy to scan
    val MetricValue = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(600),
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = -0.5.sp,
    )

    val MetricValueSmall = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(600),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = -0.25.sp,
    )

    val MetricLabel = TextStyle(
        fontFamily = FontFamilyDefault,
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    )
}

/**
 * CompositionLocal for GlassTypography.
 */
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class GlassTypographyScheme(
    val displayLarge: TextStyle = GlassTypography.DisplayLarge,
    val displayMedium: TextStyle = GlassTypography.DisplayMedium,
    val displaySmall: TextStyle = GlassTypography.DisplaySmall,
    val headlineLarge: TextStyle = GlassTypography.HeadlineLarge,
    val headlineMedium: TextStyle = GlassTypography.HeadlineMedium,
    val headlineSmall: TextStyle = GlassTypography.HeadlineSmall,
    val titleLarge: TextStyle = GlassTypography.TitleLarge,
    val titleMedium: TextStyle = GlassTypography.TitleMedium,
    val titleSmall: TextStyle = GlassTypography.TitleSmall,
    val bodyLarge: TextStyle = GlassTypography.BodyLarge,
    val bodyMedium: TextStyle = GlassTypography.BodyMedium,
    val bodySmall: TextStyle = GlassTypography.BodySmall,
    val labelLarge: TextStyle = GlassTypography.LabelLarge,
    val labelMedium: TextStyle = GlassTypography.LabelMedium,
    val labelSmall: TextStyle = GlassTypography.LabelSmall,
    val monoLarge: TextStyle = GlassTypography.MonoLarge,
    val monoMedium: TextStyle = GlassTypography.MonoMedium,
    val monoSmall: TextStyle = GlassTypography.MonoSmall,
    val metricValue: TextStyle = GlassTypography.MetricValue,
    val metricValueSmall: TextStyle = GlassTypography.MetricValueSmall,
    val metricLabel: TextStyle = GlassTypography.MetricLabel,
) {
    companion object {
        val Default = GlassTypographyScheme()
    }
}

val LocalGlassTypography = staticCompositionLocalOf { GlassTypographyScheme.Default }