package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AuraRoute {
  @Serializable data object Home : AuraRoute

  @Serializable data object Wallpapers : AuraRoute

  @Serializable data object Widgets : AuraRoute

  @Serializable data object Themes : AuraRoute

  @Serializable data object SoundSettings : AuraRoute
}
