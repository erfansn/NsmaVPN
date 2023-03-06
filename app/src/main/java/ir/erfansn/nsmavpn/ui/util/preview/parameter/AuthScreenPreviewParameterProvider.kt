package ir.erfansn.nsmavpn.ui.util.preview.parameter

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ir.erfansn.nsmavpn.feature.auth.AuthUiState
import ir.erfansn.nsmavpn.feature.auth.google.AuthenticationStatus
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import kotlin.random.Random

class AuthScreenPreviewParameterProvider : PreviewParameterProvider<Pair<AuthUiState, GoogleAuthState>> {
    override val values
        get() = sequenceOf(
            Pair(
                AuthUiState(
                    isSubscribedToVpnGate = true,
                    isUserProfileStored = true,
                ),
                FakeGoogleAuthState(
                    authStatus = AuthenticationStatus.SignedIn,
                ),
            ),
            Pair(
                AuthUiState(
                    isSubscribedToVpnGate = false,
                    isUserProfileStored = Random.nextBoolean(),
                ),
                FakeGoogleAuthState(
                    authStatus = AuthenticationStatus.SignedIn,
                ),
            ),
            Pair(
                AuthUiState(
                    isSubscribedToVpnGate = Random.nextBoolean(),
                    isUserProfileStored = Random.nextBoolean(),
                ),
                FakeGoogleAuthState(
                    authStatus = AuthenticationStatus.SignedOut,
                ),
            ),
            Pair(
                AuthUiState(
                    isSubscribedToVpnGate = Random.nextBoolean(),
                    isUserProfileStored = Random.nextBoolean(),
                ),
                FakeGoogleAuthState(
                    authStatus = AuthenticationStatus.PermissionsRequest,
                ),
            ),
            Pair(
                AuthUiState(
                    isSubscribedToVpnGate = Random.nextBoolean(),
                    isUserProfileStored = Random.nextBoolean(),
                ),
                FakeGoogleAuthState(
                    authStatus = AuthenticationStatus.InProgress,
                ),
            ),
        )
}

class FakeGoogleAuthState(
    override val authStatus: AuthenticationStatus,
) : GoogleAuthState {
    override val currentAccount = null
    override fun getSignedInAccountData(data: Intent?) = null
    override fun requestPermissions(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>) = Unit
    override fun signIn(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>) = Unit
    override fun signOut() = Unit
}
