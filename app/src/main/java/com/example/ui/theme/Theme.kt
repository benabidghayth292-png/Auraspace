package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.CustomPaletteEntity
import com.example.data.model.DarkModePreference

@Composable
fun AuraSpaceTheme(
  darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
  activePalette: CustomPaletteEntity? = null,
  useDynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val isDarkTheme =
    when (darkModePreference) {
      DarkModePreference.SYSTEM -> isSystemInDarkTheme()
      DarkModePreference.LIGHT -> false
      DarkModePreference.DARK -> true
    }

  val context = LocalContext.current
  val colorScheme: ColorScheme =
    when {
      (useDynamicColor || activePalette?.isDynamicSystem == true) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      activePalette != null && !activePalette.isDynamicSystem -> {
        buildPaletteColorScheme(activePalette, isDarkTheme)
      }
      isDarkTheme -> {
        darkColorScheme(
          primary = AuraIndigo,
          onPrimary = Color.White,
          primaryContainer = Color(0xFF282E5C),
          onPrimaryContainer = Color(0xFFE0E4FF),
          secondary = AuraCyan,
          onSecondary = Color(0xFF061E26),
          tertiary = AuraCoral,
          background = AuraObsidian,
          onBackground = Color(0xFFF1F5F9),
          surface = AuraSurfaceDark,
          onSurface = Color(0xFFF1F5F9),
          surfaceVariant = AuraSurfaceVariantDark,
          onSurfaceVariant = Color(0xFFB4BCD0),
        )
      }
      else -> {
        lightColorScheme(
          primary = AuraIndigo,
          onPrimary = Color.White,
          primaryContainer = Color(0xFFE6E8FF),
          onPrimaryContainer = Color(0xFF191D4D),
          secondary = Color(0xFF0E7490),
          onSecondary = Color.White,
          tertiary = Color(0xFFC25E3B),
          background = AuraSlateLight,
          onBackground = Color(0xFF0F172A),
          surface = Color.White,
          onSurface = Color(0xFF0F172A),
          surfaceVariant = Color(0xFFEAEFF7),
          onSurfaceVariant = Color(0xFF475569),
        )
      }
    }

  MaterialTheme(colorScheme = colorScheme, typography = AuraTypography, content = content)
}

private fun buildPaletteColorScheme(
  palette: CustomPaletteEntity,
  isDarkTheme: Boolean,
): ColorScheme {
  val primary = parseHexColor(palette.primaryHex, AuraIndigo)
  val secondary = parseHexColor(palette.secondaryHex, AuraCyan)
  val tertiary = parseHexColor(palette.tertiaryHex, AuraCoral)
  val customSurface = parseHexColor(palette.surfaceHex, AuraSlateLight)

  return if (isDarkTheme) {
    val bgBase =
      if (customSurface.luminance() < 0.25f) customSurface else blendColors(Color(0xFF0D0F18), primary, 0.08f)
    val surface = blendColors(bgBase, Color.White, 0.05f)
    val surfaceHigh = blendColors(bgBase, Color.White, 0.11f)
    val primaryContainer = blendColors(bgBase, primary, 0.34f)
    val secondaryContainer = blendColors(bgBase, secondary, 0.28f)

    darkColorScheme(
      primary = primary,
      onPrimary = contrastOnColor(primary),
      primaryContainer = primaryContainer,
      onPrimaryContainer = contrastOnColor(primaryContainer),
      secondary = secondary,
      onSecondary = contrastOnColor(secondary),
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = contrastOnColor(secondaryContainer),
      tertiary = tertiary,
      onTertiary = contrastOnColor(tertiary),
      background = bgBase,
      onBackground = Color(0xFFF3F4F6),
      surface = surface,
      onSurface = Color(0xFFF3F4F6),
      surfaceVariant = surfaceHigh,
      onSurfaceVariant = Color(0xFFCBD5E1),
      surfaceContainerLow = blendColors(bgBase, Color.White, 0.03f),
      surfaceContainer = surface,
      surfaceContainerHigh = surfaceHigh,
      outline = Color.White.copy(alpha = 0.18f),
    )
  } else {
    val bgBase =
      if (customSurface.luminance() > 0.72f) customSurface else blendColors(Color(0xFFF8FAFC), primary, 0.05f)
    val surface = Color.White
    val surfaceVariant = blendColors(bgBase, primary, 0.09f)
    val primaryContainer = blendColors(Color.White, primary, 0.18f)
    val secondaryContainer = blendColors(Color.White, secondary, 0.18f)

    lightColorScheme(
      primary = primary,
      onPrimary = contrastOnColor(primary),
      primaryContainer = primaryContainer,
      onPrimaryContainer = Color(0xFF111827),
      secondary = secondary,
      onSecondary = contrastOnColor(secondary),
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = Color(0xFF111827),
      tertiary = tertiary,
      onTertiary = contrastOnColor(tertiary),
      background = bgBase,
      onBackground = Color(0xFF0F172A),
      surface = surface,
      onSurface = Color(0xFF0F172A),
      surfaceVariant = surfaceVariant,
      onSurfaceVariant = Color(0xFF475569),
      surfaceContainerLow = bgBase,
      surfaceContainer = blendColors(bgBase, Color.White, 0.55f),
      surfaceContainerHigh = surfaceVariant,
      outline = Color(0xFF0F172A).copy(alpha = 0.14f),
    )
  }
}
