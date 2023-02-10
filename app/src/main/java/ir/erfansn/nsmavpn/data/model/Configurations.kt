package ir.erfansn.nsmavpn.data.model

data class Configurations(
    val themeMode: ThemeMode,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }
