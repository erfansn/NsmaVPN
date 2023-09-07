package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class TunnelSplittingPreviews {

    @Preview(
        group = "EmptyAppList",
        name = "Light"
    )
    @Preview(
        group = "EmptyAppList",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class EmptyAppList

    @Preview(
        group = "Loading",
        name = "Light"
    )
    @Preview(
        group = "Loading",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class Loading

    @Preview(
        group = "AppItems",
        name = "Light"
    )
    @Preview(
        group = "AppItems",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class AppItems
}
