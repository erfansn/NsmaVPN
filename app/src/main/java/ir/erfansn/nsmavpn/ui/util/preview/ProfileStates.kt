package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

annotation class ProfileStates {

    @Preview(
        group = "Initial",
        name = "Light"
    )
    @Preview(
        group = "Initial",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewInitial

    @Preview(
        group = "WithoutSubscription",
        name = "Light"
    )
    @Preview(
        group = "WithoutSubscription",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewWithoutSubscription


    @Preview(
        name = "Phone",
        group = "Device type",
        showBackground = true
    )
    @Preview(
        name = "Foldable",
        group = "Device type",
        showBackground = true,
        device = Devices.FOLDABLE
    )
    @Preview(
        name = "Tablet",
        group = "Device type",
        showBackground = true,
        device = Devices.TABLET
    )
    annotation class PreviewDeviceType
}