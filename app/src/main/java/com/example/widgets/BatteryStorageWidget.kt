package com.example.widgets

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import com.example.data.model.DeviceTelemetry
import com.example.data.model.WidgetFontStyle
import com.example.data.model.WidgetTemplate
import com.example.data.repository.AuraRepository
import com.example.system.SystemSettingsHelper

class BatteryStorageWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val config = AuraRepository(context).getWidgetConfigOnce(WidgetTemplate.BATTERY_STORAGE.id)
    val telemetry = SystemSettingsHelper.getDeviceTelemetry(context)

    provideContent { BatteryStorageContent(config = config, telemetry = telemetry) }
  }

  @Composable
  private fun BatteryStorageContent(config: WidgetConfigEntity, telemetry: DeviceTelemetry) {
    val accentColor =
      runCatching { Color(AndroidColor.parseColor(config.accentHex)) }
        .getOrDefault(Color(0xFF10B981))
    val containerBg = Color(0xFF101322).copy(alpha = config.transparency.coerceIn(0.1f, 1f))
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
    ) {
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "DEVICE VITALS",
          style =
            TextStyle(
              color = ColorProvider(accentColor),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            ),
          modifier = GlanceModifier.defaultWeight(),
        )
        Text(
          text = "REFRESH",
          style =
            TextStyle(
              color = ColorProvider(Color.White.copy(alpha = 0.7f)),
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
            ),
          modifier = GlanceModifier.clickable(actionRunCallback<RefreshBatteryWidgetAction>()),
        )
      }
      Spacer(modifier = GlanceModifier.height(10.dp))
      Text(
        text =
          if (telemetry.isCharging) "Battery ${telemetry.batteryPercent}% (Charging)"
          else "Battery ${telemetry.batteryPercent}%",
        style =
          TextStyle(
            color = ColorProvider(Color.White),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
          ),
      )
      Spacer(modifier = GlanceModifier.height(4.dp))
      LinearProgressIndicator(
        progress = telemetry.batteryPercent / 100f,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = ColorProvider(accentColor),
        backgroundColor = ColorProvider(Color.White.copy(alpha = 0.16f)),
      )
      Spacer(modifier = GlanceModifier.height(10.dp))
      Text(
        text = "Storage ${telemetry.storageUsedPercent}% used (${telemetry.storageFreeGb} free)",
        style =
          TextStyle(
            color = ColorProvider(Color.White.copy(alpha = 0.88f)),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = fontFamily,
          ),
      )
      Spacer(modifier = GlanceModifier.height(4.dp))
      LinearProgressIndicator(
        progress = telemetry.storageUsedPercent / 100f,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = ColorProvider(Color(0xFF38BDF8)),
        backgroundColor = ColorProvider(Color.White.copy(alpha = 0.16f)),
      )
    }
  }
}

class RefreshBatteryWidgetAction : ActionCallback {
  override suspend fun onAction(
    context: Context,
    glanceId: GlanceId,
    parameters: ActionParameters,
  ) {
    BatteryStorageWidget().updateAll(context)
  }
}

class BatteryStorageWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = BatteryStorageWidget()
}
