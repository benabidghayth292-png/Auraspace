package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_wallpapers")
data class FavoriteWallpaperEntity(
  @PrimaryKey val id: String,
  val title: String,
  val subtitle: String,
  val categoryName: String,
  val isGeneratedGradient: Boolean,
  val drawableResName: String?,
  val gradientColorsCsv: String?,
  val gradientStyleName: String,
  val userUriString: String?,
  val isFavorite: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "custom_palettes")
data class CustomPaletteEntity(
  @PrimaryKey val id: String,
  val name: String,
  val subtitle: String,
  val primaryHex: String,
  val secondaryHex: String,
  val tertiaryHex: String,
  val surfaceHex: String,
  val isBuiltIn: Boolean = false,
  val isDynamicSystem: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
  @PrimaryKey val templateId: String,
  val transparency: Float,
  val cornerRadiusDp: Int,
  val fontStyle: String,
  val accentHex: String,
  val customQuoteText: String,
  val customQuoteAuthor: String,
  val updatedAt: Long = System.currentTimeMillis(),
)
