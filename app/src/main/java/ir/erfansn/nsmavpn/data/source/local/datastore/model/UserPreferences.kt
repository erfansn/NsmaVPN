package ir.erfansn.nsmavpn.data.source.local.datastore.model

import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences

fun UserPreferences.toConfigurations() = Configurations(
    themeMode = when (themeModeProto) {
        null,
        ThemeModeProto.UNRECOGNIZED,
        ThemeModeProto.SYSTEM -> Configurations.ThemeMode.System
        ThemeModeProto.LIGHT -> Configurations.ThemeMode.Light
        ThemeModeProto.DARK -> Configurations.ThemeMode.Dark
    },
    isEnableDynamicScheme = enableDynamicScheme,
    splitTunnelingAppIds = splitTunnelingAppIdList
)
