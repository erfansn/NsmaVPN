package ir.erfansn.nsmavpn.data.model

import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto

data class Configurations(
    val themeMode: ThemeMode,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

fun ThemeMode.asThemeModeProto() = when (this) {
    ThemeMode.LIGHT -> ThemeModeProto.LIGHT
    ThemeMode.DARK -> ThemeModeProto.DARK
    ThemeMode.SYSTEM -> ThemeModeProto.SYSTEM
}
