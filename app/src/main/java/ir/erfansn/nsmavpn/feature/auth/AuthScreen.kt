@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)

package ir.erfansn.nsmavpn.feature.auth

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.feature.auth.google.*
import ir.erfansn.nsmavpn.feature.auth.google.contract.*
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.DevicesWithThemePreviews
import ir.erfansn.nsmavpn.ui.util.preview.parameter.AuthScreenPreviewParameterProvider
import ir.erfansn.nsmavpn.ui.util.rememberErrorNotifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Composable
fun AuthRoute(
    windowSize: WindowSizeClass,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onSuccessfulSignIn = viewModel::verifyVpnGateSubscriptionAndSaveIt,
        onNavigateToHome = onNavigateToHome,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScreen(
    windowSize: WindowSizeClass,
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit = { },
    onNavigateToHome: () -> Unit = { },
    googleAuthState: GoogleAuthState = rememberGoogleAuthState(
        clientId = R.string.web_client_id,
        Scope(GmailScopes.GMAIL_READONLY),
        Scope(Scopes.PROFILE),
        Scope(Scopes.EMAIL)
    ),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorNotifier = rememberErrorNotifier(snackbarHostState)

    NsmaVpnScaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val contentModifier = when {
                windowSize.widthSizeClass == WindowWidthSizeClass.Compact &&
                        windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
                    Modifier.fillMaxSize()
                }
                windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
                    Modifier.aspectRatio(10 / 7f)
                }
                else -> {
                    Modifier.aspectRatio(7 / 10f)
                }
            }
            val content: @Composable LayoutType.() -> Unit = {
                AuthContent(
                    contentPadding = contentPadding,
                    subscriptionStatus = uiState.subscriptionStatus,
                    onOccurError = errorNotifier::showErrorMessage,
                    onSuccessfulSignIn = onSuccessfulSignIn,
                    onNavigateToHome = onNavigateToHome,
                    googleAuthState = googleAuthState,
                )
            }

            if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                Row(
                    modifier = contentModifier
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LayoutType.Row(this).content()
                }
            } else {
                Column(
                    modifier = contentModifier
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LayoutType.Column(this).content()
                }
            }
        }
    }
}

@Composable
private fun LayoutType.AuthContent(
    contentPadding: PaddingValues,
    subscriptionStatus: VpnGateSubscriptionStatus,
    onOccurError: (stringId: Int) -> Unit,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit,
    onNavigateToHome: () -> Unit,
    googleAuthState: GoogleAuthState,
) {
    val connectToGoogleAccount =
        rememberLauncherForActivityResult(ConnectToGoogleAccount(googleAuthState)) {
            when (it) {
                is ConnectToGoogleAccount.Result.Error -> onOccurError(it.messageId)
                is ConnectToGoogleAccount.Result.Success -> it.googleSignInAccount?.run(
                    onSuccessfulSignIn
                )
            }
        }

    val commonModifier = if (isColumn()) with(scope) {
        Modifier
            .weight(1.0f, false)
            .padding(horizontal = 16.dp)
    } else with(scope) {
        Modifier
            .weight(1.0f, false)
            .padding(vertical = 16.dp)
    }

    Image(
        modifier = Modifier
            .size(240.dp) then commonModifier,
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
        painter = painterResource(id = R.drawable.ic_round_key),
        contentDescription = null
    )

    Spacer(modifier = Modifier.size(24.dp))

    AnimatedContent(googleAuthState.authStatus) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .then(commonModifier),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (it) {
                AuthenticationStatus.InProgress -> {
                    AuthDescriptionText(stringId = R.string.auth_explanation)
                    Button(
                        enabled = googleAuthState.authStatus != AuthenticationStatus.InProgress,
                        onClick = {
                            connectToGoogleAccount.launch(ConnectToGoogleAccount.RequestType.SignIn)
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in)
                        )
                    }
                }
                AuthenticationStatus.SignedOut -> {
                    AuthDescriptionText(stringId = R.string.auth_explanation)
                    Button(
                        onClick = {
                            connectToGoogleAccount.launch(ConnectToGoogleAccount.RequestType.SignIn)
                        }
                    ) {
                        Text(text = stringResource(id = R.string.sign_in))
                    }
                }
                AuthenticationStatus.PermissionsNotGranted -> {
                    AuthDescriptionText(stringId = R.string.permission_rationals)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                connectToGoogleAccount.launch(ConnectToGoogleAccount.RequestType.RequestPermissions)
                            },
                        ) {
                            Text(
                                text = stringResource(id = R.string.request)
                            )
                        }
                        Button(
                            onClick = googleAuthState::signOut
                        ) {
                            Text(text = stringResource(id = R.string.sign_out))
                        }
                    }
                }
                AuthenticationStatus.SignedIn -> when (subscriptionStatus) {
                    VpnGateSubscriptionStatus.Unknown -> {
                        CircularProgressIndicator()
                    }
                    VpnGateSubscriptionStatus.Is -> {
                        onNavigateToHome()
                    }
                    VpnGateSubscriptionStatus.Not -> {
                        AuthDescriptionText(stringId = R.string.not_being_subscribed_to_vpngate)
                        Button(
                            onClick = googleAuthState::signOut
                        ) {
                            Text(text = stringResource(id = R.string.sign_out))
                        }
                    }
                }
                AuthenticationStatus.PreSignedIn -> {
                    googleAuthState.signOut()
                }
            }
        }
    }
}

@Composable
private fun AuthDescriptionText(
    @StringRes stringId: Int,
) {
    Text(
        modifier = Modifier.widthIn(max = 320.dp),
        text = stringResource(id = stringId),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private sealed interface LayoutType {
    data class Column(val scope: ColumnScope) : LayoutType
    data class Row(val scope: RowScope) : LayoutType
}

@OptIn(ExperimentalContracts::class)
private fun LayoutType.isColumn(): Boolean {
    contract {
        returns(true) implies (this@isColumn is LayoutType.Column)
        returns(false) implies (this@isColumn is LayoutType.Row)
    }
    return this is LayoutType.Column
}

@DevicesWithThemePreviews
@Composable
private fun SignInScreenPreview(
    @PreviewParameter(AuthScreenPreviewParameterProvider::class) params: Pair<AuthUiState, GoogleAuthState>,
) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                AuthScreen(
                    uiState = params.first,
                    windowSize = windowSize,
                    googleAuthState = params.second,
                )
            }
        }
    }
}

@Preview(
    group = "Phone",
    device = "spec:parent=pixel_5,orientation=landscape",
    showBackground = true,
)
@Preview(
    group = "Phone",
    device = "spec:parent=pixel_5,orientation=landscape",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun SignInScreenPhoneLandscapePreview() {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                AuthScreen(
                    uiState = AuthUiState(),
                    windowSize = windowSize,
                )
            }
        }
    }
}
