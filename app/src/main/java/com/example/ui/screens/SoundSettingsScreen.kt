package com.example.ui.screens

import android.app.Activity
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.model.DarkModePreference
import com.example.data.preferences.AuraUserPreferences
import com.example.system.AudioVolumeSnapshot
import com.example.system.SystemSettingsHelper
import com.example.ui.components.PermissionExplanationDialog
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SoundSettingsScreen(
  preferences: AuraUserPreferences,
  audioVolumes: AudioVolumeSnapshot,
  canWriteSettings: Boolean,
  screenTimeoutMs: Int,
  fontScale: Float,
  onRefreshSystemState: () -> Unit,
  onSetDarkMode: (DarkModePreference) -> Unit,
  onUpdateScreenTimeout: (Int) -> Unit,
  onUpdateFontScale: (Float) -> Unit,
  onSetVolume: (Int, Int) -> Unit,
  onApplySoundProfile: (String, String) -> Unit,
  onUpdateRingtone: (Uri?) -> Unit,
  onUpdateNotificationSound: (Uri?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var showWriteSettingsDialog by remember { mutableStateOf(false) }
  var playingType by remember { mutableStateOf<Int?>(null) }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        onRefreshSystemState()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      SystemSettingsHelper.stopPreviewTone()
    }
  }

  val ringtoneLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val uri: Uri? =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.data?.getParcelableExtra(
              RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
              Uri::class.java,
            )
          } else {
            @Suppress("DEPRECATION")
            result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
          }
        onUpdateRingtone(uri)
      }
    }

  val notificationLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val uri: Uri? =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.data?.getParcelableExtra(
              RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
              Uri::class.java,
            )
          } else {
            @Suppress("DEPRECATION")
            result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
          }
        onUpdateNotificationSound(uri)
      }
    }

  val timeoutOptions =
    listOf(
      15_000 to "15s",
      30_000 to "30s",
      60_000 to "1m",
      120_000 to "2m",
      300_000 to "5m",
    )

  val fontScaleOptions =
    listOf(0.85f to "Small", 1.0f to "Default", 1.15f to "Large", 1.3f to "XL")

  val soundProfiles =
    listOf(
      Triple("focus_quiet", "Focus Quiet", "Low ringer & muted alerts"),
      Triple("balanced", "Balanced", "Comfortable everyday levels"),
      Triple("vibrant_loud", "Vibrant Loud", "High ring & crisp media"),
    )

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column {
        Text(
          text = "QUICK SETTINGS & AUDIO",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Display & Sound Profiles",
          style = MaterialTheme.typography.headlineMedium,
        )
      }
    }

    // Permissions & Privacy Status Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (canWriteSettings) MaterialTheme.colorScheme.secondaryContainer
              else MaterialTheme.colorScheme.surfaceContainerHigh
          ),
        modifier = Modifier.fillMaxWidth().testTag("permissions_status_card"),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector =
                if (canWriteSettings) Icons.Default.CheckCircle else Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text =
                  if (canWriteSettings) "System Write Access Enabled"
                  else "System Write Access Needed",
                style = MaterialTheme.typography.titleMedium,
              )
              Text(
                text =
                  if (canWriteSettings)
                    "Screen timeout & font scale edits apply directly. Photo Picker & SET_WALLPAPER are active."
                  else
                    "Grant WRITE_SETTINGS to modify Screen Timeout and Font Size directly from AuraSpace.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          if (!canWriteSettings) {
            Button(
              onClick = { showWriteSettingsDialog = true },
              modifier = Modifier.fillMaxWidth().testTag("request_write_settings_button"),
            ) {
              Text("Review & Grant WRITE_SETTINGS")
            }
          }
        }
      }
    }

    // Display Personalizer Card
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
          Text("Display & Typography", style = MaterialTheme.typography.titleMedium)

          // Dark Mode Toggle
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("App Appearance", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DarkModePreference.entries.forEach { mode ->
                FilterChip(
                  selected = preferences.darkModePreference == mode,
                  onClick = { onSetDarkMode(mode) },
                  label = { Text(mode.label) },
                  modifier = Modifier.testTag("dark_mode_${mode.name.lowercase()}"),
                )
              }
            }
          }

          // Screen Timeout
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Screen Timeout", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(timeoutOptions) { (ms, label) ->
                FilterChip(
                  selected = screenTimeoutMs == ms,
                  onClick = {
                    if (canWriteSettings) onUpdateScreenTimeout(ms)
                    else showWriteSettingsDialog = true
                  },
                  label = { Text(label) },
                )
              }
            }
          }

          // Font Scale
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("System Font Size", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(fontScaleOptions) { (scale, label) ->
                FilterChip(
                  selected = abs(fontScale - scale) < 0.05f,
                  onClick = {
                    if (canWriteSettings) onUpdateFontScale(scale)
                    else showWriteSettingsDialog = true
                  },
                  label = { Text("$label (${scale}x)") },
                )
              }
            }
          }

          // System Settings Shortcuts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { SystemSettingsHelper.openSystemDisplaySettings(context) },
              modifier = Modifier.weight(1f).testTag("open_display_settings_button"),
            ) {
              Icon(
                Icons.Default.DisplaySettings,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Display")
            }
            OutlinedButton(
              onClick = { SystemSettingsHelper.openSystemSoundSettings(context) },
              modifier = Modifier.weight(1f).testTag("open_sound_settings_button"),
            ) {
              Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sound OS")
            }
          }
        }
      }
    }

    // Sound Profiles Card
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
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text("Sound Profiles", style = MaterialTheme.typography.titleMedium)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(soundProfiles) { (id, label, desc) ->
              val isSelected = preferences.activeSoundProfileId == id
              Surface(
                onClick = { onApplySoundProfile(id, label) },
                shape = RoundedCornerShape(18.dp),
                color =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(156.dp).testTag("sound_profile_$id"),
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }

          VolumeSliderRow(
            label = "Ring Volume",
            icon = Icons.Default.RingVolume,
            level = audioVolumes.ringLevel,
            max = audioVolumes.ringMax,
            onChange = { onSetVolume(AudioManager.STREAM_RING, it) },
          )
          VolumeSliderRow(
            label = "Media Volume",
            icon = Icons.Default.VolumeUp,
            level = audioVolumes.mediaLevel,
            max = audioVolumes.mediaMax,
            onChange = { onSetVolume(AudioManager.STREAM_MUSIC, it) },
          )
          VolumeSliderRow(
            label = "Notification Volume",
            icon = Icons.Default.NotificationsActive,
            level = audioVolumes.notifLevel,
            max = audioVolumes.notifMax,
            onChange = { onSetVolume(AudioManager.STREAM_NOTIFICATION, it) },
          )
          VolumeSliderRow(
            label = "Alarm Volume",
            icon = Icons.Default.Alarm,
            level = audioVolumes.alarmLevel,
            max = audioVolumes.alarmMax,
            onChange = { onSetVolume(AudioManager.STREAM_ALARM, it) },
          )
        }
      }
    }

    // Ringtone & Notification Tone Picker
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
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text("Custom Audio Pickers", style = MaterialTheme.typography.titleMedium)

          TonePickerRow(
            title = "Ringtone",
            currentName = preferences.selectedRingtoneName,
            icon = Icons.Default.MusicNote,
            isPlaying = playingType == RingtoneManager.TYPE_RINGTONE,
            pickTestTag = "pick_ringtone_button",
            onPick = {
              val intent =
                SystemSettingsHelper.buildRingtonePickerIntent(
                  context = context,
                  type = RingtoneManager.TYPE_RINGTONE,
                  currentUriString = preferences.selectedRingtoneUri,
                  title = "Choose AuraSpace Ringtone",
                )
              ringtoneLauncher.launch(intent)
            },
            onTogglePlay = {
              if (playingType == RingtoneManager.TYPE_RINGTONE) {
                SystemSettingsHelper.stopPreviewTone()
                playingType = null
              } else {
                val started =
                  SystemSettingsHelper.playPreviewTone(
                    context = context,
                    uriString = preferences.selectedRingtoneUri,
                    fallbackType = RingtoneManager.TYPE_RINGTONE,
                  )
                playingType = if (started) RingtoneManager.TYPE_RINGTONE else null
              }
            },
          )

          TonePickerRow(
            title = "Notification Sound",
            currentName = preferences.selectedNotificationName,
            icon = Icons.Default.NotificationsActive,
            isPlaying = playingType == RingtoneManager.TYPE_NOTIFICATION,
            pickTestTag = "pick_notification_button",
            onPick = {
              val intent =
                SystemSettingsHelper.buildRingtonePickerIntent(
                  context = context,
                  type = RingtoneManager.TYPE_NOTIFICATION,
                  currentUriString = preferences.selectedNotificationUri,
                  title = "Choose Notification Tone",
                )
              notificationLauncher.launch(intent)
            },
            onTogglePlay = {
              if (playingType == RingtoneManager.TYPE_NOTIFICATION) {
                SystemSettingsHelper.stopPreviewTone()
                playingType = null
              } else {
                val started =
                  SystemSettingsHelper.playPreviewTone(
                    context = context,
                    uriString = preferences.selectedNotificationUri,
                    fallbackType = RingtoneManager.TYPE_NOTIFICATION,
                  )
                playingType = if (started) RingtoneManager.TYPE_NOTIFICATION else null
              }
            },
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }

  if (showWriteSettingsDialog) {
    PermissionExplanationDialog(
      title = stringResource(R.string.perm_write_settings_title),
      message = stringResource(R.string.perm_write_settings_desc),
      confirmLabel = "Open System Approval",
      onConfirm = {
        showWriteSettingsDialog = false
        SystemSettingsHelper.openWriteSettingsScreen(context)
      },
      onDismiss = { showWriteSettingsDialog = false },
    )
  }
}

@Composable
private fun VolumeSliderRow(
  label: String,
  icon: ImageVector,
  level: Int,
  max: Int,
  onChange: (Int) -> Unit,
) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
      }
      Text(
        text = "$level / $max",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
      )
    }
    Slider(
      value = level.toFloat(),
      onValueChange = { onChange(it.roundToInt()) },
      valueRange = 0f..max.toFloat(),
    )
  }
}

@Composable
private fun TonePickerRow(
  title: String,
  currentName: String,
  icon: ImageVector,
  isPlaying: Boolean,
  pickTestTag: String,
  onPick: () -> Unit,
  onTogglePlay: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(
          text = currentName,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      IconButton(onClick = onTogglePlay, modifier = Modifier.size(48.dp)) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Stop preview" else "Play preview",
        )
      }
      FilledTonalButton(onClick = onPick, modifier = Modifier.testTag(pickTestTag)) {
        Text("Pick")
      }
    }
  }
}
