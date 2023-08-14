package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    group = "Phone",
    device = "spec:parent=pixel_5,orientation=landscape",
    showBackground = true,
)
@Preview(
    group = "Phone",
    device = "spec:parent=pixel_5,orientation=landscape",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class LandscapeThemePreviews
