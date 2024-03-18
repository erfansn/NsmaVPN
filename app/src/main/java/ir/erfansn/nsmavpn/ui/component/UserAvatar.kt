package ir.erfansn.nsmavpn.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    crossfadeDurationMillis: Int = 250,
    borderWidth: Dp = Dp.Unspecified,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    imageScale: ContentScale = ContentScale.Crop,
    avatarUrl: String? = null,
    placeholder: Painter = rememberVectorPainter(Icons.Rounded.AccountCircle),
    tintColor: Color = LocalContentColor.current,
    contentDescription: String = stringResource(id = R.string.content_description_avatar_picture),
) {
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
    ) {
        var isImageLoaded by rememberSaveable { mutableStateOf(false) }
        val animatedBorderColor by animateColorAsState(
            targetValue = borderColor.copy(alpha = if (isImageLoaded) 1f else 0f),
            animationSpec = tween(durationMillis = crossfadeDurationMillis),
            label = "borderColor"
        )

        AsyncImage(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(
                    width = borderWidth,
                    color = animatedBorderColor,
                    shape = CircleShape,
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .size(constraints.maxWidth, constraints.maxHeight)
                .crossfade(crossfadeDurationMillis)
                .build(),
            placeholder = placeholder,
            error = placeholder,
            fallback = placeholder,
            contentScale = imageScale,
            onSuccess = {
                isImageLoaded = true
            },
            colorFilter = ColorFilter.tint(tintColor).takeIf { !isImageLoaded },
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    NsmaVpnTheme {
        UserAvatar()
    }
}
