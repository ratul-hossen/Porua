package com.porua.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class PoruaColorTokens(
    val primary: Color,
    val primaryStrong: Color,
    val primarySoft: Color,
    val onPrimary: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val ai: Color,
    val aiSoft: Color,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color
)

val LocalPoruaColors = staticCompositionLocalOf {
    PoruaColorTokens(
        primary = CrimsonPrimaryLight,
        primaryStrong = CrimsonPrimaryStrongLight,
        primarySoft = CrimsonPrimarySoftLight,
        onPrimary = CrimsonOnPrimaryLight,
        heroStart = CrimsonHeroStartLight,
        heroEnd = CrimsonHeroEndLight,
        ai = CrimsonAiLight,
        aiSoft = CrimsonAiSoftLight,
        bg = CrimsonBgLight,
        surface = CrimsonSurfaceLight,
        surface2 = CrimsonSurface2Light,
        border = CrimsonBorderLight,
        text = CrimsonTextLight,
        textMuted = CrimsonTextMutedLight,
        success = StatusSuccess,
        successSoft = StatusSuccessSoft,
        warning = StatusWarning,
        warningSoft = StatusWarningSoft,
        danger = StatusDanger,
        dangerSoft = StatusDangerSoft
    )
}

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimaryLight,
    onPrimary = CrimsonOnPrimaryLight,
    primaryContainer = CrimsonPrimarySoftLight,
    onPrimaryContainer = CrimsonPrimaryStrongLight,
    background = CrimsonBgLight,
    onBackground = CrimsonTextLight,
    surface = CrimsonSurfaceLight,
    onSurface = CrimsonTextLight,
    surfaceVariant = CrimsonSurface2Light,
    onSurfaceVariant = CrimsonTextMutedLight,
    outline = CrimsonBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimaryDark,
    onPrimary = CrimsonOnPrimaryDark,
    primaryContainer = CrimsonPrimarySoftDark,
    onPrimaryContainer = CrimsonPrimaryStrongDark,
    background = CrimsonBgDark,
    onBackground = CrimsonTextDark,
    surface = CrimsonSurfaceDark,
    onSurface = CrimsonTextDark,
    surfaceVariant = CrimsonSurface2Dark,
    onSurfaceVariant = CrimsonTextMutedDark,
    outline = CrimsonBorderDark
)

@Composable
fun PoruaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val poruaTokens = if (darkTheme) {
        PoruaColorTokens(
            primary = CrimsonPrimaryDark,
            primaryStrong = CrimsonPrimaryStrongDark,
            primarySoft = CrimsonPrimarySoftDark,
            onPrimary = CrimsonOnPrimaryDark,
            heroStart = CrimsonHeroStartDark,
            heroEnd = CrimsonHeroEndDark,
            ai = CrimsonAiDark,
            aiSoft = CrimsonAiSoftDark,
            bg = CrimsonBgDark,
            surface = CrimsonSurfaceDark,
            surface2 = CrimsonSurface2Dark,
            border = CrimsonBorderDark,
            text = CrimsonTextDark,
            textMuted = CrimsonTextMutedDark,
            success = StatusSuccessDark,
            successSoft = StatusSuccessSoftDark,
            warning = StatusWarningDark,
            warningSoft = StatusWarningSoftDark,
            danger = StatusDangerDark,
            dangerSoft = StatusDangerSoftDark
        )
    } else {
        PoruaColorTokens(
            primary = CrimsonPrimaryLight,
            primaryStrong = CrimsonPrimaryStrongLight,
            primarySoft = CrimsonPrimarySoftLight,
            onPrimary = CrimsonOnPrimaryLight,
            heroStart = CrimsonHeroStartLight,
            heroEnd = CrimsonHeroEndLight,
            ai = CrimsonAiLight,
            aiSoft = CrimsonAiSoftLight,
            bg = CrimsonBgLight,
            surface = CrimsonSurfaceLight,
            surface2 = CrimsonSurface2Light,
            border = CrimsonBorderLight,
            text = CrimsonTextLight,
            textMuted = CrimsonTextMutedLight,
            success = StatusSuccess,
            successSoft = StatusSuccessSoft,
            warning = StatusWarning,
            warningSoft = StatusWarningSoft,
            danger = StatusDanger,
            dangerSoft = StatusDangerSoft
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = poruaTokens.bg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalPoruaColors provides poruaTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PoruaTypography,
            shapes = PoruaShapes,
            content = content
        )
    }
}
