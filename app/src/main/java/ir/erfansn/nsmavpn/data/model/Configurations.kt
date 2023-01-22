package ir.erfansn.nsmavpn.data.model

import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences

data class Configurations(
    val themeMode: ThemeMode,
)
enum class ThemeMode { LIGHT, DARK, SYSTEM }

fun UserPreferences.toConfigurations() = Configurations(
    themeMode = when (themeMode) {
        null,
        ThemeModeProto.UNRECOGNIZED,
        ThemeModeProto.SYSTEM -> ThemeMode.SYSTEM
        ThemeModeProto.LIGHT -> ThemeMode.LIGHT
        ThemeModeProto.DARK -> ThemeMode.DARK
    },
)
