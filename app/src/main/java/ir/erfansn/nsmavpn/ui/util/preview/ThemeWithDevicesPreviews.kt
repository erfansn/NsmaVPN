package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone",
    group = "Light theme",
    showBackground = true
)
@Preview(
    name = "Foldable",
    group = "Light theme",
    showBackground = true,
    device = Devices.FOLDABLE
)
@Preview(
    name = "Tablet",
    group = "Light theme",
    showBackground = true,
    device = Devices.TABLET
)

@Preview(
    name = "Phone",
    group = "Dark theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Foldable",
    group = "Dark theme",
    showBackground = true,
    device = Devices.FOLDABLE,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Tablet",
    group = "Dark theme",
    showBackground = true,
    device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class ThemeWithDevicesPreviews
