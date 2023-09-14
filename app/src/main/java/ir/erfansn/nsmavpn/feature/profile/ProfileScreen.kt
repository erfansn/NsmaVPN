@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        windowSize = windowSize,
        modifier = modifier,
        onNavigateToBack = onNavigateToBack,
        onSignOut = {
            viewModel.signOutFromAccount()
            onNavigateToAuth()
        },
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    windowSize: WindowSizeClass,
    onNavigateToBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldShowSignOutDialog by remember { mutableStateOf(false) }
    if (shouldShowSignOutDialog) {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.dialog_title_sign_out))
            },
            text = {
                Text(text = stringResource(R.string.dialog_description_sign_out))
            },
            onDismissRequest = {
                shouldShowSignOutDialog = false
            },
            confirmButton = {
                TextButton(onClick = onSignOut) {
                    Text(text = stringResource(R.string.dialog_button_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { shouldShowSignOutDialog = false }) {
                    Text(text = stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }

    val topAppBarState = remember(LocalConfiguration.current.orientation) {
        TopAppBarState(
            initialHeightOffsetLimit = -Float.MAX_VALUE,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = topAppBarState
    )
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
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_to_home)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { shouldShowSignOutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = stringResource(R.string.cd_sign_out_from_account)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        scrollBehavior = scrollBehavior,
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
        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(visible = uiState.isInfoLoaded) {
            UserInfoContent(
                windowSize = windowSize,
                displayName = uiState.displayName,
                emailAddress = uiState.emailAddress,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        uiState.vpnGateSubscriptionStatus?.let {
            val subscriptionStatusMessage = if (it.isSubscribed) {
                stringResource(R.string.message_subscribed_to_vpngate)
            } else {
                stringResource(R.string.message_no_subscribe_to_vpngate)
            }
            Text(
                text = subscriptionStatusMessage,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun UserInfoContent(
    windowSize: WindowSizeClass,
    displayName: String,
    emailAddress: String,
    modifier: Modifier = Modifier,
) {
    val fieldModifier = Modifier.run {
        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> fillMaxSize()
            WindowWidthSizeClass.Medium -> width(360.dp)
            WindowWidthSizeClass.Expanded -> width(420.dp)
            else -> throw IllegalStateException("Unknown window width size class!")
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserInfoField(
            modifier = fieldModifier,
            title = stringResource(R.string.field_full_name),
            value = displayName,
        )
        UserInfoField(
            modifier = fieldModifier,
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
        singleLine = true
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
                    onSignOut = { },
                    onNavigateToBack = { },
                )
            }
        }
    }
}
