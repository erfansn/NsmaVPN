package ir.erfansn.nsmavpn.ui.util.preview.parameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ir.erfansn.nsmavpn.feature.auth.VpnGateSubscriptionStatus

class VpnGateSubscriptionStatusParameterProvider : PreviewParameterProvider<VpnGateSubscriptionStatus> {
    override val values = VpnGateSubscriptionStatus.entries.asSequence()
}
