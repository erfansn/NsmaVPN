package ir.erfansn.nsmavpn.ui.util.preview.parameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

object VpnSwitchPreviewParameterProvider : PreviewParameterProvider<VpnSwitchStatus> {
    override val values = sequenceOf(
        VpnSwitchStatus(
            enabled = true,
            connected = true
        ),
        VpnSwitchStatus(
            enabled = true,
            connected = false
        ),
        VpnSwitchStatus(
            enabled = false,
            connected = true
        ),
        VpnSwitchStatus(
            enabled = false,
            connected = true
        ),
    )
}

data class VpnSwitchStatus(
    val enabled: Boolean,
    val connected: Boolean,
)
