package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class ThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val sampleColorHex: String
) {
    SUNSET(
        id = "sunset",
        title = "Sunset",
        subtitle = "Warm pastel peach · Coral & magenta twilight",
        sampleColorHex = "#FFF4EC"
    ),
    RED(
        id = "red",
        title = "Red",
        subtitle = "Obsidian crimson · Laser coral & flame",
        sampleColorHex = "#8E6B74"
    ),
    LIGHT(
        id = "light",
        title = "Light",
        subtitle = "Crisp studio white · Blue & Violet accents",
        sampleColorHex = "#F8FAFC"
    ),
    DARK(
        id = "dark",
        title = "Dark",
        subtitle = "Deep midnight cosmic · Neon cyan & purple",
        sampleColorHex = "#64748B"
    ),
    EMERALD(
        id = "emerald",
        title = "Emerald",
        subtitle = "Obsidian jade · Neon mint & matrix green",
        sampleColorHex = "#1F5240"
    ),
    CUSTOM(
        id = "custom",
        title = "Custom Background",
        subtitle = "Choose any HEX colour (#RRGGBB)",
        sampleColorHex = "#FFFFFF"
    )
}

class ThemeStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alic_theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PRESET = "theme_preset"
        private const val KEY_CUSTOM_HEX = "theme_custom_hex"
        val DEFAULT_PRESET = ThemePreset.SUNSET
        const val DEFAULT_CUSTOM_HEX = "#FFF4EC"
    }

    fun getThemePreset(): ThemePreset {
        val savedId = prefs.getString(KEY_PRESET, DEFAULT_PRESET.id)
        return ThemePreset.values().firstOrNull { it.id == savedId } ?: DEFAULT_PRESET
    }

    fun saveThemePreset(preset: ThemePreset) {
        prefs.edit().putString(KEY_PRESET, preset.id).apply()
    }

    fun getCustomHex(): String {
        return prefs.getString(KEY_CUSTOM_HEX, DEFAULT_CUSTOM_HEX) ?: DEFAULT_CUSTOM_HEX
    }

    fun saveCustomHex(hex: String) {
        val cleaned = if (hex.startsWith("#")) hex else "#$hex"
        prefs.edit().putString(KEY_CUSTOM_HEX, cleaned.uppercase()).apply()
    }
}
