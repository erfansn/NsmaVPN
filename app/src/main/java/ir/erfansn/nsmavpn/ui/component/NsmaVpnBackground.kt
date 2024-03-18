package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.modifier.MarqueeAnimationMode
import ir.erfansn.nsmavpn.ui.component.modifier.basicMarquee
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NsmaVpnBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.06f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    delayMillis = 0,
                ),
            painter = painterResource(id = R.drawable.world_map),
            colorFilter = ColorFilter.tint(color = LocalContentColor.current),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

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
