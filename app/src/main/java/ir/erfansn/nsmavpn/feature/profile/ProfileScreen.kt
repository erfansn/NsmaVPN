@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemeWithDevicesPreviews
import ir.erfansn.nsmavpn.ui.util.whenever

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
    contentPadding: PaddingValues = PaddingValues()
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
        val personIcon = rememberVectorPainter(image = Icons.Rounded.Person)

        AsyncImage(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .whenever(uiState.avatarUrl != null) {
                    border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                },
            placeholder = personIcon,
            fallback = personIcon,
            error = personIcon,
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiState.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.avatar_picture),
            colorFilter = if (uiState.avatarUrl == null) {
                ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
            } else {
                null
            },
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(
            visible = uiState.displayName.isNotEmpty() && uiState.emailAddress.isNotEmpty(),
        ) {
            val userInfoModifier = Modifier.width(360.dp)
            Column {
                UserInfoItem(
                    modifier = userInfoModifier,
                    title = stringResource(R.string.full_name),
                    value = uiState.displayName,
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserInfoItem(
                    modifier = userInfoModifier,
                    title = stringResource(R.string.email_address),
                    value = uiState.emailAddress,
                )
            }
        }
    }
}

@Composable
private fun UserInfoItem(
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
                        avatarUrl = "",
                        displayName = "Erfan Sn",
                        emailAddress = "erfansn.es@gmail.com"
                    ),
                    windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                )
            }
        }
    }
}
