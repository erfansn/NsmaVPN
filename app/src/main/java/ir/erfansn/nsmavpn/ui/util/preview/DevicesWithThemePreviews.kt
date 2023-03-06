package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/** Suitable for showing compose screen preview with parameters. */
@Preview(
    name = "Light theme",
    group = "Phone",
    showBackground = true
)
@Preview(
    name = "Dark theme",
    group = "Phone",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    group = "Foldable",
    name = "Light theme",
    showBackground = true,
    device = Devices.FOLDABLE
)
@Preview(
    group = "Foldable",
    name = "Dark theme",
    showBackground = true,
    device = Devices.FOLDABLE,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    group = "Tablet",
    name = "Light theme",
    showBackground = true,
    device = Devices.TABLET
)
@Preview(
    group = "Tablet",
    name = "Dark theme",
    showBackground = true,
    device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class DevicesWithThemePreviews
