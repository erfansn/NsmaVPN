@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)

package ir.erfansn.nsmavpn.feature.auth

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
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
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.DevicesWithThemePreviews
import ir.erfansn.nsmavpn.ui.util.preview.parameter.AuthScreenPreviewParameterProvider

@Composable
fun AuthRoute(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(),
    windowSize: WindowSizeClass,
    onOccurError: (stringId: Int) -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onSuccessfulSignIn = viewModel::verifyVpnGateSubscription,
        onSaveUserProfile = viewModel::saveUserProfile,
        onOccurError = onOccurError,
        onNavigateToHome = onNavigateToHome
    )
}

@Composable
private fun AuthScreen(
    modifier: Modifier = Modifier,
    uiState: AuthUiState,
    googleAuthState: GoogleAuthState = rememberGoogleAuthState(
        clientId = R.string.web_client_id,
        Scope(GmailScopes.GMAIL_READONLY),
        Scope(Scopes.PROFILE),
        Scope(Scopes.EMAIL)
    ),
    windowSize: WindowSizeClass,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit,
    onSaveUserProfile: (GoogleSignInAccount) -> Unit,
    onOccurError: (stringId: Int) -> Unit,
    onNavigateToHome: () -> Unit,
) {
    LaunchedEffect(true) {
        if (googleAuthState.authStatus == AuthenticationStatus.PreSignedIn) {
            googleAuthState.signOut()
        }
    }

    googleAuthState.currentAccount?.let {
        if (uiState.isSubscribedToVpnGate == true) {
            if (!uiState.isUserProfileStored) {
                onSaveUserProfile(it)
            } else {
                onNavigateToHome()
            }
        }
    }

    AuthContent(
        modifier = modifier,
        windowSize = windowSize,
        onOccurError = onOccurError,
        onSuccessfulSignIn = onSuccessfulSignIn,
        isSubscribedToVpnGate = uiState.isSubscribedToVpnGate,
        googleAuthState = googleAuthState,
    )
}

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    onOccurError: (stringId: Int) -> Unit,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit,
    isSubscribedToVpnGate: Boolean?,
    googleAuthState: GoogleAuthState,
) {
    Box(
        modifier = modifier
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
        val content: @Composable LayoutScope.() -> Unit = {
            AuthInsideLayoutContent(
                onOccurError = onOccurError,
                onSuccessfulSignIn = onSuccessfulSignIn,
                isSubscribedToVpnGate = isSubscribedToVpnGate,
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
                LayoutScope.Row(this).content()
            }
        } else {
            Column(
                modifier = contentModifier
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LayoutScope.Column(this).content()
            }
        }
    }
}

@Composable
fun LayoutScope.AuthInsideLayoutContent(
    onOccurError: (stringId: Int) -> Unit,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit,
    isSubscribedToVpnGate: Boolean?,
    googleAuthState: GoogleAuthState,
) {
    val commonModifier = when (this) {
        is LayoutScope.Column -> with(scope) {
            Modifier
                .weight(1.0f, false)
                .padding(horizontal = 16.dp)
        }
        is LayoutScope.Row -> with(scope) {
            Modifier
                .weight(1.0f, false)
                .padding(vertical = 16.dp)
        }
    }

    Image(
        modifier = Modifier
            .size(240.dp) then commonModifier,
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
        painter = painterResource(id = R.drawable.ic_round_key),
        contentDescription = null
    )
    Spacer(modifier = Modifier.size(24.dp))
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()) then commonModifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isNotEligibleAccountSignedIn =
            (isSubscribedToVpnGate != null && googleAuthState.authStatus == AuthenticationStatus.SignedIn && !isSubscribedToVpnGate)
        Text(
            modifier = Modifier.width(320.dp),
            text = when {
                googleAuthState.authStatus == AuthenticationStatus.PermissionsRequest -> {
                    stringResource(id = R.string.permission_rationals)
                }
                isNotEligibleAccountSignedIn -> {
                    stringResource(id = R.string.not_being_subscribed_to_vpngate)
                }
                else -> {
                    stringResource(id = R.string.auth_explanation)
                }
            },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(24.dp))
        if (isNotEligibleAccountSignedIn) {
            Button(
                onClick = googleAuthState::signOut
            ) {
                Text(text = stringResource(id = R.string.sign_out))
            }
        } else {
            GoogleAuthButtons(
                state = googleAuthState,
                onSuccess = onSuccessfulSignIn,
                onFailure = onOccurError,
            )
        }
    }
}


sealed interface LayoutScope {
    data class Column(val scope: ColumnScope) : LayoutScope
    data class Row(val scope: RowScope) : LayoutScope
}

@Composable
private fun GoogleAuthButtons(
    state: GoogleAuthState,
    onSuccess: (GoogleSignInAccount) -> Unit = { },
    onFailure: (stringId: Int) -> Unit = { },
) {
    val getAuthResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                state.getSignedInAccountData(it.data)?.let { userAccount ->
                    onSuccess(userAccount)
                }
            } catch (e: SignInIsFailedException) {
                onFailure(R.string.sign_in_failed)
            } catch (e: NoNetworkConnectionException) {
                onFailure(R.string.network_problem)
            }
        }

    when (state.authStatus) {
        AuthenticationStatus.PermissionsRequest -> {
            Button(onClick = { state.requestPermissions(getAuthResult) }) {
                Text(
                    text = stringResource(id = R.string.request)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = state::signOut
            ) {
                Text(text = stringResource(id = R.string.sign_out))
            }
        }
        else -> {
            Button(
                enabled = state.authStatus != AuthenticationStatus.InProgress,
                onClick = { state.signIn(getAuthResult) },
            ) {
                Text(
                    text = stringResource(id = R.string.sign_in)
                )
            }
        }
    }
}

@DevicesWithThemePreviews
@Composable
private fun SignInScreenPreview(
    @PreviewParameter(AuthScreenPreviewParameterProvider::class) params: Pair<AuthUiState, GoogleAuthState>,
) {
    BoxWithConstraints {
        val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                AuthScreen(
                    uiState = params.first,
                    windowSize = windowSize,
                    googleAuthState = params.second,
                    onSaveUserProfile = { },
                    onOccurError = { },
                    onSuccessfulSignIn = { },
                    onNavigateToHome = { },
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
        val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                AuthScreen(
                    uiState = AuthUiState(),
                    windowSize = windowSize,
                    onSaveUserProfile = { },
                    onOccurError = { },
                    onSuccessfulSignIn = { },
                    onNavigateToHome = { },
                )
            }
        }
    }
}
