/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
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
    placeholder: Painter = painterResource(R.drawable.round_account_circle_24),
    tintColor: Color = LocalContentColor.current,
    contentDescription: String = stringResource(id = R.string.cd_avatar_picture),
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
