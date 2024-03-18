package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class SettingsStates {

    @Preview(
        group = "Api30AndEarlier",
        name = "Light",
        apiLevel = 30
    )
    @Preview(
        group = "Api30AndEarlier",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
        apiLevel = 30
    )
    annotation class PreviewApi30AndEarlier

    @Preview(
        group = "Api30Later",
        name = "Light"
    )
    @Preview(
        group = "Api30Later",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewApi30Later

    annotation class TunnelSplittingStates {

        @Preview(
            group = "EmptyAppList",
            name = "Light"
        )
        @Preview(
            group = "EmptyAppList",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewEmptyAppList

        @Preview(
            group = "Loading",
            name = "Light"
        )
        @Preview(
            group = "Loading",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewLoading

        @Preview(
            group = "AppItems",
            name = "Light"
        )
        @Preview(
            group = "AppItems",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewAppItems
    }

}