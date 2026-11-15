package com.example.widgets

import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.example.data.model.WidgetTemplate

object WidgetPinManager {

  suspend fun refreshAllWidgets(context: Context) {
    runCatching { ClockDateWidget().updateAll(context) }
    runCatching { BatteryStorageWidget().updateAll(context) }
    runCatching { MotivationalQuoteWidget().updateAll(context) }
  }

  suspend fun requestPinWidget(context: Context, template: WidgetTemplate): Result<String> {
    return runCatching {
      refreshAllWidgets(context)
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        error("Pinning widgets from inside an app requires Android 8.0 or higher. Add it from your Home Screen widget picker.")
      }
      val manager = GlanceAppWidgetManager(context)
      val receiverClass =
        when (template) {
          WidgetTemplate.CLOCK_DATE -> ClockDateWidgetReceiver::class.java
          WidgetTemplate.BATTERY_STORAGE -> BatteryStorageWidgetReceiver::class.java
          WidgetTemplate.DAILY_QUOTE -> MotivationalQuoteWidgetReceiver::class.java
        }
      val supported = manager.requestPinGlanceAppWidget(receiver = receiverClass)
      if (!supported) {
        error("Your launcher does not support in-app widget pinning. Long-press your home screen and pick AuraSpace under Widgets.")
      }
      "Pin request sent for ${template.title}."
    }
  }
}
