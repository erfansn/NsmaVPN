package ir.erfansn.nsmavpn.ui.util.preview.parameter

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import ir.erfansn.nsmavpn.feature.auth.VpnGateSubscriptionStatus

class VpnGateSubscriptionStatusParameterProvider :
    CollectionPreviewParameterProvider<VpnGateSubscriptionStatus>(
        collection = VpnGateSubscriptionStatus.entries
    )