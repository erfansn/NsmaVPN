package ir.erfansn.nsmavpn.feature.auth.google

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ir.erfansn.nsmavpn.core.AndroidString
import javax.inject.Inject

class FakeGoogleAuthState @Inject constructor() : GoogleAuthState {

    override var authStatus: AuthenticationStatus by mutableStateOf(AuthenticationStatus.InProgress)

    override var onErrorOccur: ((AndroidString) -> Unit)? = null

    override fun signIn() {
        authStatus = AuthenticationStatus.PermissionsNotGranted
    }

    override fun requestPermissions() {
        authStatus = AuthenticationStatus.SignedIn(GoogleSignInAccount.createDefault())
    }

    override fun signOut() {
        authStatus = AuthenticationStatus.SignedOut
    }
}
