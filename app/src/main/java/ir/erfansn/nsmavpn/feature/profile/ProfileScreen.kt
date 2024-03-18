@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
import ir.erfansn.nsmavpn.ui.util.preview.ProfileStates
import kotlin.random.Random

@Composable
fun ProfileRoute(
    windowSize: WindowSizeClass,
    onNavigateToBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSubscribedToVpnGate by viewModel.isSubscribedToVpnGate.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        isSubscribedToVpnGate = isSubscribedToVpnGate,
        windowSize = windowSize,
        modifier = modifier,
        onNavigateToBack = onNavigateToBack,
        onSignOut = onSignOut,
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    isSubscribedToVpnGate: Boolean,
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
                TextButton(
                    onClick = {
                        shouldShowSignOutDialog = false
                        onSignOut()
                    }
                ) {
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

    val scrollState = rememberScrollState()
    NsmaVpnScaffold(
        modifier = modifier,
        topBar = {
            NsmaVpnTopBar(
                title = {
                    Text(text = stringResource(id = R.string.title_profile))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateToBack()
                            shouldShowSignOutDialog = false
                        }
                    ) {
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
                overlappedWithContent = { scrollState.value > 0 }
            )
        },
    ) {
        ProfileContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(it)
                .consumeWindowInsets(it),
            uiState = uiState,
            isSubscribedToVpnGate = isSubscribedToVpnGate,
            windowSize = windowSize
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    isSubscribedToVpnGate: Boolean,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (windowSize.widthSizeClass != WindowWidthSizeClass.Compact) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }
    ) {
        Box {
            UserAvatar(
                modifier = Modifier.size(240.dp),
                borderWidth = 4.dp,
                avatarUrl = uiState.avatarUrl,
                tintColor = MaterialTheme.colorScheme.onBackground,
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = !isSubscribedToVpnGate,
                label = "subscription_alert",
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                var shouldShowSubscriptionStatusAlertDialog by remember { mutableStateOf(false) }
                if (shouldShowSubscriptionStatusAlertDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            shouldShowSubscriptionStatusAlertDialog = false
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                shouldShowSubscriptionStatusAlertDialog = false
                            }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        },
                        title = {
                            Text(text = stringResource(R.string.subscription_status))
                        },
                        text = {
                            Text(text = stringResource(R.string.desc_subscription_status))
                        }
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.round_error_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.secondary)
                        .size(48.dp)
                        .clickable {
                            shouldShowSubscriptionStatusAlertDialog = true
                        },
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        UserInfoContent(
            windowSize = windowSize,
            displayName = uiState.displayName,
            emailAddress = uiState.emailAddress,
        )
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

@ProfileStates.PreviewInitial
@Composable
private fun ProfileScreenPreview_Initial() {
    ProfileScreenPreview(
        uiState = ProfileUiState(),
        isSubscribedToVpnGate = true,
    )
}

@ProfileStates.PreviewWithoutSubscription
@Composable
private fun ProfileScreenPreview_WithoutSubscription() {
    ProfileScreenPreview(
        uiState = ProfileUiState(
            avatarUrl = "https://example.com/avatar.png",
            displayName = "Erfan Sn",
            emailAddress = "erfansn.es@gmail.com",
        ),
        isSubscribedToVpnGate = false,
    )
}

@ProfileStates.PreviewDeviceType
@Composable
private fun ProfileScreenPreview_DeviceType() {
    ProfileScreenPreview(
        uiState = ProfileUiState(
            avatarUrl = "https://example.com/avatar.png",
            displayName = "Example",
            emailAddress = "example@email.com",
        ),
        isSubscribedToVpnGate = Random.nextBoolean()
    )
}

@Composable
private fun ProfileScreenPreview(
    uiState: ProfileUiState,
    isSubscribedToVpnGate: Boolean,
) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                ProfileScreen(
                    uiState = uiState,
                    isSubscribedToVpnGate = isSubscribedToVpnGate,
                    windowSize = windowSize,
                    onSignOut = { },
                    onNavigateToBack = { },
                )
            }
        }
    }
}
