package ir.erfansn.nsmavpn.ui.util.preview.parameter

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ir.erfansn.nsmavpn.feature.auth.AuthUiState
import ir.erfansn.nsmavpn.feature.auth.VpnGateSubscriptionStatus
import ir.erfansn.nsmavpn.feature.auth.google.AuthenticationStatus
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import kotlin.random.Random

class AuthScreenPreviewParameterProvider : PreviewParameterProvider<Pair<AuthUiState, GoogleAuthState>> {
    override val values
        get() = sequenceOf(
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Unknown),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.PreSignedIn),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Is),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.SignedIn),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Not),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.SignedIn),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Unknown),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.SignedIn),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Unknown),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.SignedOut),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Unknown),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.PermissionsNotGranted),
            ),
            Pair(
                AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.Unknown),
                FakeGoogleAuthState(authStatus = AuthenticationStatus.InProgress),
            ),
        )
}

class FakeGoogleAuthState(
    override val authStatus: AuthenticationStatus,
) : GoogleAuthState {
    override val currentAccount = null
    override fun getSignedInAccountData(data: Intent?) = null
    override fun obtainRequestPermissionsIntent() = Intent()
    override fun obtainSignInIntent() = Intent()
    override fun signOut() = Unit
}
