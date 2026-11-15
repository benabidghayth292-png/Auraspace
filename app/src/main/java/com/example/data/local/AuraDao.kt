package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {
  // Wallpapers & Favorites
  @Query("SELECT * FROM favorite_wallpapers ORDER BY createdAt DESC")
  fun observeSavedWallpapers(): Flow<List<FavoriteWallpaperEntity>>

  @Query("SELECT * FROM favorite_wallpapers WHERE id = :id LIMIT 1")
  suspend fun getSavedWallpaperById(id: String): FavoriteWallpaperEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSavedWallpaper(entity: FavoriteWallpaperEntity)

  @Query("DELETE FROM favorite_wallpapers WHERE id = :id")
  suspend fun deleteSavedWallpaperById(id: String)

  // Color Palettes
  @Query("SELECT * FROM custom_palettes ORDER BY isBuiltIn DESC, createdAt ASC")
  fun observePalettes(): Flow<List<CustomPaletteEntity>>

  @Query("SELECT * FROM custom_palettes WHERE id = :id LIMIT 1")
  suspend fun getPaletteById(id: String): CustomPaletteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPalette(palette: CustomPaletteEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPalettesIfAbsent(palettes: List<CustomPaletteEntity>)

  @Query("DELETE FROM custom_palettes WHERE id = :id AND isBuiltIn = 0")
  suspend fun deleteCustomPalette(id: String)

  // Widget Configs
  @Query("SELECT * FROM widget_configs")
  fun observeWidgetConfigs(): Flow<List<WidgetConfigEntity>>

  @Query("SELECT * FROM widget_configs WHERE templateId = :templateId LIMIT 1")
  suspend fun getWidgetConfig(templateId: String): WidgetConfigEntity?

  @Query("SELECT * FROM widget_configs")
  suspend fun getAllWidgetConfigsOnce(): List<WidgetConfigEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWidgetConfig(config: WidgetConfigEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertWidgetConfigsIfAbsent(configs: List<WidgetConfigEntity>)
}
