package com.example.data.model

import com.example.R
import com.example.data.local.CustomPaletteEntity
import com.example.data.local.WidgetConfigEntity

object AuraDefaults {
  val curatedWallpapers: List<WallpaperItem> =
    listOf(
      WallpaperItem(
        id = "wall_nature_aurora",
        title = "Aurora Alpine",
        subtitle = "Twilight peaks & emerald sky",
        category = WallpaperCategory.NATURE,
        drawableResId = R.drawable.img_wall_nature_1794736920106,
      ),
      WallpaperItem(
        id = "wall_abstract_silk",
        title = "Iridescent Silk",
        subtitle = "Cobalt & amethyst glass waves",
        category = WallpaperCategory.ABSTRACT,
        drawableResId = R.drawable.img_wall_abstract_1794736918780,
      ),
      WallpaperItem(
        id = "wall_minimal_dunes",
        title = "Sandstone Arch",
        subtitle = "Warm architectural dunes",
        category = WallpaperCategory.MINIMAL,
        drawableResId = R.drawable.img_wall_minimal_1794736920468,
      ),
      WallpaperItem(
        id = "wall_amoled_ribbon",
        title = "Obsidian Ribbon",
        subtitle = "Pure black OLED liquid light",
        category = WallpaperCategory.AMOLED_DARK,
        drawableResId = R.drawable.img_wall_amoled_1794736919717,
      ),
      WallpaperItem(
        id = "wall_grad_solstice",
        title = "Solstice Bloom",
        subtitle = "Indigo, orchid & coral mesh",
        category = WallpaperCategory.GRADIENTS,
        gradientColorsHex = listOf("#1E1B4B", "#7C3AED", "#FB7185"),
        gradientStyle = GradientStyle.MESH_AURA,
      ),
      WallpaperItem(
        id = "wall_grad_oceanic",
        title = "Deep Abyss",
        subtitle = "Midnight navy to bioluminescent teal",
        category = WallpaperCategory.GRADIENTS,
        gradientColorsHex = listOf("#090D16", "#0F4C81", "#2DD4BF"),
        gradientStyle = GradientStyle.LINEAR_DIAGONAL,
      ),
      WallpaperItem(
        id = "wall_minimal_linen",
        title = "Nordic Mist",
        subtitle = "Soft alabaster & slate horizon",
        category = WallpaperCategory.MINIMAL,
        gradientColorsHex = listOf("#F1F5F9", "#CBD5E1", "#94A3B8"),
        gradientStyle = GradientStyle.LINEAR_VERTICAL,
      ),
      WallpaperItem(
        id = "wall_minimal_clay",
        title = "Terracotta Calm",
        subtitle = "Muted clay & warm oat",
        category = WallpaperCategory.MINIMAL,
        gradientColorsHex = listOf("#EFE3D8", "#D8B4A0", "#9E7B6B"),
        gradientStyle = GradientStyle.RADIAL_GLOW,
      ),
      WallpaperItem(
        id = "wall_amoled_ember",
        title = "OLED Ember Core",
        subtitle = "True #000000 black with amber halo",
        category = WallpaperCategory.AMOLED_DARK,
        gradientColorsHex = listOf("#000000", "#050508", "#F97316"),
        gradientStyle = GradientStyle.RADIAL_GLOW,
      ),
      WallpaperItem(
        id = "wall_amoled_cyber",
        title = "Void Cyan",
        subtitle = "Pure OLED dark & electric cyan",
        category = WallpaperCategory.AMOLED_DARK,
        gradientColorsHex = listOf("#000000", "#030712", "#06B6D4"),
        gradientStyle = GradientStyle.MESH_AURA,
      ),
      WallpaperItem(
        id = "wall_nature_forest",
        title = "Moss Canopy",
        subtitle = "Deep evergreen & sunlit mist",
        category = WallpaperCategory.NATURE,
        gradientColorsHex = listOf("#062019", "#1B4332", "#74C69D"),
        gradientStyle = GradientStyle.LINEAR_DIAGONAL,
      ),
      WallpaperItem(
        id = "wall_abstract_prism",
        title = "Prism Flare",
        subtitle = "Magenta & electric violet aura",
        category = WallpaperCategory.ABSTRACT,
        gradientColorsHex = listOf("#140B29", "#EC4899", "#38BDF8"),
        gradientStyle = GradientStyle.MESH_AURA,
      ),
    )

