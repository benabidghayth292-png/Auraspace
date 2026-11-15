package com.example.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.example.data.model.DeviceTelemetry
import java.util.Locale

data class AudioVolumeSnapshot(
  val ringLevel: Int,
  val ringMax: Int,
  val mediaLevel: Int,
  val mediaMax: Int,
  val notifLevel: Int,
  val notifMax: Int,
  val alarmLevel: Int,
  val alarmMax: Int,
)

object SystemSettingsHelper {

  private var activePreviewRingtone: Ringtone? = null

  fun getDeviceTelemetry(context: Context): DeviceTelemetry {
    val batteryStatus =
      context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val fallbackPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 82
    val batteryPct =
      if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt().coerceIn(0, 100)
      else fallbackPct.coerceIn(0, 100)

    val stat = StatFs(Environment.getDataDirectory().path)
    val totalBytes = stat.totalBytes.coerceAtLeast(1L)
    val availBytes = stat.availableBytes.coerceAtLeast(0L)
    val usedBytes = (totalBytes - availBytes).coerceAtLeast(0L)
    val usedPercent = ((usedBytes * 100.0) / totalBytes).toInt().coerceIn(0, 100)

    val freeGb = String.format(Locale.US, "%.1f GB", availBytes / (1024.0 * 1024.0 * 1024.0))
    val totalGb = String.format(Locale.US, "%.0f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))

    return DeviceTelemetry(
      batteryPercent = batteryPct,
      isCharging = isCharging,
      storageUsedPercent = usedPercent,
      storageFreeGb = freeGb,
      storageTotalGb = totalGb,
    )
  }

  fun canWriteSystemSettings(context: Context): Boolean {
    return Settings.System.canWrite(context)
  }

  fun openWriteSettingsScreen(context: Context) {
    val intent =
      Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { context.startActivity(intent) }
  }

  fun openSystemDisplaySettings(context: Context) {
    val intent =
      Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { context.startActivity(intent) }
  }

  fun openSystemSoundSettings(context: Context) {
    val intent =
      Intent(Settings.ACTION_SOUND_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { context.startActivity(intent) }
  }

  fun getScreenTimeoutMs(context: Context): Int {
    return runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
      }
      .getOrDefault(30_000)
  }

  fun setScreenTimeoutMs(context: Context, timeoutMs: Int): Boolean {
    if (!canWriteSystemSettings(context)) return false
    return runCatching {
        Settings.System.putInt(
          context.contentResolver,
          Settings.System.SCREEN_OFF_TIMEOUT,
          timeoutMs,
        )
      }
      .getOrDefault(false)
  }

  fun getFontScale(context: Context): Float {
    return runCatching {
        Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
      }
      .getOrDefault(context.resources.configuration.fontScale)
  }

  fun setFontScale(context: Context, scale: Float): Boolean {
    if (!canWriteSystemSettings(context)) return false
    return runCatching {
        Settings.System.putFloat(context.contentResolver, Settings.System.FONT_SCALE, scale)
      }
      .getOrDefault(false)
  }

  fun getAudioVolumes(context: Context): AudioVolumeSnapshot {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return AudioVolumeSnapshot(
      ringLevel = am.getStreamVolume(AudioManager.STREAM_RING),
      ringMax = am.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1),
      mediaLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC),
      mediaMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1),
      notifLevel = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
      notifMax = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION).coerceAtLeast(1),
      alarmLevel = am.getStreamVolume(AudioManager.STREAM_ALARM),
      alarmMax = am.getStreamMaxVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1),
    )
  }

  fun setStreamVolume(context: Context, streamType: Int, level: Int): Boolean {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val max = am.getStreamMaxVolume(streamType)
    return runCatching {
        am.setStreamVolume(streamType, level.coerceIn(0, max), 0)
        true
      }
      .getOrDefault(false)
  }

  fun applySoundProfile(context: Context, profileId: String): Boolean {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return runCatching {
        when (profileId) {
          "focus_quiet" -> {
            setStreamVolume(context, AudioManager.STREAM_MUSIC, (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.35f).toInt())
            setStreamVolume(context, AudioManager.STREAM_RING, (am.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.15f).toInt())
            setStreamVolume(context, AudioManager.STREAM_NOTIFICATION, 0)
          }
          "vibrant_loud" -> {
            setStreamVolume(context, AudioManager.STREAM_MUSIC, (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.85f).toInt())
            setStreamVolume(context, AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING))
            setStreamVolume(context, AudioManager.STREAM_NOTIFICATION, (am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) * 0.85f).toInt())
          }
          else -> {
            setStreamVolume(context, AudioManager.STREAM_MUSIC, (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.6f).toInt())
            setStreamVolume(context, AudioManager.STREAM_RING, (am.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.65f).toInt())
            setStreamVolume(context, AudioManager.STREAM_NOTIFICATION, (am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) * 0.6f).toInt())
          }
        }
        true
      }
      .getOrDefault(false)
  }

  fun buildRingtonePickerIntent(
    context: Context,
    type: Int,
    currentUriString: String,
    title: String,
  ): Intent {
    val existingUri =
      if (currentUriString.isNotBlank()) Uri.parse(currentUriString)
      else RingtoneManager.getActualDefaultRingtoneUri(context, type)
    return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
      putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, type)
      putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
      putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
      putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
      putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
    }
  }

  fun resolveRingtoneTitle(context: Context, uri: Uri?): String {
    if (uri == null) return "Silent"
    return runCatching {
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)?.takeIf { it.isNotBlank() }
          ?: "Custom Audio"
      }
      .getOrDefault("Custom Audio")
  }

  fun playPreviewTone(context: Context, uriString: String, fallbackType: Int): Boolean {
    stopPreviewTone()
    val uri =
      if (uriString.isNotBlank()) Uri.parse(uriString)
      else RingtoneManager.getDefaultUri(fallbackType)
    return runCatching {
        val ringtone = RingtoneManager.getRingtone(context, uri) ?: return false
        activePreviewRingtone = ringtone
        ringtone.play()
        true
      }
      .getOrDefault(false)
  }

  fun stopPreviewTone() {
    runCatching {
      activePreviewRingtone?.stop()
      activePreviewRingtone = null
    }
  }
}
