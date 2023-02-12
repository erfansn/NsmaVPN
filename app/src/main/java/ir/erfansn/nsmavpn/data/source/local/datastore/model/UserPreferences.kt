package ir.erfansn.nsmavpn.data.source.local.datastore.model

import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences

fun UserPreferences.toProfile() = Profile(
    avatarUrl = profileProto.avatarUrl.ifEmpty { null },
    displayName = profileProto.displayName,
    emailAddress = profileProto.emailAddress
)

fun UserPreferences.toConfigurations() = Configurations(
    themeMode = when (themeModeProto) {
        null,
        ThemeModeProto.UNRECOGNIZED,
        ThemeModeProto.SYSTEM -> ThemeMode.SYSTEM
        ThemeModeProto.LIGHT -> ThemeMode.LIGHT
        ThemeModeProto.DARK -> ThemeMode.DARK
    },
)
