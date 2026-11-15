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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.WidgetConfigEntity
import com.example.data.model.AuraDefaults
import com.example.data.model.WidgetFontStyle
import com.example.data.model.WidgetTemplate
import com.example.data.repository.AuraRepository

class MotivationalQuoteWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val config = AuraRepository(context).getWidgetConfigOnce(WidgetTemplate.DAILY_QUOTE.id)
    provideContent { QuoteContent(config = config) }
  }

  @Composable
  private fun QuoteContent(config: WidgetConfigEntity) {
    val accentColor =
      runCatching { Color(AndroidColor.parseColor(config.accentHex)) }
        .getOrDefault(Color(0xFFF59E0B))
    val containerBg = Color(0xFF171326).copy(alpha = config.transparency.coerceIn(0.1f, 1f))
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
          text = "DAILY FOCUS",
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
          text = "NEXT",
          style =
            TextStyle(
              color = ColorProvider(Color.White.copy(alpha = 0.72f)),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
            ),
          modifier = GlanceModifier.clickable(actionRunCallback<CycleQuoteAction>()),
        )
      }
      Spacer(modifier = GlanceModifier.height(6.dp))
      Text(
        text = "\"${config.customQuoteText}\"",
        style =
          TextStyle(
            color = ColorProvider(Color.White),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily,
          ),
        maxLines = 3,
      )
      Spacer(modifier = GlanceModifier.height(6.dp))
      Text(
        text = "— ${config.customQuoteAuthor}",
        style =
          TextStyle(
            color = ColorProvider(accentColor.copy(alpha = 0.9f)),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = fontFamily,
          ),
      )
    }
  }
}

class CycleQuoteAction : ActionCallback {
  override suspend fun onAction(
    context: Context,
    glanceId: GlanceId,
    parameters: ActionParameters,
  ) {
    val repo = AuraRepository(context)
    val current = repo.getWidgetConfigOnce(WidgetTemplate.DAILY_QUOTE.id)
    val quotes = AuraDefaults.curatedQuotes
    val idx = quotes.indexOfFirst { it.first == current.customQuoteText }
    val next = quotes[(idx + 1) % quotes.size]
    repo.saveWidgetConfig(
      current.copy(customQuoteText = next.first, customQuoteAuthor = next.second)
    )
    MotivationalQuoteWidget().updateAll(context)
  }
}

class MotivationalQuoteWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = MotivationalQuoteWidget()
}
