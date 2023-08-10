@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBar
import ir.erfansn.nsmavpn.ui.component.UserAvatar
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemeWithDevicesPreviews

@Composable
fun ProfileRoute(
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = { },
    onNavigateToBack: () -> Unit = { },
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        windowSize = windowSize,
        modifier = modifier,
        onNavigateToBack = onNavigateToBack,
        onSignOut = onSignOut,
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    onNavigateToBack: () -> Unit = { },
    onSignOut: () -> Unit = { },
) {
    NsmaVpnScaffold(
        modifier = modifier,
        topBar = {
            NsmaVpnTopBar(
                title = {
                    Text(text = stringResource(id = R.string.title_profile))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back to home"
                        )
                    }
                },
                actions = {
                    // TODO: first open confirmation dialog then invoke onSignOut
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = "Sign out"
                        )
                    }
                },
                scrollBehavior = it,
            )
        },
    ) {
        ProfileContent(
            uiState = uiState,
            contentPadding = it,
            windowSize = windowSize
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    contentPadding: PaddingValues,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
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
            modifier = Modifier.size(240.dp),
            borderWidth = 4.dp,
            avatarUrl = uiState.avatarUrl,
            tintColor = MaterialTheme.colorScheme.onBackground,
        )
        AnimatedVisibility(visible = uiState.isInfoLoaded) {
            UserInfoContent(
                modifier = Modifier.padding(top = 32.dp),
                displayName = uiState.displayName,
                emailAddress = uiState.emailAddress,
            )
        }
    }
}

@Composable
private fun UserInfoContent(
    displayName: String,
    emailAddress: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserInfoField(
            title = stringResource(R.string.field_full_name),
            value = displayName,
        )
        UserInfoField(
            title = stringResource(R.string.field_email_address),
            value = emailAddress,
        )
    }
}

@Composable
private fun UserInfoField(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = title) },
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
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                ProfileScreen(
                    uiState = ProfileUiState(
                        displayName = "Erfan Sn",
                        emailAddress = "erfansn.es@gmail.com"
                    ),
                    windowSize = windowSize,
                )
            }
        }
    }
}
