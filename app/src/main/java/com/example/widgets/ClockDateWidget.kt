package com.example.widgets

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.WidgetFontStyle
import com.example.data.model.WidgetTemplate
import com.example.data.repository.AuraRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockDateWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val config = AuraRepository(context).getWidgetConfigOnce(WidgetTemplate.CLOCK_DATE.id)
    val now = Date()
    val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    val dateText = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now)

    provideContent { ClockDateContent(config = config, timeText = timeText, dateText = dateText) }
  }

  @Composable
  private fun ClockDateContent(config: WidgetConfigEntity, timeText: String, dateText: String) {
    val accentColor =
      runCatching { Color(AndroidColor.parseColor(config.accentHex)) }
        .getOrDefault(Color(0xFF6366F1))
    val containerBg = Color(0xFF121426).copy(alpha = config.transparency.coerceIn(0.1f, 1f))
    val fontFamily =
      when (config.fontStyle) {
        WidgetFontStyle.EDITORIAL.id -> FontFamily.Serif
        WidgetFontStyle.MONO.id -> FontFamily.Monospace
        else -> FontFamily.SansSerif
      }

    Column(
      modifier =
        GlanceModifier.fillMaxSize()
          .background(ColorProvider(containerBg))
          .cornerRadius(config.cornerRadiusDp.dp)
          .clickable(actionStartActivity<MainActivity>())
          .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalAlignment = Alignment.Start,
    ) {
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "AURASPACE",
          style =
            TextStyle(
              color = ColorProvider(accentColor),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            ),
        )
      }
      Spacer(modifier = GlanceModifier.height(4.dp))
      Text(
        text = timeText,
        style =
          TextStyle(
            color = ColorProvider(Color.White),
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
          ),
      )
      Spacer(modifier = GlanceModifier.height(2.dp))
      Text(
        text = dateText,
        style =
          TextStyle(
            color = ColorProvider(Color.White.copy(alpha = 0.78f)),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
          ),
      )
    }
  }
}

class ClockDateWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = ClockDateWidget()
}
