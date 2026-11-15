package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.DeviceTelemetry
import com.example.data.model.GradientStyle
import com.example.data.model.WallpaperItem
import com.example.data.model.WidgetTemplate
import com.example.data.preferences.ActiveWallpaperPreview
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.widgetComposeFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WallpaperBackdrop(
  drawableResId: Int? = null,
  userUriString: String? = null,
  gradientColorsHex: List<String> = emptyList(),
  gradientStyle: GradientStyle = GradientStyle.LINEAR_DIAGONAL,
  contentDescription: String? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  when {
    drawableResId != null -> {
      Image(
        painter = painterResource(id = drawableResId),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
      )
    }
    !userUriString.isNullOrBlank() -> {
      AsyncImage(
        model = ImageRequest.Builder(context).data(userUriString).crossfade(true).build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
      )
    }
    else -> {
      val colors =
        gradientColorsHex.map { parseHexColor(it) }.ifEmpty {
          listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
        }
      val safeColors = if (colors.size == 1) listOf(colors[0], colors[0]) else colors
      Canvas(modifier = modifier) {
        when (gradientStyle) {
          GradientStyle.LINEAR_VERTICAL -> {
            drawRect(brush = Brush.verticalGradient(safeColors))
          }
          GradientStyle.LINEAR_DIAGONAL -> {
            drawRect(
              brush =
                Brush.linearGradient(
                  colors = safeColors,
                  start = Offset.Zero,
                  end = Offset(size.width, size.height),
                )
            )
          }
          GradientStyle.RADIAL_GLOW -> {
            drawRect(color = safeColors.first())
            drawRect(
              brush =
                Brush.radialGradient(
                  colors = safeColors.reversed(),
                  center = Offset(size.width * 0.5f, size.height * 0.4f),
                  radius = maxOf(size.width, size.height) * 0.72f,
                )
            )
          }
          GradientStyle.MESH_AURA -> {
            drawRect(color = safeColors.first())
            val c1 = safeColors.getOrElse(1) { safeColors.first() }
            val c2 = safeColors.last()
            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(c1.copy(alpha = 0.92f), Color.Transparent),
                  center = Offset(size.width * 0.22f, size.height * 0.28f),
                  radius = size.width * 0.9f,
                )
            )
            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(c2.copy(alpha = 0.88f), Color.Transparent),
                  center = Offset(size.width * 0.8f, size.height * 0.74f),
                  radius = size.width * 0.95f,
                )
            )
          }
        }
      }
    }
  }
}

@Composable
fun WallpaperBackdropFromPreview(
  preview: ActiveWallpaperPreview,
  resolveDrawableId: (String?) -> Int?,
  modifier: Modifier = Modifier,
) {
  val style =
    runCatching { GradientStyle.valueOf(preview.gradientStyleName) }
      .getOrDefault(GradientStyle.LINEAR_DIAGONAL)
  val colors = preview.gradientColorsCsv?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
  WallpaperBackdrop(
    drawableResId = resolveDrawableId(preview.drawableResName),
    userUriString = preview.userUriString,
    gradientColorsHex = colors,
    gradientStyle = style,
    contentDescription = preview.title,
    modifier = modifier,
  )
}

@Composable
fun WallpaperBackdropFromItem(item: WallpaperItem, modifier: Modifier = Modifier) {
  WallpaperBackdrop(
    drawableResId = item.drawableResId,
    userUriString = item.userUriString,
    gradientColorsHex = item.gradientColorsHex,
    gradientStyle = item.gradientStyle,
    contentDescription = item.title,
    modifier = modifier,
  )
}

