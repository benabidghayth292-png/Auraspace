package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.local.CustomPaletteEntity
import com.example.data.model.IconShapeOption
import com.example.data.model.InstalledAppInfo
import com.example.system.ShortcutPinManager
import com.example.ui.components.ColorChipSelector
import com.example.ui.theme.parseHexColor

@Composable
fun ThemeIconStudioScreen(
  palettes: List<CustomPaletteEntity>,
  activePalette: CustomPaletteEntity?,
  installedApps: List<InstalledAppInfo>,
  onSelectPalette: (CustomPaletteEntity) -> Unit,
  onCreatePalette: (String, String, String, String, String) -> Unit,
  onDeletePalette: (String) -> Unit,
  onPinShortcut: (InstalledAppInfo, String, IconShapeOption, String, String, Boolean) -> Unit,
  onNotify: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  var showCreatePaletteDialog by remember { mutableStateOf(false) }
  var showAppPickerDialog by remember { mutableStateOf(false) }

  // Shortcut creator state
  var selectedApp by remember(installedApps) { mutableStateOf(installedApps.firstOrNull()) }
  var shortcutLabel by remember(selectedApp) { mutableStateOf(selectedApp?.label ?: "My App") }
  var selectedShape by remember { mutableStateOf(IconShapeOption.SQUIRCLE) }
  var bgTintHex by remember(activePalette) {
    mutableStateOf(activePalette?.primaryHex ?: "#5E6AD2")
  }
  var fgTintHex by remember { mutableStateOf("#FFFFFF") }
  var monochromeForeground by remember { mutableStateOf(true) }

  val tintSwatches =
    buildList {
        activePalette?.let {
          add(it.primaryHex)
          add(it.secondaryHex)
          add(it.tertiaryHex)
        }
        addAll(listOf("#5E6AD2", "#FF2A85", "#10B981", "#27272A", "#F59E0B", "#FFFFFF"))
      }
      .distinct()
      .take(7)

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "THEME & ICON STUDIO",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Palettes & Shortcuts",
            style = MaterialTheme.typography.headlineMedium,
          )
        }
        FilledTonalButton(
          onClick = { showCreatePaletteDialog = true },
          modifier = Modifier.testTag("create_palette_button"),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New")
        }
      }
    }

    // Curated & Custom Palettes
    item {
      Text(
        text = "Curated & Saved Palettes",
        style = MaterialTheme.typography.titleMedium,
      )
    }

    items(palettes, key = { it.id }) { palette ->
      val isSelected = palette.id == activePalette?.id
      Card(
        onClick = { onSelectPalette(palette) },
        shape = RoundedCornerShape(22.dp),
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isSelected) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceContainerLow
          ),
        modifier =
          Modifier.fillMaxWidth()
            .border(
              width = if (isSelected) 2.dp else 0.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
              shape = RoundedCornerShape(22.dp),
            )
            .testTag("palette_card_${palette.id}"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = palette.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Active palette",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            Text(
              text = palette.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            listOf(
              palette.primaryHex,
              palette.secondaryHex,
              palette.tertiaryHex,
              palette.surfaceHex,
            ).forEach { hex ->
              Box(
                modifier =
                  Modifier.size(22.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(hex))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
              )
            }
            if (!palette.isBuiltIn) {
              IconButton(
                onClick = { onDeletePalette(palette.id) },
                modifier = Modifier.size(48.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete custom palette",
                )
              }
            }
          }
        }
      }
    }

    // Exportable Color Hex Codes Card
    activePalette?.let { palette ->
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
          modifier = Modifier.fillMaxWidth().testTag("export_hex_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "Exportable Hex Tokens",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Tap any token to copy its hex code",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              FilledTonalButton(
                onClick = {
                  val exportJson =
                    """
                    {
                      "name": "${palette.name}",
                      "primary": "${palette.primaryHex}",
                      "secondary": "${palette.secondaryHex}",
                      "tertiary": "${palette.tertiaryHex}",
                      "surface": "${palette.surfaceHex}"
                    }
                    """
                      .trimIndent()
                  clipboardManager.setText(AnnotatedString(exportJson))
                  onNotify("Copied ${palette.name} hex JSON to clipboard")
                },
                modifier = Modifier.testTag("copy_all_hex_button"),
              ) {
                Icon(
                  Icons.Default.ContentCopy,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy JSON")
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              listOf(
                "Primary" to palette.primaryHex,
                "Secondary" to palette.secondaryHex,
                "Tertiary" to palette.tertiaryHex,
                "Surface" to palette.surfaceHex,
              ).forEach { (role, hex) ->
                Surface(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(hex))
                    onNotify("Copied $role hex $hex")
                  },
                  shape = RoundedCornerShape(14.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Box(
                      modifier =
                        Modifier.size(28.dp)
                          .clip(CircleShape)
                          .background(parseHexColor(hex))
                          .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Text(
                      text = role,
                      style = MaterialTheme.typography.labelSmall,
                      maxLines = 1,
                    )
                    Text(
                      text = hex,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Custom App-Shortcut Creator
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          ),
        modifier = Modifier.fillMaxWidth().testTag("shortcut_creator_card"),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = "Custom App-Shortcut Creator",
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = "Pick an installed app, style its icon shape and tint, and pin it directly to your launcher.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // App Picker Row + Live Rendered Shortcut Preview
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val previewBitmap =
              remember(selectedApp, selectedShape, bgTintHex, fgTintHex, monochromeForeground) {
                val appDrawable =
                  selectedApp?.let { ShortcutPinManager.loadAppIconDrawable(context, it.packageName) }
                ShortcutPinManager.renderCustomShortcutBitmap(
                    sizePx = 160,
                    appDrawable = appDrawable,
                    shape = selectedShape,
                    bgTintHex = bgTintHex,
                    fgTintHex = fgTintHex,
                    monochromeForeground = monochromeForeground,
                  )
                  .asImageBitmap()
              }

            Image(
              bitmap = previewBitmap,
              contentDescription = "Custom shortcut icon preview",
              modifier = Modifier.size(72.dp),
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = selectedApp?.label ?: "No apps detected",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              FilledTonalButton(
                onClick = { showAppPickerDialog = true },
                modifier = Modifier.testTag("pick_installed_app_button"),
              ) {
                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose Installed App (${installedApps.size})")
              }
            }
          }

          OutlinedTextField(
            value = shortcutLabel,
            onValueChange = { shortcutLabel = it },
            label = { Text("Shortcut Label") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("shortcut_label_input"),
          )

          // Icon Shape Selector
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Icon Shape", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(IconShapeOption.entries) { shape ->
                FilterChip(
                  selected = shape == selectedShape,
                  onClick = { selectedShape = shape },
                  label = { Text(shape.label) },
                  modifier = Modifier.testTag("shortcut_shape_${shape.id.lowercase()}"),
                )
              }
            }
          }

          // Background Tint
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Badge Background Tint", style = MaterialTheme.typography.titleSmall)
            ColorChipSelector(
              colorsHex = tintSwatches,
              selectedHex = bgTintHex,
              onSelect = { bgTintHex = it },
            )
          }

          // Monochrome Foreground Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Monochrome Icon Tint", style = MaterialTheme.typography.titleSmall)
              Text(
                text = "Recolor the app glyph to match your theme",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = monochromeForeground,
              onCheckedChange = { monochromeForeground = it },
            )
          }

          if (monochromeForeground) {
            ColorChipSelector(
              colorsHex = listOf("#FFFFFF", "#0F172A", "#00E5FF", "#FFD600", "#FF2A85"),
              selectedHex = fgTintHex,
              onSelect = { fgTintHex = it },
            )
          }

          Button(
            onClick = {
              val app = selectedApp
              if (app != null) {
                onPinShortcut(
                  app,
                  shortcutLabel,
                  selectedShape,
                  bgTintHex,
                  fgTintHex,
                  monochromeForeground,
                )
              } else {
                onNotify("Choose an installed app first")
              }
            },
            enabled = selectedApp != null,
            modifier = Modifier.fillMaxWidth().testTag("pin_shortcut_button"),
          ) {
            Icon(
              Icons.Default.AddToHomeScreen,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pin Custom Shortcut to Launcher")
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }

  // Create Custom Palette Dialog
  if (showCreatePaletteDialog) {
    CreatePaletteDialog(
      onDismiss = { showCreatePaletteDialog = false },
      onCreate = { name, primary, secondary, tertiary, surface ->
        showCreatePaletteDialog = false
        onCreatePalette(name, primary, secondary, tertiary, surface)
      },
    )
  }

  // Installed App Picker Dialog
  if (showAppPickerDialog) {
    AppPickerDialog(
      apps = installedApps,
      onSelect = { app ->
        selectedApp = app
        shortcutLabel = app.label
        showAppPickerDialog = false
      },
      onDismiss = { showAppPickerDialog = false },
    )
  }
}

@Composable
private fun CreatePaletteDialog(
  onDismiss: () -> Unit,
  onCreate: (String, String, String, String, String) -> Unit,
) {
  var name by remember { mutableStateOf("Sunset Neon") }
  var primary by remember { mutableStateOf("#FF4D6D") }
  var secondary by remember { mutableStateOf("#48BFE3") }
  var tertiary by remember { mutableStateOf("#FFBE0B") }
  var surface by remember { mutableStateOf("#141224") }

  val paletteOptions =
    listOf("#FF4D6D", "#6366F1", "#48BFE3", "#10B981", "#FFBE0B", "#EC4899", "#27272A")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Theme Palette") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Palette Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text("Primary Color ($primary)", style = MaterialTheme.typography.labelMedium)
        ColorChipSelector(
          colorsHex = paletteOptions,
          selectedHex = primary,
          onSelect = { primary = it },
        )
        Text("Secondary Color ($secondary)", style = MaterialTheme.typography.labelMedium)
        ColorChipSelector(
          colorsHex = paletteOptions,
          selectedHex = secondary,
          onSelect = { secondary = it },
        )
        Text("Surface Tone", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = surface == "#F8FAFC",
            onClick = { surface = "#F8FAFC" },
            label = { Text("Airy Light") },
          )
          FilterChip(
            selected = surface == "#141224",
            onClick = { surface = "#141224" },
            label = { Text("Midnight") },
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = { onCreate(name, primary, secondary, tertiary, surface) }) {
        Text("Save & Apply")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
private fun AppPickerDialog(
  apps: List<InstalledAppInfo>,
  onSelect: (InstalledAppInfo) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  var query by remember { mutableStateOf("") }
  val filtered =
    remember(apps, query) {
      if (query.isBlank()) apps
      else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select Installed App") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          placeholder = { Text("Search apps...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(modifier = Modifier.height(280.dp)) {
          items(filtered, key = { it.packageName }) { app ->
            val iconBitmap =
              remember(app.packageName) {
                ShortcutPinManager.loadAppIconDrawable(context, app.packageName)
                  ?.toBitmap(width = 72, height = 72)
                  ?.asImageBitmap()
              }
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clickable { onSelect(app) }
                  .padding(vertical = 8.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (iconBitmap != null) {
                Image(
                  bitmap = iconBitmap,
                  contentDescription = app.label,
                  modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)),
                )
              } else {
                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(34.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = app.label,
                  style = MaterialTheme.typography.bodyLarge,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = app.packageName,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    },
  )
}
