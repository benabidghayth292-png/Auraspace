package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

val AuraIndigo = Color(0xFF5E6AD2)
val AuraCyan = Color(0xFF48BFE3)
val AuraCoral = Color(0xFFF4845F)
val AuraSlateLight = Color(0xFFF8FAFC)
val AuraObsidian = Color(0xFF0E101D)
val AuraSurfaceDark = Color(0xFF171A2B)
val AuraSurfaceVariantDark = Color(0xFF232740)

fun parseHexColor(hex: String, fallback: Color = AuraIndigo): Color {
  return runCatching { Color(android.graphics.Color.parseColor(hex.trim())) }.getOrDefault(fallback)
}

fun Color.toHexString(): String {
  val argb = toArgb()
  return String.format("#%06X", 0xFFFFFF and argb)
}

fun contrastOnColor(background: Color): Color {
  return if (background.luminance() > 0.45f) Color(0xFF11131C) else Color(0xFFFFFFFF)
}

fun blendColors(a: Color, b: Color, fraction: Float): Color {
  val f = fraction.coerceIn(0f, 1f)
  return Color(
    red = a.red + (b.red - a.red) * f,
    green = a.green + (b.green - a.green) * f,
    blue = a.blue + (b.blue - a.blue) * f,
    alpha = 1f,
  )
}
