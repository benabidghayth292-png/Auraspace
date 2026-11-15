package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.DarkModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.auraDataStore: DataStore<Preferences> by preferencesDataStore(name = "auraspace_prefs")

data class ActiveWallpaperPreview(
  val id: String = "wall_nature_aurora",
  val title: String = "Aurora Alpine",
  val drawableResName: String? = "img_wall_nature_1794736920106",
  val gradientColorsCsv: String? = null,
  val gradientStyleName: String = "LINEAR_DIAGONAL",
  val userUriString: String? = null,
)

data class AuraUserPreferences(
  val activePaletteId: String = "material_you",
  val useDynamicColor: Boolean = true,
  val darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
  val activeWallpaper: ActiveWallpaperPreview = ActiveWallpaperPreview(),
  val selectedRingtoneName: String = "Default System Ringtone",
  val selectedRingtoneUri: String = "",
  val selectedNotificationName: String = "Default Notification Tone",
  val selectedNotificationUri: String = "",
  val activeSoundProfileId: String = "balanced",
)

class UserPreferencesRepository(private val context: Context) {
  private object Keys {
    val ACTIVE_PALETTE_ID = stringPreferencesKey("active_palette_id")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val DARK_MODE = stringPreferencesKey("dark_mode")
    val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
    val WALLPAPER_TITLE = stringPreferencesKey("wallpaper_title")
    val WALLPAPER_DRAWABLE = stringPreferencesKey("wallpaper_drawable")
    val WALLPAPER_GRADIENT = stringPreferencesKey("wallpaper_gradient")
    val WALLPAPER_STYLE = stringPreferencesKey("wallpaper_style")
    val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
    val RINGTONE_NAME = stringPreferencesKey("ringtone_name")
    val RINGTONE_URI = stringPreferencesKey("ringtone_uri")
    val NOTIF_NAME = stringPreferencesKey("notif_name")
    val NOTIF_URI = stringPreferencesKey("notif_uri")
    val SOUND_PROFILE_ID = stringPreferencesKey("sound_profile_id")
  }

  val userPreferences: Flow<AuraUserPreferences> =
    context.auraDataStore.data.map { prefs ->
      val darkModeStr = prefs[Keys.DARK_MODE] ?: DarkModePreference.SYSTEM.name
      val darkMode =
        runCatching { DarkModePreference.valueOf(darkModeStr) }
          .getOrDefault(DarkModePreference.SYSTEM)
      AuraUserPreferences(
        activePaletteId = prefs[Keys.ACTIVE_PALETTE_ID] ?: "material_you",
        useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: true,
        darkModePreference = darkMode,
        activeWallpaper =
          ActiveWallpaperPreview(
            id = prefs[Keys.WALLPAPER_ID] ?: "wall_nature_aurora",
            title = prefs[Keys.WALLPAPER_TITLE] ?: "Aurora Alpine",
            drawableResName =
              if (prefs.contains(Keys.WALLPAPER_ID)) prefs[Keys.WALLPAPER_DRAWABLE]
              else "img_wall_nature_1794736920106",
            gradientColorsCsv = prefs[Keys.WALLPAPER_GRADIENT],
            gradientStyleName = prefs[Keys.WALLPAPER_STYLE] ?: "LINEAR_DIAGONAL",
            userUriString = prefs[Keys.WALLPAPER_URI],
          ),
        selectedRingtoneName = prefs[Keys.RINGTONE_NAME] ?: "Default System Ringtone",
        selectedRingtoneUri = prefs[Keys.RINGTONE_URI] ?: "",
        selectedNotificationName = prefs[Keys.NOTIF_NAME] ?: "Default Notification Tone",
        selectedNotificationUri = prefs[Keys.NOTIF_URI] ?: "",
        activeSoundProfileId = prefs[Keys.SOUND_PROFILE_ID] ?: "balanced",
      )
    }

  suspend fun setActivePalette(paletteId: String, useDynamicColor: Boolean) {
    context.auraDataStore.edit { prefs ->
      prefs[Keys.ACTIVE_PALETTE_ID] = paletteId
      prefs[Keys.USE_DYNAMIC_COLOR] = useDynamicColor
    }
  }

  suspend fun setDarkMode(mode: DarkModePreference) {
    context.auraDataStore.edit { prefs -> prefs[Keys.DARK_MODE] = mode.name }
  }

  suspend fun setActiveWallpaper(preview: ActiveWallpaperPreview) {
    context.auraDataStore.edit { prefs ->
      prefs[Keys.WALLPAPER_ID] = preview.id
      prefs[Keys.WALLPAPER_TITLE] = preview.title
      if (preview.drawableResName != null) {
        prefs[Keys.WALLPAPER_DRAWABLE] = preview.drawableResName
      } else {
        prefs.remove(Keys.WALLPAPER_DRAWABLE)
      }
      if (preview.gradientColorsCsv != null) {
        prefs[Keys.WALLPAPER_GRADIENT] = preview.gradientColorsCsv
      } else {
        prefs.remove(Keys.WALLPAPER_GRADIENT)
      }
      prefs[Keys.WALLPAPER_STYLE] = preview.gradientStyleName
      if (preview.userUriString != null) {
        prefs[Keys.WALLPAPER_URI] = preview.userUriString
      } else {
        prefs.remove(Keys.WALLPAPER_URI)
      }
    }
  }

  suspend fun setRingtone(name: String, uri: String) {
    context.auraDataStore.edit { prefs ->
      prefs[Keys.RINGTONE_NAME] = name
      prefs[Keys.RINGTONE_URI] = uri
    }
  }

  suspend fun setNotificationSound(name: String, uri: String) {
    context.auraDataStore.edit { prefs ->
      prefs[Keys.NOTIF_NAME] = name
      prefs[Keys.NOTIF_URI] = uri
    }
  }

  suspend fun setSoundProfile(profileId: String) {
    context.auraDataStore.edit { prefs -> prefs[Keys.SOUND_PROFILE_ID] = profileId }
  }
}
