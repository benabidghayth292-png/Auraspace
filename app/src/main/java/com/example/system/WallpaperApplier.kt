package com.example.system

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import com.example.data.model.GradientStyle
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperApplier {

  suspend fun applyWallpaper(
    context: Context,
    item: WallpaperItem,
    target: WallpaperTarget,
  ): Result<Unit> =
    withContext(Dispatchers.IO) {
      runCatching {
        val bitmap =
          createWallpaperBitmap(context, item)
            ?: error("Could not decode or render wallpaper image.")
        try {
          val manager = WallpaperManager.getInstance(context)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val flags =
              when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH ->
                  WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
              }
            manager.setBitmap(bitmap, null, true, flags)
          } else {
            manager.setBitmap(bitmap)
          }
        } finally {
          if (!bitmap.isRecycled) {
            bitmap.recycle()
          }
        }
        Unit
      }
    }

  fun createWallpaperBitmap(context: Context, item: WallpaperItem): Bitmap? {
    val metrics = context.resources.displayMetrics
    val targetW = metrics.widthPixels.coerceIn(1080, 1440)
    val targetH = metrics.heightPixels.coerceIn(1920, 3200)

    if (item.drawableResId != null) {
      val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
      return BitmapFactory.decodeResource(context.resources, item.drawableResId, opts)
    }

    if (!item.userUriString.isNullOrBlank()) {
      val uri = Uri.parse(item.userUriString)
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
      }
      var sampleSize = 1
      while (bounds.outWidth / sampleSize > targetW * 2 || bounds.outHeight / sampleSize > targetH * 2) {
        sampleSize *= 2
      }
      val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
      return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOpts)
      }
    }

    if (item.gradientColorsHex.isNotEmpty()) {
      return renderGradientBitmap(
        width = targetW,
        height = targetH,
        hexColors = item.gradientColorsHex,
        style = item.gradientStyle,
      )
    }

    return null
  }

  fun renderGradientBitmap(
    width: Int,
    height: Int,
    hexColors: List<String>,
    style: GradientStyle,
  ): Bitmap {
    val colors =
      hexColors.mapNotNull { runCatching { Color.parseColor(it) }.getOrNull() }.ifEmpty {
        listOf(Color.parseColor("#1E1B4B"), Color.parseColor("#6366F1"))
      }
    val safeColors = if (colors.size == 1) listOf(colors[0], colors[0]) else colors

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    when (style) {
      GradientStyle.LINEAR_VERTICAL -> {
        paint.shader =
          LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            safeColors.toIntArray(),
            null,
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      }

      GradientStyle.LINEAR_DIAGONAL -> {
        paint.shader =
          LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            safeColors.toIntArray(),
            null,
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      }

      GradientStyle.RADIAL_GLOW -> {
        canvas.drawColor(safeColors.first())
        val glowColors = safeColors.reversed().toIntArray()
        paint.shader =
          RadialGradient(
            width * 0.5f,
            height * 0.42f,
            maxOf(width, height) * 0.65f,
            glowColors,
            null,
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      }

      GradientStyle.MESH_AURA -> {
        canvas.drawColor(safeColors.first())
        val blob1 = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        val c1 = safeColors.getOrElse(1) { safeColors.first() }
        blob1.shader =
          RadialGradient(
            width * 0.22f,
            height * 0.28f,
            width * 0.85f,
            intArrayOf(c1, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), blob1)

        val blob2 = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        val c2 = safeColors.last()
        blob2.shader =
          RadialGradient(
            width * 0.8f,
            height * 0.72f,
            width * 0.9f,
            intArrayOf(c2, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), blob2)
      }
    }
    return bitmap
  }
}
