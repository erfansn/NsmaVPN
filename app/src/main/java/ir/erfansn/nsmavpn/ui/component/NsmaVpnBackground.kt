package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ir.erfansn.nsmavpn.R

@Composable
fun NsmaVpnBackground(
    state: BackgroundState,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            BackgroundState.None -> Box(modifier = Modifier)
            BackgroundState.Static -> Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.06f),
                painter = painterResource(id = R.drawable.world_map),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        content()
    }
}

enum class BackgroundState { None, Static }

@Preview
@Composable
fun NsmaVpnBackgroundPreview(
    @PreviewParameter(NsmaVpnBackgroundStatesPreview::class) state: BackgroundState
) {
    NsmaVpnBackground(state = BackgroundState.None) {
        Box(modifier = Modifier)
    }
}

class NsmaVpnBackgroundStatesPreview : PreviewParameterProvider<BackgroundState> {
    override val values: Sequence<BackgroundState>
        get() = BackgroundState.values().asSequence()
}
