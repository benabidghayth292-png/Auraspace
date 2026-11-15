package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GradientStyle
import com.example.data.model.IconShapeOption
import com.example.data.repository.AuraRepository
import com.example.system.ShortcutPinManager
import com.example.system.WallpaperApplier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `app name matches AuraSpace`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AuraSpace", appName)
  }

  @Test
  fun `gradient bitmap renders exact dimensions`() {
    val bitmap =
      WallpaperApplier.renderGradientBitmap(
        width = 360,
        height = 720,
        hexColors = listOf("#1E1B4B", "#6366F1", "#38BDF8"),
        style = GradientStyle.MESH_AURA,
      )
    assertEquals(360, bitmap.width)
    assertEquals(720, bitmap.height)
    bitmap.recycle()
  }

  @Test
  fun `custom shortcut bitmap renders at target size`() {
    val bitmap =
      ShortcutPinManager.renderCustomShortcutBitmap(
        sizePx = 128,
        appDrawable = null,
        shape = IconShapeOption.HEXAGON,
        bgTintHex = "#5E6AD2",
        fgTintHex = "#FFFFFF",
        monochromeForeground = true,
      )
    assertEquals(128, bitmap.width)
    assertEquals(128, bitmap.height)
    bitmap.recycle()
  }

  @Test
  fun `repository seeds default palettes and widget configs`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AuraRepository(context)
    repo.ensureSeeded()
    val palettes = repo.palettes.first()
    val widgets = repo.widgetConfigs.first()
    assertTrue(palettes.any { it.id == "cyberpunk" })
    assertEquals(3, widgets.size)
  }
}
