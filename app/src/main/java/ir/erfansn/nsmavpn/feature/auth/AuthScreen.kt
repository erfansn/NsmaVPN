@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.auth

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import ir.erfansn.nsmavpn.feature.auth.google.AuthenticationStatus
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAccountSignInResult
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import ir.erfansn.nsmavpn.feature.auth.google.rememberGoogleAuthState
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.AuthStates
import ir.erfansn.nsmavpn.ui.util.preview.PreviewLightDarkLandscape
import ir.erfansn.nsmavpn.ui.util.preview.parameter.VpnGateSubscriptionStatusParameterProvider
import ir.erfansn.nsmavpn.ui.util.rememberUserMessageNotifier
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Composable
fun AuthRoute(
    windowSize: WindowSizeClass,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onSuccessfulSignIn = viewModel::verifyVpnGateSubscriptionAndSaveIt,
        onErrorShown = viewModel::notifyMessageShown,
        onNavigateToHome = onNavigateToHome,
    )
}

@Composable
private fun AuthScreen(
    windowSize: WindowSizeClass,
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit = { },
    onErrorShown: () -> Unit = { },
    onNavigateToHome: () -> Unit = { },
    googleAuthState: GoogleAuthState = rememberGoogleAuthState(
        clientId = stringResource(R.string.web_client_id),
        Scope(GmailScopes.GMAIL_READONLY),
        Scope(Scopes.PROFILE),
        Scope(Scopes.EMAIL),
    ),
) {
    val coroutineScope = rememberCoroutineScope()
    val userNotifier = rememberUserMessageNotifier()

    DisposableEffect(googleAuthState) {
        googleAuthState.onSignInResult = {
            when (it) {
                is GoogleAccountSignInResult.Error -> coroutineScope.launch {
                    userNotifier.showMessage(
                        messageId = it.messageId,
                        actionLabelId = R.string.ok
                    )
                }

                is GoogleAccountSignInResult.Success -> {
                    it.googleSignInAccount?.run(onSuccessfulSignIn)
                }
            }
        }
        onDispose {
            googleAuthState.onSignInResult = null
        }
    }

    LaunchedEffect(googleAuthState, uiState.errorMessage, onErrorShown) {
        if (uiState.errorMessage != null) {
            googleAuthState.signOut()
            userNotifier.showMessage(
                messageId = uiState.errorMessage,
                actionLabelId = R.string.ok
            )
            onErrorShown()
        }
    }

    NsmaVpnScaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = userNotifier.snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val contentLayoutModifier = Modifier.fillMaxSize()
            val content: @Composable LayoutType.() -> Unit = {
                AuthContent(
                    contentPadding = contentPadding,
                    subscriptionStatus = uiState.subscriptionStatus,
                    onNavigateToHome = onNavigateToHome,
                    googleAuthState = googleAuthState,
                )
            }

            if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                Row(
                    modifier = contentLayoutModifier
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LayoutType.Row(this).content()
                }
            } else {
                Column(
                    modifier = contentLayoutModifier
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
    onNavigateToHome: () -> Unit,
    googleAuthState: GoogleAuthState,
) {
    val commonModifier = if (isColumn()) with(scope) {
        Modifier
            .weight(1.0f, fill = false)
            .padding(horizontal = 16.dp)
    } else with(scope) {
        Modifier
            .weight(1.0f, fill = false)
            .padding(vertical = 16.dp)
    }

    Image(
        modifier = Modifier
            .size(240.dp)
            .then(commonModifier),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
        painter = painterResource(id = R.drawable.ic_round_key),
        contentDescription = null
    )

    Spacer(modifier = Modifier.size(24.dp))

    AnimatedContent(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .then(commonModifier),
        targetState = googleAuthState.authStatus,
        label = "auth_content"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (it) {
                AuthenticationStatus.InProgress, AuthenticationStatus.SignedOut -> {
                    DescriptionText(stringId = R.string.auth_explanation)
                    Button(
                        enabled = googleAuthState.authStatus != AuthenticationStatus.InProgress,
                        onClick = googleAuthState::signIn,
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in)
                        )
                    }
                }
                AuthenticationStatus.PermissionsNotGranted -> {
                    DescriptionText(stringId = R.string.permission_rationals)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = googleAuthState::requestPermissions,
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
                AuthenticationStatus.SignedIn -> {
                    SignedInSubContent(
                        onNavigateToHome = onNavigateToHome,
                        onSignOut = googleAuthState::signOut,
                        subscriptionStatus = subscriptionStatus,
                    )
                }
                AuthenticationStatus.PreSignedIn -> {
                    LaunchedEffect(Unit) {
                        googleAuthState.signOut()
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedInSubContent(
    onNavigateToHome: () -> Unit,
    onSignOut: () -> Unit,
    subscriptionStatus: VpnGateSubscriptionStatus,
) {
    AnimatedContent(subscriptionStatus, label = "signed-in") {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (it) {
                VpnGateSubscriptionStatus.Unknown -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(32.dp)
                    )
                }

                VpnGateSubscriptionStatus.Is -> {
                    LaunchedEffect(Unit) {
                        onNavigateToHome()
                    }
                }

                VpnGateSubscriptionStatus.IsNot -> {
                    DescriptionText(stringId = R.string.not_being_subscribed_to_vpngate)
                    Button(
                        onClick = onSignOut
                    ) {
                        Text(text = stringResource(id = R.string.sign_out))
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionText(
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

@AuthStates.PreviewPreSignedIn
@Composable
private fun AuthScreenPreview_PreSignedIn() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.PreSignedIn,
    )
}

@AuthStates.PreviewSignedOut
@Composable
private fun AuthScreenPreview_SignedOut() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@AuthStates.PreviewInProgress
@Composable
private fun AuthScreenPreview_InProgress() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.InProgress,
    )
}

@AuthStates.PreviewPermissionsNotGranted
@Composable
private fun AuthScreenPreview_PermissionsNotGranted() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.PermissionsNotGranted,
    )
}

@AuthStates.PreviewSignedIn
@Composable
private fun AuthScreenPreview_SignedIn(
    @PreviewParameter(VpnGateSubscriptionStatusParameterProvider::class) params: VpnGateSubscriptionStatus
) {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = params),
        authenticationStatus = AuthenticationStatus.SignedIn,
    )
}

@PreviewLightDarkLandscape
@Composable
private fun AuthScreenPreview_Landscape() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@Composable
private fun AuthScreenPreview(uiState: AuthUiState, authenticationStatus: AuthenticationStatus) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                AuthScreen(
                    uiState = uiState,
                    windowSize = windowSize,
                    googleAuthState = object : GoogleAuthState {
                        override val authStatus: AuthenticationStatus = authenticationStatus

                        override var onSignInResult: ((GoogleAccountSignInResult) -> Unit)? = null

                        override fun signIn() = Unit

                        override fun requestPermissions() = Unit

                        override fun signOut() = Unit
                    },
                )
            }
        }
    }
}
