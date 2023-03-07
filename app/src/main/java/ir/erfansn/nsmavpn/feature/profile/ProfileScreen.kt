@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemeWithDevicesPreviews

@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    windowSize: WindowSizeClass,
    contentPadding: PaddingValues,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        contentPadding = contentPadding,
    )
}

@Composable
private fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    windowSize: WindowSizeClass,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (windowSize.widthSizeClass != WindowWidthSizeClass.Compact) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }
    ) {
        UserAvatar(
            modifier = Modifier.width(280.dp),
            borderWidth = 4.dp,
            avatarUrl = uiState.avatarUrl
        )
        AnimatedVisibility(
            visible = uiState.displayName.isNotEmpty() && uiState.emailAddress.isNotEmpty(),
        ) {
            UserInfoContent(
                modifier = Modifier.padding(top = 32.dp)
                    .run {
                        if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                            fillMaxWidth()
                        } else {
                            widthIn(max = 360.dp)
                        }
                    },
                displayName = uiState.displayName,
                emailAddress = uiState.emailAddress,
            )
        }
    }
}

@Composable
private fun UserAvatar(
    modifier: Modifier = Modifier,
    crossfadeDurationMillis: Int = 100,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    imageScale: ContentScale = ContentScale.Crop,
    avatarUrl: String? = null,
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
                ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
            } else {
                null
            }
        )
    }
}

@Composable
private fun UserInfoContent(
    modifier: Modifier,
    displayName: String,
    emailAddress: String,
) {
    Column(
        modifier = modifier,
    ) {
        UserInfoField(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.field_full_name),
            value = displayName,
        )
        Spacer(modifier = Modifier.height(12.dp))
        UserInfoField(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.field_email_address),
            value = emailAddress,
        )
    }
}

@Composable
private fun UserInfoField(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
) {
    OutlinedTextField(
        modifier = modifier,
        label = {
            Text(text = title)
        },
        value = value,
        onValueChange = { },
        readOnly = true,
    )
}

@ThemeWithDevicesPreviews
@Composable
private fun ProfileScreenPreview() {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                ProfileScreen(
                    uiState = ProfileUiState(
                        displayName = "Erfan Sn",
                        emailAddress = "erfansn.es@gmail.com"
                    ),
                    windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                )
            }
        }
    }
}
