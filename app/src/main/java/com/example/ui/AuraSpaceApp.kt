package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AuraRoute
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.SoundSettingsScreen
import com.example.ui.screens.ThemeIconStudioScreen
import com.example.ui.screens.WallpaperManagerScreen
import com.example.ui.screens.WidgetStudioScreen
import com.example.ui.theme.AuraSpaceTheme
import com.example.ui.viewmodel.AuraViewModel

private data class NavItem(
  val route: AuraRoute,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val AuraNavItems =
  listOf(
    NavItem(
      route = AuraRoute.Home,
      label = "Home",
      selectedIcon = Icons.Filled.Home,
      unselectedIcon = Icons.Outlined.Home,
      testTag = "nav_home",
    ),
    NavItem(
      route = AuraRoute.Wallpapers,
      label = "Walls",
      selectedIcon = Icons.Filled.Wallpaper,
      unselectedIcon = Icons.Outlined.Wallpaper,
      testTag = "nav_wallpapers",
    ),
    NavItem(
      route = AuraRoute.Widgets,
      label = "Widgets",
      selectedIcon = Icons.Filled.Widgets,
      unselectedIcon = Icons.Outlined.Widgets,
      testTag = "nav_widgets",
    ),
    NavItem(
      route = AuraRoute.Themes,
      label = "Themes",
      selectedIcon = Icons.Filled.Palette,
      unselectedIcon = Icons.Outlined.Palette,
      testTag = "nav_themes",
    ),
    NavItem(
      route = AuraRoute.SoundSettings,
      label = "System",
      selectedIcon = Icons.Filled.Tune,
      unselectedIcon = Icons.Outlined.Tune,
      testTag = "nav_system",
    ),
  )

@Composable
fun AuraSpaceApp(viewModel: AuraViewModel = viewModel()) {
  val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
  val palettes by viewModel.palettes.collectAsStateWithLifecycle()
  val activePalette by viewModel.activePalette.collectAsStateWithLifecycle()
  val allWallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
  val filteredWallpapers by viewModel.filteredWallpapers.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val widgetConfigs by viewModel.widgetConfigs.collectAsStateWithLifecycle()
  val telemetry by viewModel.deviceTelemetry.collectAsStateWithLifecycle()
  val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
  val audioVolumes by viewModel.audioVolumes.collectAsStateWithLifecycle()
  val canWriteSettings by viewModel.canWriteSettings.collectAsStateWithLifecycle()
  val screenTimeoutMs by viewModel.screenTimeoutMs.collectAsStateWithLifecycle()
  val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
  val isApplyingWallpaper by viewModel.isApplyingWallpaper.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = backStackEntry?.destination

  LaunchedEffect(Unit) {
    viewModel.messages.collect { message -> snackbarHostState.showSnackbar(message) }
  }

  val navigateToTab: (AuraRoute) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  AuraSpaceTheme(
    darkModePreference = preferences.darkModePreference,
    activePalette = activePalette,
    useDynamicColor = preferences.useDynamicColor,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavigationRail = maxWidth >= 600.dp

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (!useNavigationRail) {
            NavigationBar {
              AuraNavItems.forEach { item ->
                val selected = currentDestination?.hasRoute(item.route::class) == true
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateToTab(item.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  },
                  label = { Text(item.label) },
                  modifier = Modifier.testTag(item.testTag),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (useNavigationRail) {
            NavigationRail {
              AuraNavItems.forEach { item ->
                val selected = currentDestination?.hasRoute(item.route::class) == true
                NavigationRailItem(
                  selected = selected,
                  onClick = { navigateToTab(item.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  },
                  label = { Text(item.label) },
                  modifier = Modifier.testTag(item.testTag),
                )
              }
            }
          }

          NavHost(
            navController = navController,
            startDestination = AuraRoute.Home,
            modifier = Modifier.weight(1f).fillMaxSize(),
          ) {
            composable<AuraRoute.Home> {
              HomeDashboardScreen(
                preferences = preferences,
                activePalette = activePalette,
                wallpapers = allWallpapers,
                widgetConfigs = widgetConfigs,
                telemetry = telemetry,
                resolveDrawableId = viewModel::resolveDrawableId,
                onNavigate = navigateToTab,
                onCycleDarkMode = viewModel::setDarkMode,
                onSelectFavoriteWallpaper = { item ->
                  viewModel.setActiveWallpaperPreviewOnly(item)
                },
              )
            }

            composable<AuraRoute.Wallpapers> {
              WallpaperManagerScreen(
                wallpapers = filteredWallpapers,
                selectedCategory = selectedCategory,
                activeWallpaperId = preferences.activeWallpaper.id,
                isApplying = isApplyingWallpaper,
                onSelectCategory = viewModel::selectWallpaperCategory,
                onToggleFavorite = viewModel::toggleFavorite,
                onSaveGeneratedGradient = viewModel::saveGeneratedGradientWallpaper,
                onImportPhoto = viewModel::importPickedPhotoWallpaper,
                onApplyWallpaper = viewModel::applyWallpaper,
              )
            }

            composable<AuraRoute.Widgets> {
              WidgetStudioScreen(
                widgetConfigs = widgetConfigs,
                activeWallpaper = preferences.activeWallpaper,
                activePalette = activePalette,
                telemetry = telemetry,
                resolveDrawableId = viewModel::resolveDrawableId,
                onSaveConfig = viewModel::saveWidgetConfig,
                onPinWidget = viewModel::pinWidget,
              )
            }

            composable<AuraRoute.Themes> {
              ThemeIconStudioScreen(
                palettes = palettes,
                activePalette = activePalette,
                installedApps = installedApps,
                onSelectPalette = viewModel::selectPalette,
                onCreatePalette = viewModel::createCustomPalette,
                onDeletePalette = viewModel::deleteCustomPalette,
                onPinShortcut = viewModel::pinCustomAppShortcut,
                onNotify = viewModel::emitMessage,
              )
            }

            composable<AuraRoute.SoundSettings> {
              SoundSettingsScreen(
                preferences = preferences,
                audioVolumes = audioVolumes,
                canWriteSettings = canWriteSettings,
                screenTimeoutMs = screenTimeoutMs,
                fontScale = fontScale,
                onRefreshSystemState = viewModel::refreshSystemState,
                onSetDarkMode = viewModel::setDarkMode,
                onUpdateScreenTimeout = viewModel::updateScreenTimeout,
                onUpdateFontScale = viewModel::updateFontScale,
                onSetVolume = viewModel::setStreamVolume,
                onApplySoundProfile = viewModel::applySoundProfile,
                onUpdateRingtone = viewModel::updateRingtone,
                onUpdateNotificationSound = viewModel::updateNotificationSound,
              )
            }
          }
        }
      }
    }
  }
}
