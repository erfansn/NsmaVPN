package ir.erfansn.nsmavpn.data.model

import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeMode
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences

data class Configurations(
    val themeMode: ThemeMode,
    val reconnection: Reconnection,
)

data class Reconnection(
    val enable: Boolean,
    val intervalTime: Long,
    val retryCount: Int,
)

fun UserPreferences.toConfiguration() = Configurations(
    themeMode = themeMode,
    reconnection = Reconnection(
        enable = reconnection.enable,
        intervalTime = reconnection.intervalTime,
        retryCount = reconnection.retryCount
    )
)
