package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.modifier.MarqueeAnimationMode
import ir.erfansn.nsmavpn.ui.component.modifier.basicMarquee
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemePreviews

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NsmaVpnBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopStart,
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
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

        content()
    }
}

@ThemePreviews
@Composable
private fun NsmaVpnBackgroundPreview() {
    NsmaVpnTheme {
        NsmaVpnBackground {
            Box(modifier = Modifier)
        }
    }
}
