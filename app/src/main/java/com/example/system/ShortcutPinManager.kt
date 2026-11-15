package com.example.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.data.model.IconShapeOption
import com.example.data.model.InstalledAppInfo
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShortcutPinManager {

  suspend fun loadInstalledApps(context: Context): List<InstalledAppInfo> =
    withContext(Dispatchers.IO) {
      val pm = context.packageManager
      val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
      val resolveInfos = pm.queryIntentActivities(intent, 0)
      resolveInfos
        .mapNotNull { info ->
          val activityInfo = info.activityInfo ?: return@mapNotNull null
          if (activityInfo.packageName == context.packageName) return@mapNotNull null
          InstalledAppInfo(
            packageName = activityInfo.packageName,
            activityName = activityInfo.name,
            label = info.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty {
              activityInfo.packageName
            },
          )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
    }

  fun loadAppIconDrawable(context: Context, packageName: String): Drawable? {
    return runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
  }

  fun pinCustomShortcut(
    context: Context,
    app: InstalledAppInfo,
    customLabel: String,
    shape: IconShapeOption,
    bgTintHex: String,
    fgTintHex: String,
    monochromeForeground: Boolean,
  ): Result<String> {
    return runCatching {
      if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        error("Your current home launcher does not support pinning custom shortcuts.")
      }

      val appDrawable = loadAppIconDrawable(context, app.packageName)
      val iconBitmap =
        renderCustomShortcutBitmap(
          sizePx = 192,
          appDrawable = appDrawable,
          shape = shape,
          bgTintHex = bgTintHex,
          fgTintHex = fgTintHex,
          monochromeForeground = monochromeForeground,
        )

      val launchIntent =
        Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
          component = ComponentName(app.packageName, app.activityName)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }

      val shortcutId = "auraspace_${app.packageName}_${shape.id}_${System.currentTimeMillis()}"
      val shortcut =
        ShortcutInfoCompat.Builder(context, shortcutId)
          .setShortLabel(customLabel.ifBlank { app.label })
          .setLongLabel(customLabel.ifBlank { app.label })
          .setIcon(IconCompat.createWithBitmap(iconBitmap))
          .setIntent(launchIntent)
          .build()

      val accepted = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
      if (!accepted) {
        error("Launcher declined the shortcut pin request.")
      }
      "Pin prompt sent to launcher for ${customLabel.ifBlank { app.label }}."
    }
  }

  fun renderCustomShortcutBitmap(
    sizePx: Int,
    appDrawable: Drawable?,
    shape: IconShapeOption,
    bgTintHex: String,
    fgTintHex: String,
    monochromeForeground: Boolean,
  ): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val pad = sizePx * 0.06f
    val rect = RectF(pad, pad, sizePx - pad, sizePx - pad)

    val bgColor = runCatching { Color.parseColor(bgTintHex) }.getOrDefault(Color.parseColor("#5E6AD2"))
    val fgColor = runCatching { Color.parseColor(fgTintHex) }.getOrDefault(Color.WHITE)

    val bgDarker = shiftColorBrightness(bgColor, 0.78f)
    val bgPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
          LinearGradient(
            0f,
            0f,
            sizePx.toFloat(),
            sizePx.toFloat(),
            bgColor,
            bgDarker,
            Shader.TileMode.CLAMP,
          )
      }

    val shapePath = createShapePath(rect, shape)
    canvas.drawPath(shapePath, bgPaint)

    if (appDrawable != null) {
      val inset = (sizePx * 0.24f).toInt()
      val mutableDrawable = appDrawable.constantState?.newDrawable()?.mutate() ?: appDrawable.mutate()
      if (monochromeForeground) {
        mutableDrawable.colorFilter = PorterDuffColorFilter(fgColor, PorterDuff.Mode.SRC_IN)
      } else {
        mutableDrawable.clearColorFilter()
      }
      mutableDrawable.setBounds(inset, inset, sizePx - inset, sizePx - inset)
      mutableDrawable.draw(canvas)
    }

    return bitmap
  }

  private fun createShapePath(rect: RectF, shape: IconShapeOption): Path {
    val path = Path()
    val cx = rect.centerX()
    val cy = rect.centerY()
    val radius = rect.width() / 2f
    when (shape) {
      IconShapeOption.CIRCLE -> {
        path.addCircle(cx, cy, radius, Path.Direction.CW)
      }
      IconShapeOption.ROUNDED_SQUARE -> {
        val cr = rect.width() * 0.22f
        path.addRoundRect(rect, cr, cr, Path.Direction.CW)
      }
      IconShapeOption.SQUIRCLE -> {
        val cr = rect.width() * 0.36f
        path.addRoundRect(rect, cr, cr, Path.Direction.CW)
      }
      IconShapeOption.HEXAGON -> {
        for (i in 0 until 6) {
          val angle = Math.toRadians((60 * i - 30).toDouble())
          val x = cx + radius * cos(angle).toFloat()
          val y = cy + radius * sin(angle).toFloat()
          if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
      }
    }
    return path
  }

  private fun shiftColorBrightness(color: Int, factor: Float): Int {
    val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
    val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
    val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
    return Color.argb(Color.alpha(color), r, g, b)
  }
}
