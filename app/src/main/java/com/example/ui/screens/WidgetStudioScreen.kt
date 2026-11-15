package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CustomPaletteEntity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.AuraDefaults
import com.example.data.model.DeviceTelemetry
import com.example.data.model.WidgetFontStyle
import com.example.data.model.WidgetTemplate
import com.example.data.preferences.ActiveWallpaperPreview
import com.example.ui.components.ColorChipSelector
import com.example.ui.components.LiveWidgetPreviewCard
import com.example.ui.components.WallpaperBackdropFromPreview
import kotlin.math.roundToInt

@Composable
fun WidgetStudioScreen(
  widgetConfigs: List<WidgetConfigEntity>,
  activeWallpaper: ActiveWallpaperPreview,
  activePalette: CustomPaletteEntity?,
  telemetry: DeviceTelemetry,
  resolveDrawableId: (String?) -> Int?,
  onSaveConfig: (WidgetConfigEntity) -> Unit,
  onPinWidget: (WidgetTemplate, WidgetConfigEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTemplate by remember { mutableStateOf(WidgetTemplate.CLOCK_DATE) }
  val savedConfig =
    widgetConfigs.firstOrNull { it.templateId == selectedTemplate.id }
      ?: AuraDefaults.defaultWidgetConfigs.first { it.templateId == selectedTemplate.id }

  var transparency by remember(selectedTemplate) { mutableFloatStateOf(savedConfig.transparency) }
  var cornerRadiusDp by remember(selectedTemplate) { mutableIntStateOf(savedConfig.cornerRadiusDp) }
  var fontStyle by remember(selectedTemplate) { mutableStateOf(savedConfig.fontStyle) }
  var accentHex by remember(selectedTemplate) { mutableStateOf(savedConfig.accentHex) }
  var quoteText by remember(selectedTemplate) { mutableStateOf(savedConfig.customQuoteText) }
  var quoteAuthor by remember(selectedTemplate) { mutableStateOf(savedConfig.customQuoteAuthor) }

  LaunchedEffect(savedConfig) {
    transparency = savedConfig.transparency
    cornerRadiusDp = savedConfig.cornerRadiusDp
    fontStyle = savedConfig.fontStyle
    accentHex = savedConfig.accentHex
    quoteText = savedConfig.customQuoteText
    quoteAuthor = savedConfig.customQuoteAuthor
  }

  val liveConfig =
    WidgetConfigEntity(
      templateId = selectedTemplate.id,
      transparency = transparency,
      cornerRadiusDp = cornerRadiusDp,
      fontStyle = fontStyle,
      accentHex = accentHex,
      customQuoteText = quoteText.ifBlank { AuraDefaults.curatedQuotes.first().first },
      customQuoteAuthor = quoteAuthor.ifBlank { AuraDefaults.curatedQuotes.first().second },
    )

  val accentChoices =
    buildList {
        activePalette?.let {
          add(it.primaryHex)
          add(it.secondaryHex)
          add(it.tertiaryHex)
        }
        addAll(listOf("#6366F1", "#10B981", "#F59E0B", "#EC4899", "#38BDF8", "#FFFFFF"))
      }
      .distinct()
      .take(7)

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column {
        Text(
          text = "GLANCE WIDGET STUDIO",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Custom Home Widgets",
          style = MaterialTheme.typography.headlineMedium,
        )
      }
    }

    // Template Picker
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WidgetTemplate.entries) { template ->
          FilterChip(
            selected = template == selectedTemplate,
            onClick = { selectedTemplate = template },
            label = { Text(template.title) },
            modifier = Modifier.testTag("widget_template_${template.id}"),
          )
        }
      }
    }

    // Live Preview Stage over Active Wallpaper
    item {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
      ) {
        WallpaperBackdropFromPreview(
          preview = activeWallpaper,
          resolveDrawableId = resolveDrawableId,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier = Modifier.fillMaxSize().padding(22.dp),
          contentAlignment = Alignment.Center,
        ) {
          LiveWidgetPreviewCard(
            template = selectedTemplate,
            config = liveConfig,
            telemetry = telemetry,
            compact = false,
            modifier = Modifier.fillMaxWidth().testTag("widget_live_preview"),
          )
        }
      }
    }

    // Customization Controls Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = "Customize ${selectedTemplate.title}",
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = selectedTemplate.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // Transparency
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Background Opacity", style = MaterialTheme.typography.titleSmall)
              Text(
                text = "${(transparency * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Slider(
              value = transparency,
              onValueChange = { transparency = it },
              valueRange = 0.15f..1.0f,
              modifier = Modifier.testTag("widget_transparency_slider"),
            )
          }

          // Corner Radius
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Corner Radius", style = MaterialTheme.typography.titleSmall)
              Text(
                text = "${cornerRadiusDp} dp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Slider(
              value = cornerRadiusDp.toFloat(),
              onValueChange = { cornerRadiusDp = it.roundToInt() },
              valueRange = 4f..32f,
              modifier = Modifier.testTag("widget_radius_slider"),
            )
          }

          // Font Style
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Font Style", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(WidgetFontStyle.entries) { style ->
                FilterChip(
                  selected = fontStyle == style.id,
                  onClick = { fontStyle = style.id },
                  label = { Text(style.label) },
                  modifier = Modifier.testTag("widget_font_${style.id.lowercase()}"),
                )
              }
            }
          }

          // Accent Color
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Accent Color", style = MaterialTheme.typography.titleSmall)
            ColorChipSelector(
              colorsHex = accentChoices,
              selectedHex = accentHex,
              onSelect = { accentHex = it },
            )
          }

          // Quote editor for Daily Quote widget
          if (selectedTemplate == WidgetTemplate.DAILY_QUOTE) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("Quote Text", style = MaterialTheme.typography.titleSmall)
                FilledTonalButton(
                  onClick = {
                    val quotes = AuraDefaults.curatedQuotes
                    val idx = quotes.indexOfFirst { it.first == quoteText }
                    val next = quotes[(idx + 1) % quotes.size]
                    quoteText = next.first
                    quoteAuthor = next.second
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Shuffle")
                }
              }
              OutlinedTextField(
                value = quoteText,
                onValueChange = { quoteText = it },
                label = { Text("Motivational Quote") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
              )
              OutlinedTextField(
                value = quoteAuthor,
                onValueChange = { quoteAuthor = it },
                label = { Text("Author") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            FilledTonalButton(
              onClick = { onSaveConfig(liveConfig) },
              modifier = Modifier.weight(1f).testTag("save_widget_config_button"),
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save & Sync")
            }
            Button(
              onClick = { onPinWidget(selectedTemplate, liveConfig) },
              modifier = Modifier.weight(1f).testTag("pin_widget_button"),
            ) {
              Icon(
                Icons.Default.AddToHomeScreen,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add to Home")
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}
