package com.example.data.repository

import android.content.Context
import com.example.data.local.AuraDao
import com.example.data.local.AuraDatabase
import com.example.data.local.CustomPaletteEntity
import com.example.data.local.FavoriteWallpaperEntity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.AuraDefaults
import com.example.data.model.DarkModePreference
import com.example.data.model.GradientStyle
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.preferences.ActiveWallpaperPreview
import com.example.data.preferences.AuraUserPreferences
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AuraRepository(private val context: Context) {
  private val dao: AuraDao = AuraDatabase.getInstance(context).auraDao()
  private val prefsRepo = UserPreferencesRepository(context)

  val userPreferences: Flow<AuraUserPreferences> = prefsRepo.userPreferences
  val palettes: Flow<List<CustomPaletteEntity>> = dao.observePalettes()
  val widgetConfigs: Flow<List<WidgetConfigEntity>> = dao.observeWidgetConfigs()

  val wallpapers: Flow<List<WallpaperItem>> =
    dao.observeSavedWallpapers().combine(prefsRepo.userPreferences) { savedEntities, _ ->
      val savedById = savedEntities.associateBy { it.id }
      val curatedWithFavs =
        AuraDefaults.curatedWallpapers.map { item ->
          val saved = savedById[item.id]
          item.copy(isFavorite = saved?.isFavorite == true)
        }
      val customEntries =
        savedEntities
          .filter { entity -> AuraDefaults.curatedWallpapers.none { it.id == entity.id } }
          .map { entity ->
            val category =
              runCatching { WallpaperCategory.valueOf(entity.categoryName) }
                .getOrDefault(WallpaperCategory.GRADIENTS)
            val style =
              runCatching { GradientStyle.valueOf(entity.gradientStyleName) }
                .getOrDefault(GradientStyle.LINEAR_DIAGONAL)
            WallpaperItem(
              id = entity.id,
              title = entity.title,
              subtitle = entity.subtitle,
              category = category,
              drawableResId = resolveDrawableId(entity.drawableResName),
              gradientColorsHex =
                entity.gradientColorsCsv?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
              gradientStyle = style,
              userUriString = entity.userUriString,
              isCustomSaved = true,
              isFavorite = entity.isFavorite,
            )
          }
      customEntries + curatedWithFavs
    }

  suspend fun ensureSeeded() {
    dao.insertPalettesIfAbsent(AuraDefaults.defaultPalettes)
    dao.insertWidgetConfigsIfAbsent(AuraDefaults.defaultWidgetConfigs)
  }

  suspend fun toggleFavorite(item: WallpaperItem) {
    val existing = dao.getSavedWallpaperById(item.id)
    if (existing != null) {
      if (item.isCustomSaved) {
        dao.upsertSavedWallpaper(existing.copy(isFavorite = !existing.isFavorite))
      } else {
        dao.deleteSavedWallpaperById(item.id)
      }
    } else {
      val drawableName =
        item.drawableResId?.let { resId ->
          runCatching { context.resources.getResourceEntryName(resId) }.getOrNull()
        }
      dao.upsertSavedWallpaper(
        FavoriteWallpaperEntity(
          id = item.id,
          title = item.title,
          subtitle = item.subtitle,
          categoryName = item.category.name,
          isGeneratedGradient = item.gradientColorsHex.isNotEmpty(),
          drawableResName = drawableName,
          gradientColorsCsv =
            if (item.gradientColorsHex.isEmpty()) null
            else item.gradientColorsHex.joinToString(","),
          gradientStyleName = item.gradientStyle.name,
          userUriString = item.userUriString,
          isFavorite = true,
        )
      )
    }
  }

  suspend fun saveCustomWallpaper(item: WallpaperItem) {
    val drawableName =
      item.drawableResId?.let { resId ->
        runCatching { context.resources.getResourceEntryName(resId) }.getOrNull()
      }
    dao.upsertSavedWallpaper(
      FavoriteWallpaperEntity(
        id = item.id,
        title = item.title,
        subtitle = item.subtitle,
        categoryName = item.category.name,
        isGeneratedGradient = item.gradientColorsHex.isNotEmpty(),
        drawableResName = drawableName,
        gradientColorsCsv =
          if (item.gradientColorsHex.isEmpty()) null else item.gradientColorsHex.joinToString(","),
        gradientStyleName = item.gradientStyle.name,
        userUriString = item.userUriString,
        isFavorite = item.isFavorite,
      )
    )
  }

  suspend fun deleteCustomWallpaper(id: String) {
    dao.deleteSavedWallpaperById(id)
  }

  suspend fun setActiveWallpaper(item: WallpaperItem) {
    val drawableName =
      item.drawableResId?.let { resId ->
        runCatching { context.resources.getResourceEntryName(resId) }.getOrNull()
      }
    prefsRepo.setActiveWallpaper(
      ActiveWallpaperPreview(
        id = item.id,
        title = item.title,
        drawableResName = drawableName,
        gradientColorsCsv =
          if (item.gradientColorsHex.isEmpty()) null else item.gradientColorsHex.joinToString(","),
        gradientStyleName = item.gradientStyle.name,
        userUriString = item.userUriString,
      )
    )
  }

  suspend fun setActivePalette(palette: CustomPaletteEntity) {
    prefsRepo.setActivePalette(palette.id, palette.isDynamicSystem)
  }

  suspend fun saveCustomPalette(palette: CustomPaletteEntity) {
    dao.upsertPalette(palette)
  }

  suspend fun deleteCustomPalette(id: String) {
    dao.deleteCustomPalette(id)
  }

  suspend fun setDarkMode(mode: DarkModePreference) {
    prefsRepo.setDarkMode(mode)
  }

  suspend fun saveWidgetConfig(config: WidgetConfigEntity) {
    dao.upsertWidgetConfig(config.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun getWidgetConfigOnce(templateId: String): WidgetConfigEntity {
    ensureSeeded()
    return dao.getWidgetConfig(templateId)
      ?: AuraDefaults.defaultWidgetConfigs.first { it.templateId == templateId }
  }

  suspend fun setRingtone(name: String, uri: String) {
    prefsRepo.setRingtone(name, uri)
  }

  suspend fun setNotificationSound(name: String, uri: String) {
    prefsRepo.setNotificationSound(name, uri)
  }

  suspend fun setSoundProfile(profileId: String) {
    prefsRepo.setSoundProfile(profileId)
  }

  fun resolveDrawableId(name: String?): Int? {
    if (name.isNullOrBlank()) return null
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (id != 0) id else null
  }
}
