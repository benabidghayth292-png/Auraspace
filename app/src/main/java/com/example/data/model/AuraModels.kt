package com.example.data.model

import androidx.annotation.DrawableRes

enum class WallpaperCategory(val label: String) {
  ALL("All"),
  MINIMAL("Minimal"),
  AMOLED_DARK("AMOLED Dark"),
  NATURE("Nature"),
  ABSTRACT("Abstract"),
  GRADIENTS("Gradients"),
  FAVORITES("Favorites"),
}

enum class GradientStyle(val label: String) {
  LINEAR_VERTICAL("Vertical"),
  LINEAR_DIAGONAL("Diagonal"),
  RADIAL_GLOW("Radial Glow"),
  MESH_AURA("Aura Mesh"),
}

enum class WallpaperTarget(val label: String) {
  HOME("Home Screen"),
  LOCK("Lock Screen"),
  BOTH("Home & Lock"),
}

data class WallpaperItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: WallpaperCategory,
  @DrawableRes val drawableResId: Int? = null,
  val gradientColorsHex: List<String> = emptyList(),
  val gradientStyle: GradientStyle = GradientStyle.LINEAR_DIAGONAL,
  val userUriString: String? = null,
  val isCustomSaved: Boolean = false,
  val isFavorite: Boolean = false,
)

enum class DarkModePreference(val label: String) {
  SYSTEM("System"),
  LIGHT("Light"),
  DARK("Dark"),
}

enum class WidgetTemplate(val id: String, val title: String, val subtitle: String) {
  CLOCK_DATE(
    id = "clock",
    title = "Minimalist Clock & Date",
    subtitle = "Clean typographic timepiece with live date badge",
  ),
  BATTERY_STORAGE(
    id = "battery_storage",
    title = "Battery & Storage Meter",
    subtitle = "Real-time power & internal storage telemetry",
  ),
  DAILY_QUOTE(
    id = "quote",
    title = "Daily Motivational Quote",
    subtitle = "Curated or custom editorial quote card",
  ),
}

enum class WidgetFontStyle(val id: String, val label: String) {
  MODERN("MODERN", "Modern Sans"),
  GEOMETRIC("GEOMETRIC", "Geometric"),
  EDITORIAL("EDITORIAL", "Editorial Serif"),
  MONO("MONO", "Tech Mono"),
}

enum class IconShapeOption(val id: String, val label: String) {
  SQUIRCLE("SQUIRCLE", "Squircle"),
  CIRCLE("CIRCLE", "Circle"),
  ROUNDED_SQUARE("ROUNDED_SQUARE", "Rounded"),
  HEXAGON("HEXAGON", "Hexagon"),
}

data class InstalledAppInfo(
  val packageName: String,
  val activityName: String,
  val label: String,
)

data class DeviceTelemetry(
  val batteryPercent: Int,
  val isCharging: Boolean,
  val storageUsedPercent: Int,
  val storageFreeGb: String,
  val storageTotalGb: String,
)
