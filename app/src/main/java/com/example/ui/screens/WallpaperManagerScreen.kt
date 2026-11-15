package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GradientStyle
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperTarget
import com.example.ui.components.ColorChipSelector
import com.example.ui.components.PermissionExplanationDialog
import com.example.ui.components.WallpaperBackdrop
import com.example.ui.components.WallpaperBackdropFromItem
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.toHexString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperManagerScreen(
  wallpapers: List<WallpaperItem>,
  selectedCategory: WallpaperCategory,
  activeWallpaperId: String,
  isApplying: Boolean,
  onSelectCategory: (WallpaperCategory) -> Unit,
  onToggleFavorite: (WallpaperItem) -> Unit,
  onSaveGeneratedGradient: (String, List<String>, GradientStyle) -> WallpaperItem,
  onImportPhoto: (Uri) -> WallpaperItem,
  onApplyWallpaper: (WallpaperItem, WallpaperTarget) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showGenerator by remember { mutableStateOf(false) }
  var showPhotoPickerExplanation by remember { mutableStateOf(false) }
  var sheetWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
  var pendingApplyTarget by remember { mutableStateOf<Pair<WallpaperItem, WallpaperTarget>?>(null) }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        val imported = onImportPhoto(uri)
        sheetWallpaper = imported
      }
    }

  // Generator state
  val stopColors = remember { mutableStateListOf("#1E1B4B", "#6366F1", "#38BDF8") }
  var useThreeStops by remember { mutableStateOf(true) }
  var activeStopIndex by remember { mutableIntStateOf(1) }
  var selectedStyle by remember { mutableStateOf(GradientStyle.MESH_AURA) }
  var customTitle by remember { mutableStateOf("Aurora Gradient") }

  val activeColor = parseHexColor(stopColors.getOrElse(activeStopIndex) { "#6366F1" })
  var redChannel by remember(activeStopIndex, stopColors[activeStopIndex]) {
    mutableFloatStateOf(activeColor.red)
  }
  var greenChannel by remember(activeStopIndex, stopColors[activeStopIndex]) {
    mutableFloatStateOf(activeColor.green)
  }
  var blueChannel by remember(activeStopIndex, stopColors[activeStopIndex]) {
    mutableFloatStateOf(activeColor.blue)
  }

  val swatchPresets =
    listOf("#090D16", "#1E1B4B", "#6366F1", "#EC4899", "#F43F5E", "#F59E0B", "#10B981", "#38BDF8")

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 152.dp),
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Header + Actions
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "WALLPAPER STUDIO",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Curated & Generative",
              style = MaterialTheme.typography.headlineMedium,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            onClick = { showGenerator = !showGenerator },
            modifier = Modifier.weight(1f).testTag("toggle_gradient_generator_button"),
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showGenerator) "Hide Generator" else "Gradient Maker")
          }
          OutlinedButton(
            onClick = { showPhotoPickerExplanation = true },
            modifier = Modifier.weight(1f).testTag("import_photo_button"),
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pick Photo")
          }
        }

        // Category Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(WallpaperCategory.entries) { category ->
            FilterChip(
              selected = category == selectedCategory,
              onClick = { onSelectCategory(category) },
              label = { Text(category.label) },
              modifier = Modifier.testTag("wallpaper_tab_${category.name.lowercase()}"),
            )
          }
        }
      }
    }

    // Built-in Color/Gradient Generator
    item(span = { GridItemSpan(maxLineSpan) }) {
      AnimatedVisibility(visible = showGenerator) {
        val activeColors = if (useThreeStops) stopColors.toList() else stopColors.take(2)
        Card(
          shape = RoundedCornerShape(24.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
          modifier = Modifier.fillMaxWidth().testTag("gradient_generator_panel"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Local High-Res Gradient Engine",
                style = MaterialTheme.typography.titleMedium,
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("3 Colors", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                  checked = useThreeStops,
                  onCheckedChange = {
                    useThreeStops = it
                    if (!it && activeStopIndex > 1) activeStopIndex = 1
                  },
                )
              }
            }

            // Live Preview
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .height(170.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
              WallpaperBackdrop(
                gradientColorsHex = activeColors,
                gradientStyle = selectedStyle,
                contentDescription = "Generated gradient preview",
                modifier = Modifier.fillMaxSize(),
              )
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
              ) {
                Text(
                  text = activeColors.joinToString(" • "),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }

            // Stop Selector Pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              activeColors.forEachIndexed { idx, hex ->
                val isSelected = idx == activeStopIndex
                FilterChip(
                  selected = isSelected,
                  onClick = { activeStopIndex = idx },
                  leadingIcon = {
                    Box(
                      modifier =
                        Modifier.size(14.dp)
                          .clip(CircleShape)
                          .background(parseHexColor(hex))
                          .border(1.dp, Color.White, CircleShape)
                    )
                  },
                  label = { Text("Stop ${idx + 1}") },
                )
              }
            }

            // Quick Swatches
            ColorChipSelector(
              colorsHex = swatchPresets,
              selectedHex = stopColors[activeStopIndex],
              onSelect = { hex -> stopColors[activeStopIndex] = hex },
            )

            // RGB Sliders
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Fine Tune Stop ${activeStopIndex + 1} (${stopColors[activeStopIndex]})",
                style = MaterialTheme.typography.labelMedium,
              )
              Slider(
                value = redChannel,
                onValueChange = {
                  redChannel = it
                  stopColors[activeStopIndex] = Color(it, greenChannel, blueChannel).toHexString()
                },
              )
              Slider(
                value = greenChannel,
                onValueChange = {
                  greenChannel = it
                  stopColors[activeStopIndex] = Color(redChannel, it, blueChannel).toHexString()
                },
              )
              Slider(
                value = blueChannel,
                onValueChange = {
                  blueChannel = it
                  stopColors[activeStopIndex] = Color(redChannel, greenChannel, it).toHexString()
                },
              )
            }

            // Gradient Style
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(GradientStyle.entries) { style ->
                FilterChip(
                  selected = style == selectedStyle,
                  onClick = { selectedStyle = style },
                  label = { Text(style.label) },
                )
              }
            }

            OutlinedTextField(
              value = customTitle,
              onValueChange = { customTitle = it },
              label = { Text("Wallpaper Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  onSaveGeneratedGradient(customTitle, activeColors, selectedStyle)
                },
                modifier = Modifier.weight(1f).testTag("save_gradient_button"),
              ) {
                Text("Save Preset")
              }
              Button(
                onClick = {
                  val created = onSaveGeneratedGradient(customTitle, activeColors, selectedStyle)
                  sheetWallpaper = created
                },
                modifier = Modifier.weight(1f).testTag("apply_gradient_button"),
              ) {
                Text("Apply Gradient")
              }
            }
          }
        }
      }
    }

    // Empty State for Favorites
    if (wallpapers.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.FavoriteBorder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp),
            )
            Text("No wallpapers in this filter", style = MaterialTheme.typography.titleMedium)
            Text(
              "Favorite any wallpaper or create a custom gradient to fill this collection.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(wallpapers, key = { it.id }) { item ->
        val isActive = item.id == activeWallpaperId
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .aspectRatio(0.62f)
              .clip(RoundedCornerShape(22.dp))
              .border(
                width = if (isActive) 2.5.dp else 1.dp,
                color =
                  if (isActive) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(22.dp),
              )
              .clickable { sheetWallpaper = item }
              .testTag("wallpaper_card_${item.id}")
        ) {
          WallpaperBackdropFromItem(item = item, modifier = Modifier.fillMaxSize())

          // Favorite button (48dp touch target)
          IconButton(
            onClick = { onToggleFavorite(item) },
            modifier =
              Modifier.align(Alignment.TopEnd)
                .padding(4.dp)
                .size(48.dp)
                .testTag("favorite_btn_${item.id}"),
          ) {
            Box(
              modifier =
                Modifier.size(34.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.48f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector =
                  if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                tint = if (item.isFavorite) Color(0xFFFF4D6D) else Color.White,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          if (isActive) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text = "Active",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                )
              }
            }
          }

          // Bottom caption overlay
          Column(
            modifier =
              Modifier.fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
          ) {
            Text(
              text = item.title,
              style = MaterialTheme.typography.titleSmall,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = item.category.label,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.78f),
            )
          }
        }
      }
    }

    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(16.dp)) }
  }

  // Photo Picker privacy explanation dialog
  if (showPhotoPickerExplanation) {
    PermissionExplanationDialog(
      title = stringResource(R.string.perm_photo_picker_title),
      message = stringResource(R.string.perm_photo_picker_desc),
      confirmLabel = "Open Photo Picker",
      onConfirm = {
        showPhotoPickerExplanation = false
        photoPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      },
      onDismiss = { showPhotoPickerExplanation = false },
    )
  }

  // SET_WALLPAPER confirmation dialog before WallpaperManager execution
  pendingApplyTarget?.let { (item, target) ->
    PermissionExplanationDialog(
      title = stringResource(R.string.perm_wallpaper_title),
      message = stringResource(R.string.perm_wallpaper_desc),
      confirmLabel = "Apply to ${target.label}",
      onConfirm = {
        pendingApplyTarget = null
        sheetWallpaper = null
        onApplyWallpaper(item, target)
      },
      onDismiss = { pendingApplyTarget = null },
    )
  }

  // Apply Wallpaper Bottom Sheet
  sheetWallpaper?.let { item ->
    ModalBottomSheet(onDismissRequest = { sheetWallpaper = null }) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.width(86.dp).height(136.dp).clip(RoundedCornerShape(16.dp))) {
            WallpaperBackdropFromItem(item = item, modifier = Modifier.fillMaxSize())
          }
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleLarge)
            Text(
              text = item.subtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = item.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }

        if (isApplying) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Applying via Android WallpaperManager...")
          }
        } else {
          Text(
            text = "Choose where to apply",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = { pendingApplyTarget = item to WallpaperTarget.HOME },
              modifier = Modifier.weight(1f).testTag("apply_home_button"),
            ) {
              Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Home")
            }
            OutlinedButton(
              onClick = { pendingApplyTarget = item to WallpaperTarget.LOCK },
              modifier = Modifier.weight(1f).testTag("apply_lock_button"),
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Lock")
            }
          }
          Button(
            onClick = { pendingApplyTarget = item to WallpaperTarget.BOTH },
            modifier = Modifier.fillMaxWidth().testTag("apply_both_button"),
          ) {
            Icon(
              Icons.Default.Smartphone,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply to Home & Lock Screen")
          }
        }
      }
    }
  }
}
