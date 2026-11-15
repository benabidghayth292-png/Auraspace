package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomPaletteEntity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.AuraDefaults
import com.example.data.model.DarkModePreference
import com.example.data.model.DeviceTelemetry
import com.example.data.model.GradientStyle
import com.example.data.model.IconShapeOption
import com.example.data.model.InstalledAppInfo
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperTarget
import com.example.data.model.WidgetTemplate
import com.example.data.preferences.AuraUserPreferences
import com.example.data.repository.AuraRepository
import com.example.system.AudioVolumeSnapshot
import com.example.system.ShortcutPinManager
import com.example.system.SystemSettingsHelper
import com.example.system.WallpaperApplier
import com.example.widgets.WidgetPinManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {
  private val appContext = application.applicationContext
  private val repository = AuraRepository(appContext)

  val userPreferences: StateFlow<AuraUserPreferences> =
    repository.userPreferences.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AuraUserPreferences(),
    )

  val palettes: StateFlow<List<CustomPaletteEntity>> =
    repository.palettes.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AuraDefaults.defaultPalettes,
    )

  val activePalette: StateFlow<CustomPaletteEntity?> =
    combine(palettes, userPreferences) { list, prefs ->
        list.find { it.id == prefs.activePaletteId } ?: list.firstOrNull()
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuraDefaults.defaultPalettes.first(),
      )

  val allWallpapers: StateFlow<List<WallpaperItem>> =
    repository.wallpapers.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AuraDefaults.curatedWallpapers,
    )

  private val _selectedCategory = MutableStateFlow(WallpaperCategory.ALL)
  val selectedCategory: StateFlow<WallpaperCategory> = _selectedCategory.asStateFlow()

  val filteredWallpapers: StateFlow<List<WallpaperItem>> =
    combine(allWallpapers, _selectedCategory) { list, category ->
        when (category) {
          WallpaperCategory.ALL -> list
          WallpaperCategory.FAVORITES -> list.filter { it.isFavorite }
          else -> list.filter { it.category == category }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuraDefaults.curatedWallpapers,
      )

  val widgetConfigs: StateFlow<List<WidgetConfigEntity>> =
    repository.widgetConfigs.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AuraDefaults.defaultWidgetConfigs,
    )

  private val _deviceTelemetry = MutableStateFlow(SystemSettingsHelper.getDeviceTelemetry(appContext))
  val deviceTelemetry: StateFlow<DeviceTelemetry> = _deviceTelemetry.asStateFlow()

  private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
  val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

  private val _audioVolumes = MutableStateFlow(SystemSettingsHelper.getAudioVolumes(appContext))
  val audioVolumes: StateFlow<AudioVolumeSnapshot> = _audioVolumes.asStateFlow()

  private val _canWriteSettings =
    MutableStateFlow(SystemSettingsHelper.canWriteSystemSettings(appContext))
  val canWriteSettings: StateFlow<Boolean> = _canWriteSettings.asStateFlow()

  private val _screenTimeoutMs =
    MutableStateFlow(SystemSettingsHelper.getScreenTimeoutMs(appContext))
  val screenTimeoutMs: StateFlow<Int> = _screenTimeoutMs.asStateFlow()

  private val _fontScale = MutableStateFlow(SystemSettingsHelper.getFontScale(appContext))
  val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

  private val _isApplyingWallpaper = MutableStateFlow(false)
  val isApplyingWallpaper: StateFlow<Boolean> = _isApplyingWallpaper.asStateFlow()

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages: SharedFlow<String> = _messages.asSharedFlow()

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
      _installedApps.value = ShortcutPinManager.loadInstalledApps(appContext)
    }
  }

  fun refreshSystemState() {
    _deviceTelemetry.value = SystemSettingsHelper.getDeviceTelemetry(appContext)
    _audioVolumes.value = SystemSettingsHelper.getAudioVolumes(appContext)
    _canWriteSettings.value = SystemSettingsHelper.canWriteSystemSettings(appContext)
    _screenTimeoutMs.value = SystemSettingsHelper.getScreenTimeoutMs(appContext)
    _fontScale.value = SystemSettingsHelper.getFontScale(appContext)
  }

  fun selectWallpaperCategory(category: WallpaperCategory) {
    _selectedCategory.value = category
  }

  fun toggleFavorite(item: WallpaperItem) {
    viewModelScope.launch {
      repository.toggleFavorite(item)
      val msg =
        if (item.isFavorite) "Removed \"${item.title}\" from Favorites"
        else "Saved \"${item.title}\" to Favorites"
      _messages.tryEmit(msg)
    }
  }

  fun saveGeneratedGradientWallpaper(
    title: String,
    colorsHex: List<String>,
    style: GradientStyle,
  ): WallpaperItem {
    val item =
      WallpaperItem(
        id = "grad_${System.currentTimeMillis()}",
        title = title.ifBlank { "Custom Aura Gradient" },
        subtitle = "${colorsHex.size}-stop ${style.label} gradient",
        category = WallpaperCategory.GRADIENTS,
        gradientColorsHex = colorsHex,
        gradientStyle = style,
        isCustomSaved = true,
        isFavorite = true,
      )
    viewModelScope.launch {
      repository.saveCustomWallpaper(item)
      _messages.tryEmit("Saved \"${item.title}\" to Gradients & Favorites")
    }
    return item
  }

  fun importPickedPhotoWallpaper(uri: Uri): WallpaperItem {
    val item =
      WallpaperItem(
        id = "photo_${System.currentTimeMillis()}",
        title = "Custom Gallery Photo",
        subtitle = "Imported via Photo Picker",
        category = WallpaperCategory.MINIMAL,
        userUriString = uri.toString(),
        isCustomSaved = true,
        isFavorite = true,
      )
    viewModelScope.launch {
      repository.saveCustomWallpaper(item)
      _messages.tryEmit("Photo imported to your library")
    }
    return item
  }

  fun applyWallpaper(item: WallpaperItem, target: WallpaperTarget) {
    viewModelScope.launch {
      _isApplyingWallpaper.value = true
      val result = WallpaperApplier.applyWallpaper(appContext, item, target)
      _isApplyingWallpaper.value = false
      result
        .onSuccess {
          repository.setActiveWallpaper(item)
          _messages.tryEmit("Applied \"${item.title}\" to ${target.label}")
        }
        .onFailure { err ->
          _messages.tryEmit("Wallpaper failed: ${err.localizedMessage ?: "Unknown error"}")
        }
    }
  }

  fun setActiveWallpaperPreviewOnly(item: WallpaperItem) {
    viewModelScope.launch {
      repository.setActiveWallpaper(item)
      _messages.tryEmit("Updated Home Preview with \"${item.title}\"")
    }
  }

  fun selectPalette(palette: CustomPaletteEntity) {
    viewModelScope.launch {
      repository.setActivePalette(palette)
      _messages.tryEmit("Activated \"${palette.name}\" theme preset")
    }
  }

  fun createCustomPalette(
    name: String,
    primaryHex: String,
    secondaryHex: String,
    tertiaryHex: String,
    surfaceHex: String,
  ) {
    viewModelScope.launch {
      val entity =
        CustomPaletteEntity(
          id = "custom_${System.currentTimeMillis()}",
          name = name.ifBlank { "Custom Aura" },
          subtitle = "User crafted palette",
          primaryHex = primaryHex,
          secondaryHex = secondaryHex,
          tertiaryHex = tertiaryHex,
          surfaceHex = surfaceHex,
          isBuiltIn = false,
          isDynamicSystem = false,
        )
      repository.saveCustomPalette(entity)
      repository.setActivePalette(entity)
      _messages.tryEmit("Saved & applied \"${entity.name}\"")
    }
  }

  fun deleteCustomPalette(id: String) {
    viewModelScope.launch {
      if (userPreferences.value.activePaletteId == id) {
        AuraDefaults.defaultPalettes.firstOrNull()?.let { repository.setActivePalette(it) }
      }
      repository.deleteCustomPalette(id)
      _messages.tryEmit("Custom palette deleted")
    }
  }

  fun setDarkMode(mode: DarkModePreference) {
    viewModelScope.launch {
      repository.setDarkMode(mode)
      _messages.tryEmit("Theme mode set to ${mode.label}")
    }
  }

  fun saveWidgetConfig(config: WidgetConfigEntity) {
    viewModelScope.launch {
      repository.saveWidgetConfig(config)
      WidgetPinManager.refreshAllWidgets(appContext)
      _messages.tryEmit("Widget configuration saved & synced")
    }
  }

  fun pinWidget(template: WidgetTemplate, config: WidgetConfigEntity) {
    viewModelScope.launch {
      repository.saveWidgetConfig(config)
      val res = WidgetPinManager.requestPinWidget(appContext, template)
      res
        .onSuccess { msg -> _messages.tryEmit(msg) }
        .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Pinning unavailable") }
    }
  }

  fun pinCustomAppShortcut(
    app: InstalledAppInfo,
    customLabel: String,
    shape: IconShapeOption,
    bgTintHex: String,
    fgTintHex: String,
    monochromeForeground: Boolean,
  ) {
    val result =
      ShortcutPinManager.pinCustomShortcut(
        context = appContext,
        app = app,
        customLabel = customLabel,
        shape = shape,
        bgTintHex = bgTintHex,
        fgTintHex = fgTintHex,
        monochromeForeground = monochromeForeground,
      )
    result
      .onSuccess { msg -> _messages.tryEmit(msg) }
      .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Could not pin shortcut") }
  }

  fun setStreamVolume(streamType: Int, level: Int) {
    val ok = SystemSettingsHelper.setStreamVolume(appContext, streamType, level)
    _audioVolumes.value = SystemSettingsHelper.getAudioVolumes(appContext)
    if (!ok) {
      _messages.tryEmit("System Do Not Disturb restricts changing ringer volume.")
    }
  }

  fun applySoundProfile(profileId: String, label: String) {
    viewModelScope.launch {
      val ok = SystemSettingsHelper.applySoundProfile(appContext, profileId)
      _audioVolumes.value = SystemSettingsHelper.getAudioVolumes(appContext)
      repository.setSoundProfile(profileId)
      _messages.tryEmit(
        if (ok) "Applied $label sound profile"
        else "Saved $label profile (some streams restricted by DND)"
      )
    }
  }

  fun updateRingtone(uri: Uri?) {
    viewModelScope.launch {
      val title = SystemSettingsHelper.resolveRingtoneTitle(appContext, uri)
      repository.setRingtone(title, uri?.toString().orEmpty())
      _messages.tryEmit("Ringtone set to $title")
    }
  }

  fun updateNotificationSound(uri: Uri?) {
    viewModelScope.launch {
      val title = SystemSettingsHelper.resolveRingtoneTitle(appContext, uri)
      repository.setNotificationSound(title, uri?.toString().orEmpty())
      _messages.tryEmit("Notification sound set to $title")
    }
  }

  fun updateScreenTimeout(timeoutMs: Int) {
    if (SystemSettingsHelper.setScreenTimeoutMs(appContext, timeoutMs)) {
      _screenTimeoutMs.value = timeoutMs
      _messages.tryEmit("Screen timeout updated")
    } else {
      _messages.tryEmit("Grant Modify System Settings permission first")
    }
  }

  fun updateFontScale(scale: Float) {
    if (SystemSettingsHelper.setFontScale(appContext, scale)) {
      _fontScale.value = scale
      _messages.tryEmit("System font scale updated to ${scale}x")
    } else {
      _messages.tryEmit("Grant Modify System Settings permission first")
    }
  }

  fun emitMessage(message: String) {
    _messages.tryEmit(message)
  }

  fun resolveDrawableId(name: String?): Int? = repository.resolveDrawableId(name)
}
