package de.paarfinanz.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Farben, die Material 3 nicht abdeckt: Partner-Akzente und Budget-Status. */
@Immutable
data class ExtendedColors(
    val partnerA: Color,
    val partnerB: Color,
    val joint: Color,
    val positive: Color,
    val warning: Color,
)

internal val LightExtendedColors = ExtendedColors(
    partnerA = Teal,
    partnerB = Coral,
    joint = Indigo40,
    positive = Positive,
    warning = Warning,
)

internal val DarkExtendedColors = ExtendedColors(
    partnerA = TealLight,
    partnerB = CoralLight,
    joint = Indigo80,
    positive = PositiveDark,
    warning = WarningDark,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
