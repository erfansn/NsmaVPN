package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.util.trace
import coil.compose.AsyncImage
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.modifier.MarqueeAnimationMode
import ir.erfansn.nsmavpn.ui.component.modifier.basicMarquee
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NsmaVpnBackground(
    modifier: Modifier = Modifier,
    onDrawn: () -> Unit = { },
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        trace("NsmaVpnBackgroundImageTrace") {
            AsyncImage(
                modifier = Modifier
                    .alpha(0.06f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                        delayMillis = 100,
                    ),
                model = R.drawable.world_map,
                contentDescription = null,
                onSuccess = { onDrawn() },
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Crop
            )
        }

        content()
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun NsmaVpnBackgroundPreview() {
    NsmaVpnTheme {
        NsmaVpnBackground {
            Box(modifier = Modifier)
        }
    }
}
