package ir.erfansn.nsmavpn.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    crossfadeDurationMillis: Int = 100,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    imageScale: ContentScale = ContentScale.Crop,
    avatarUrl: String? = null,
    tintColor: Color = LocalContentColor.current,
) {
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
    ) {
        var isImageLoaded by rememberSaveable { mutableStateOf(false) }

        val animatedBorderColor by animateColorAsState(
            targetValue = borderColor.let {
                if (isImageLoaded) it else it.copy(alpha = 0f)
            },
            animationSpec = tween(durationMillis = crossfadeDurationMillis)
        )

        val avatarPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .size(constraints.maxWidth, constraints.maxHeight)
                .crossfade(crossfadeDurationMillis)
                .build(),
            contentScale = imageScale,
            onState = {
                if (it is AsyncImagePainter.State.Success) {
                    isImageLoaded = true
                }
            }
        )
        Image(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(
                    width = borderWidth,
                    color = animatedBorderColor,
                    shape = CircleShape
                ),
            painter = if (isImageLoaded) {
                avatarPainter
            } else {
                rememberVectorPainter(image = Icons.Rounded.Person)
            },
            contentScale = imageScale,
            contentDescription = stringResource(R.string.content_description_avatar_picture),
            colorFilter = if (!isImageLoaded) {
                ColorFilter.tint(color = tintColor)
            } else {
                null
            }
        )
    }
}

@Preview
@Composable
fun UserAvatarPreview() {
    NsmaVpnTheme {
        UserAvatar()
    }
}
