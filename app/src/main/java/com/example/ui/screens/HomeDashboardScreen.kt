package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CustomPaletteEntity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.DarkModePreference
import com.example.data.model.DeviceTelemetry
import com.example.data.model.WallpaperItem
import com.example.data.model.WidgetTemplate
import com.example.data.preferences.AuraUserPreferences
import com.example.ui.components.PhoneHomeScreenMockup
import com.example.ui.components.WallpaperBackdropFromItem
import com.example.ui.navigation.AuraRoute

@Composable
fun HomeDashboardScreen(
  preferences: AuraUserPreferences,
  activePalette: CustomPaletteEntity?,
  wallpapers: List<WallpaperItem>,
  widgetConfigs: List<WidgetConfigEntity>,
  telemetry: DeviceTelemetry,
  resolveDrawableId: (String?) -> Int?,
  onNavigate: (AuraRoute) -> Unit,
  onCycleDarkMode: (DarkModePreference) -> Unit,
  onSelectFavoriteWallpaper: (WallpaperItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val favoriteWallpapers = wallpapers.filter { it.isFavorite }
  val clockConfig =
    widgetConfigs.firstOrNull { it.templateId == WidgetTemplate.CLOCK_DATE.id }
      ?: WidgetConfigEntity(
        templateId = WidgetTemplate.CLOCK_DATE.id,
        transparency = 0.78f,
        cornerRadiusDp = 24,
        fontStyle = "GEOMETRIC",
        accentHex = "#6366F1",
        customQuoteText = "",
        customQuoteAuthor = "",
      )

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "AURASPACE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Personalization Studio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
        }

        val nextMode =
          when (preferences.darkModePreference) {
            DarkModePreference.SYSTEM -> DarkModePreference.DARK
            DarkModePreference.DARK -> DarkModePreference.LIGHT
            DarkModePreference.LIGHT -> DarkModePreference.SYSTEM
          }
        val modeIcon =
          when (preferences.darkModePreference) {
            DarkModePreference.SYSTEM -> Icons.Default.BrightnessAuto
            DarkModePreference.DARK -> Icons.Default.Brightness4
            DarkModePreference.LIGHT -> Icons.Default.Brightness7
          }

        Surface(
          onClick = { onCycleDarkMode(nextMode) },
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier.testTag("home_dark_mode_pill"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = modeIcon,
              contentDescription = "Cycle theme mode",
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = preferences.darkModePreference.label,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }
    }

    // Live Preview Hero Banner
    item {
      Card(
        shape = RoundedCornerShape(28.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
          ),
        modifier = Modifier.fillMaxWidth().testTag("home_live_preview_banner"),
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .background(
                Brush.linearGradient(
                  listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                  )
                )
              )
              .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            PhoneHomeScreenMockup(
              wallpaperPreview = preferences.activeWallpaper,
              resolveDrawableId = resolveDrawableId,
              widgetConfig = clockConfig,
              telemetry = telemetry,
            )

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              ) {
                Text(
                  text = "LIVE PREVIEW",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
              Text(
                text = "Your Current Aura",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              PreviewSpecRow(
                label = "Wallpaper",
                value = preferences.activeWallpaper.title,
              )
              PreviewSpecRow(
                label = "Palette",
                value = activePalette?.name ?: "Material You",
              )
              PreviewSpecRow(
                label = "Ringtone",
                value = preferences.selectedRingtoneName,
              )
              Spacer(modifier = Modifier.height(2.dp))
              FilledTonalButton(
                onClick = { onNavigate(AuraRoute.Wallpapers) },
                modifier = Modifier.fillMaxWidth().testTag("home_customize_cta"),
              ) {
                Text("Customize")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }
    }

    // Quick-Access Studio Grid
    item {
      Text(
        text = "Studio Modules",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickAccessStudioCard(
            title = "Wallpapers",
            subtitle = "${wallpapers.size} curated & custom",
            icon = Icons.Default.Wallpaper,
            accent = MaterialTheme.colorScheme.primary,
            testTag = "quick_card_wallpapers",
            onClick = { onNavigate(AuraRoute.Wallpapers) },
            modifier = Modifier.weight(1f),
          )
          QuickAccessStudioCard(
            title = "Custom Widgets",
            subtitle = "3 Glance templates",
            icon = Icons.Default.Widgets,
            accent = MaterialTheme.colorScheme.secondary,
            testTag = "quick_card_widgets",
            onClick = { onNavigate(AuraRoute.Widgets) },
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickAccessStudioCard(
            title = "Icon & Theme Presets",
            subtitle = activePalette?.name ?: "Material You",
            icon = Icons.Default.Palette,
            accent = MaterialTheme.colorScheme.tertiary,
            testTag = "quick_card_themes",
            onClick = { onNavigate(AuraRoute.Themes) },
            modifier = Modifier.weight(1f),
          )
          QuickAccessStudioCard(
            title = "Sound Profiles",
            subtitle = preferences.activeSoundProfileId.replace("_", " ")
              .replaceFirstChar { it.uppercase() },
            icon = Icons.Default.GraphicEq,
            accent = MaterialTheme.colorScheme.primary,
            testTag = "quick_card_sounds",
            onClick = { onNavigate(AuraRoute.SoundSettings) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // Favorites Strip
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Saved Favorites",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "${favoriteWallpapers.size} saved",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    item {
      if (favoriteWallpapers.isEmpty()) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier =
                Modifier.size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "No favorites pinned yet",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = "Tap the heart on any curated or generated wallpaper to keep it handy here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(vertical = 2.dp),
        ) {
          items(favoriteWallpapers, key = { it.id }) { item ->
            val isActive = item.id == preferences.activeWallpaper.id
            val borderColor by
              animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "fav_border",
              )
            Box(
              modifier =
                Modifier.width(108.dp)
                  .height(164.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                  .clickable { onSelectFavoriteWallpaper(item) }
            ) {
              WallpaperBackdropFromItem(item = item, modifier = Modifier.fillMaxSize())
              Box(
                modifier =
                  Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}

@Composable
private fun PreviewSpecRow(label: String, value: String) {
  Column {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun QuickAccessStudioCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accent: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(24.dp),
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.height(132.dp).testTag(testTag),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Box(
        modifier =
          Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accent,
          modifier = Modifier.size(22.dp),
        )
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
