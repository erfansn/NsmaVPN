package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class HomeStates {

    @Preview(
        group = "StoppedAndSyncing",
        name = "Light"
    )
    @Preview(
        group = "StoppedAndSyncing",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedAndSyncing

    @Preview(
        group = "StartedAndSyncing",
        name = "Light"
    )
    @Preview(
        group = "StartedAndSyncing",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedAndSyncing

    @Preview(
        group = "StartedInConnecting",
        name = "Light"
    )
    @Preview(
        group = "StartedInConnecting",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInConnecting

    @Preview(
        group = "StartedInConnected",
        name = "Light"
    )
    @Preview(
        group = "StartedInConnected",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInConnected

    @Preview(
        group = "StoppedInDisconnecting",
        name = "Light"
    )
    @Preview(
        group = "StoppedInDisconnecting",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInDisconnecting

    @Preview(
        group = "StoppedInDisconnected",
        name = "Light"
    )
    @Preview(
        group = "StoppedInDisconnected",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInDisconnected

    @Preview(
        group = "StoppedInNetworkError",
        name = "Light"
    )
    @Preview(
        group = "StoppedInNetworkError",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInNetworkError

    @Preview(
        group = "StartedInValidating",
        name = "Light"
    )
    @Preview(
        group = "StartedInValidating",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInValidating
}
