package ir.erfansn.nsmavpn.data.model

/**
 * The reason this name was selected for this model is that it consists of anything that can
 * be changed individually by users
 *
 * [Reference](https://stackoverflow.com/questions/2074384/options-settings-properties-configuration-preferences-when-and-why)
 */
data class Configurations(
    val themeMode: ThemeMode,
    val isEnableDynamicScheme: Boolean,
    val splitTunnelingAppIds: List<String>,
) {
    enum class ThemeMode { System, Light, Dark }
}