  val defaultPalettes: List<CustomPaletteEntity> =
    listOf(
      CustomPaletteEntity(
        id = "material_you",
        name = "Material You",
        subtitle = "Dynamic Android system tones",
        primaryHex = "#5E6AD2",
        secondaryHex = "#48BFE3",
        tertiaryHex = "#F4845F",
        surfaceHex = "#F8FAFC",
        isBuiltIn = true,
        isDynamicSystem = true,
      ),
      CustomPaletteEntity(
        id = "cyberpunk",
        name = "Cyberpunk",
        subtitle = "Neon magenta & electric cyan",
        primaryHex = "#FF2A85",
        secondaryHex = "#00E5FF",
        tertiaryHex = "#FFD600",
        surfaceHex = "#120E24",
        isBuiltIn = true,
      ),
      CustomPaletteEntity(
        id = "pastel_calm",
        name = "Pastel Calm",
        subtitle = "Soft periwinkle & dusty rose",
        primaryHex = "#7A89C2",
        secondaryHex = "#E5989B",
        tertiaryHex = "#81B29A",
        surfaceHex = "#FAF7F5",
        isBuiltIn = true,
      ),
      CustomPaletteEntity(
        id = "monochrome",
        name = "Monochrome",
        subtitle = "Crisp architectural grayscale",
        primaryHex = "#27272A",
        secondaryHex = "#71717A",
        tertiaryHex = "#A1A1AA",
        surfaceHex = "#F4F4F5",
        isBuiltIn = true,
      ),
      CustomPaletteEntity(
        id = "forest",
        name = "Forest",
        subtitle = "Deep pine & sage botanical",
        primaryHex = "#2D6A4F",
        secondaryHex = "#52B788",
        tertiaryHex = "#D8A47F",
        surfaceHex = "#F2F7F4",
        isBuiltIn = true,
      ),
    )

  val defaultWidgetConfigs: List<WidgetConfigEntity> =
    listOf(
      WidgetConfigEntity(
        templateId = WidgetTemplate.CLOCK_DATE.id,
        transparency = 0.78f,
        cornerRadiusDp = 24,
        fontStyle = WidgetFontStyle.GEOMETRIC.id,
        accentHex = "#6366F1",
        customQuoteText = "Simplicity is the ultimate sophistication.",
        customQuoteAuthor = "Leonardo da Vinci",
      ),
      WidgetConfigEntity(
        templateId = WidgetTemplate.BATTERY_STORAGE.id,
        transparency = 0.82f,
        cornerRadiusDp = 22,
        fontStyle = WidgetFontStyle.MONO.id,
        accentHex = "#10B981",
        customQuoteText = "Simplicity is the ultimate sophistication.",
        customQuoteAuthor = "Leonardo da Vinci",
      ),
      WidgetConfigEntity(
        templateId = WidgetTemplate.DAILY_QUOTE.id,
        transparency = 0.75f,
        cornerRadiusDp = 26,
        fontStyle = WidgetFontStyle.EDITORIAL.id,
        accentHex = "#F59E0B",
        customQuoteText = "Create spaces that feel calm, intentional, and unmistakably yours.",
        customQuoteAuthor = "AuraSpace Studio",
      ),
    )

  val curatedQuotes: List<Pair<String, String>> =
    listOf(
      "Create spaces that feel calm, intentional, and unmistakably yours." to "AuraSpace Studio",
      "Simplicity is about subtracting the obvious and adding the meaningful." to "John Maeda",
      "Details are not the details. They make the design." to "Charles Eames",
      "Focus is a matter of deciding what things you are not going to do." to "John Carmack",
      "Quiet the mind, and the soul will speak." to "Ma Jaya Sati Bhagavati",
    )
}
