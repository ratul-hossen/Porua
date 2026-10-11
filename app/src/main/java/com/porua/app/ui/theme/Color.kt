package com.porua.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Porua Design System - Color Tokens
// Extracted exactly from Figma Design System
// ==========================================

// Crimson Light
val CrimsonPrimaryLight = Color(0xFFE11D2E)
val CrimsonPrimaryStrongLight = Color(0xFFB4141F)
val CrimsonPrimarySoftLight = Color(0xFFFDECEE)
val CrimsonOnPrimaryLight = Color(0xFFFFFFFF)
val CrimsonHeroStartLight = Color(0xFF0A0A0A)
val CrimsonHeroEndLight = Color(0xFFE11D2E)
val CrimsonAiLight = Color(0xFF0A0A0A)
val CrimsonAiSoftLight = Color(0xFFF4F4F5)
val CrimsonBgLight = Color(0xFFFAFAFA)
val CrimsonSurfaceLight = Color(0xFFFFFFFF)
val CrimsonSurface2Light = Color(0xFFF4F4F5)
val CrimsonBorderLight = Color(0xFFE4E4E7)
val CrimsonTextLight = Color(0xFF0A0A0A)
val CrimsonTextMutedLight = Color(0xFF63636B)

// Crimson Dark
val CrimsonPrimaryDark = Color(0xFFFF4D5A)
val CrimsonPrimaryStrongDark = Color(0xFFFF7A84)
val CrimsonPrimarySoftDark = Color(0xFF3A0D12)
val CrimsonOnPrimaryDark = Color(0xFF0A0A0A)
val CrimsonHeroStartDark = Color(0xFF1F1F1F)
val CrimsonHeroEndDark = Color(0xFFB4141F)
val CrimsonAiDark = Color(0xFFFAFAFA)
val CrimsonAiSoftDark = Color(0xFF262626)
val CrimsonBgDark = Color(0xFF0A0A0A)
val CrimsonSurfaceDark = Color(0xFF161616)
val CrimsonSurface2Dark = Color(0xFF232323)
val CrimsonBorderDark = Color(0xFF2E2E2E)
val CrimsonTextDark = Color(0xFFFAFAFA)
val CrimsonTextMutedDark = Color(0xFFA1A1AA)

// Status Colors - Common across themes
val StatusSuccess = Color(0xFF16803D)
val StatusSuccessSoft = Color(0xFFDCFCE7)
val StatusSuccessDark = Color(0xFF4ADE80)
val StatusSuccessSoftDark = Color(0xFF0F2A1A)

val StatusWarning = Color(0xFFB45309)
val StatusWarningSoft = Color(0xFFFEF3C7)
val StatusWarningDark = Color(0xFFFBBF24)
val StatusWarningSoftDark = Color(0xFF2E2108)

val StatusDanger = Color(0xFFB91C1C)
val StatusDangerSoft = Color(0xFFFEE2E2)
val StatusDangerDark = Color(0xFFF87171)
val StatusDangerSoftDark = Color(0xFF3A1010)

// Helper function to get mastery color based on score
fun getMasteryColor(mastery: Int): Color {
    return when {
        mastery >= 75 -> StatusSuccess
        mastery >= 50 -> StatusWarning
        else -> StatusDanger
    }
}

fun getMasterySoftColor(mastery: Int): Color {
    return when {
        mastery >= 75 -> StatusSuccessSoft
        mastery >= 50 -> StatusWarningSoft
        else -> StatusDangerSoft
    }
}