@Composable
fun LiveWidgetPreviewCard(
  template: WidgetTemplate,
  config: WidgetConfigEntity,
  telemetry: DeviceTelemetry,
  compact: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val accentColor = parseHexColor(config.accentHex, MaterialTheme.colorScheme.primary)
  val bgAlpha = config.transparency.coerceIn(0.12f, 1f)
  val shape = RoundedCornerShape(config.cornerRadiusDp.dp)
  val fontFamily = widgetComposeFontFamily(config.fontStyle)

  Box(
    modifier =
      modifier
        .clip(shape)
        .background(Color(0xFF101324).copy(alpha = bgAlpha))
        .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
        .padding(if (compact) 12.dp else 16.dp)
  ) {
    when (template) {
      WidgetTemplate.CLOCK_DATE -> {
        val now = Date()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now)
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "AURASPACE",
            fontFamily = JetBrainsMonoFamily,
            fontSize = if (compact) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
          )
          Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
          Text(
            text = timeStr,
            fontFamily = fontFamily,
            fontSize = if (compact) 28.sp else 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
          Text(
            text = dateStr,
            fontFamily = fontFamily,
            fontSize = if (compact) 11.sp else 14.sp,
            color = Color.White.copy(alpha = 0.8f),
          )
        }
      }

      WidgetTemplate.BATTERY_STORAGE -> {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "DEVICE VITALS",
              fontFamily = JetBrainsMonoFamily,
              fontSize = if (compact) 9.sp else 11.sp,
              fontWeight = FontWeight.Bold,
              color = accentColor,
            )
            Icon(
              imageVector =
                if (telemetry.isCharging) Icons.Default.BatteryChargingFull
                else Icons.Default.BatteryStd,
              contentDescription = "Battery",
              tint = accentColor,
              modifier = Modifier.size(if (compact) 14.dp else 18.dp),
            )
          }
          Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
          Text(
            text = "Battery ${telemetry.batteryPercent}%",
            fontFamily = fontFamily,
            fontSize = if (compact) 11.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
          )
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { telemetry.batteryPercent / 100f },
            modifier = Modifier.fillMaxWidth().height(if (compact) 5.dp else 7.dp).clip(CircleShape),
            color = accentColor,
            trackColor = Color.White.copy(alpha = 0.16f),
          )
          Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
          Text(
            text = "Storage ${telemetry.storageUsedPercent}% (${telemetry.storageFreeGb} free)",
            fontFamily = fontFamily,
            fontSize = if (compact) 10.sp else 12.sp,
            color = Color.White.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { telemetry.storageUsedPercent / 100f },
            modifier = Modifier.fillMaxWidth().height(if (compact) 5.dp else 7.dp).clip(CircleShape),
            color = Color(0xFF38BDF8),
            trackColor = Color.White.copy(alpha = 0.16f),
          )
        }
      }

      WidgetTemplate.DAILY_QUOTE -> {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "DAILY FOCUS",
            fontFamily = JetBrainsMonoFamily,
            fontSize = if (compact) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
          )
          Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
          Text(
            text = "\"${config.customQuoteText}\"",
            fontFamily = fontFamily,
            fontSize = if (compact) 12.sp else 15.sp,
            lineHeight = if (compact) 16.sp else 21.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
          Text(
            text = "— ${config.customQuoteAuthor}",
            fontFamily = fontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = if (compact) 10.sp else 12.sp,
            color = accentColor.copy(alpha = 0.92f),
          )
        }
      }
    }
  }
}

@Composable
fun PhoneHomeScreenMockup(
  wallpaperPreview: ActiveWallpaperPreview,
  resolveDrawableId: (String?) -> Int?,
  widgetConfig: WidgetConfigEntity,
  telemetry: DeviceTelemetry,
  modifier: Modifier = Modifier,
) {
  val primary = MaterialTheme.colorScheme.primary
  val secondary = MaterialTheme.colorScheme.secondary
  val tertiary = MaterialTheme.colorScheme.tertiary
  val containerColor = MaterialTheme.colorScheme.primaryContainer

  Box(
    modifier =
      modifier
        .width(156.dp)
        .height(280.dp)
        .clip(RoundedCornerShape(28.dp))
        .border(3.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
  ) {
    WallpaperBackdropFromPreview(
      preview = wallpaperPreview,
      resolveDrawableId = resolveDrawableId,
      modifier = Modifier.fillMaxSize(),
    )
    // Scrim for legibility
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.25f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.45f),
              )
            )
          )
    )
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Status bar + notch pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "9:41",
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Box(
          modifier =
            Modifier.size(8.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.7f))
        )
        Text(
          text = "${telemetry.batteryPercent}%",
          color = Color.White,
          fontSize = 9.sp,
          fontFamily = JetBrainsMonoFamily,
        )
      }

      // Live widget preview inside mockup
      LiveWidgetPreviewCard(
        template = WidgetTemplate.CLOCK_DATE,
        config = widgetConfig,
        telemetry = telemetry,
        compact = true,
        modifier = Modifier.fillMaxWidth(),
      )

      // Themed icon dock
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          MockDockIcon(Icons.Default.Phone, primary)
          MockDockIcon(Icons.Default.ChatBubble, secondary)
          MockDockIcon(Icons.Default.MusicNote, tertiary)
          MockDockIcon(Icons.Default.CameraAlt, containerColor, tint = primary)
        }
      }
    }
  }
}

@Composable
private fun MockDockIcon(
  icon: ImageVector,
  bgColor: Color,
  tint: Color = Color.White,
) {
  Box(
    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(bgColor),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Composable
fun ColorChipSelector(
  colorsHex: List<String>,
  selectedHex: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    colorsHex.forEach { hex ->
      val color = parseHexColor(hex)
      val isSelected = hex.equals(selectedHex, ignoreCase = true)
      Box(
        modifier =
          Modifier.size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(
              width = if (isSelected) 3.dp else 1.dp,
              color =
                if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outline,
              shape = CircleShape,
            )
            .clickable { onSelect(hex) },
        contentAlignment = Alignment.Center,
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected $hex",
            tint = if (color.red * 0.3f + color.green * 0.59f + color.blue * 0.11f > 0.6f) Color.Black else Color.White,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
fun PermissionExplanationDialog(
  title: String,
  message: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    },
    title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
    text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.testTag("permission_confirm_button"),
      ) {
        Text(confirmLabel)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("permission_dismiss_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}
