package ir.erfansn.nsmavpn.feature.signin.google

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes

@Stable
class GoogleSignInState(
    private val context: Context,
    clientId: String,
) {
    var signInState by mutableStateOf<SignInState>(SignInState.SignOut)
        private set

    init {
        signInState = if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            SignInState.SignedIn
        } else {
            SignInState.SignOut
        }
    }

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestScopes(Scope(GmailScopes.GMAIL_READONLY))
        .requestEmail()
        .build()

    private val googleSignIn = GoogleSignIn.getClient(context, googleSignInOptions)

    fun prepareSignInIntent() =
        googleSignIn.signInIntent

    fun getSignedInAccountData(data: Intent?): GoogleSignInAccount = try {
        GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java).also {
            signInState = SignInState.SignedIn
        }
    } catch (e: ApiException) {
        throw when (e.statusCode) {
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> SignInFailedException()
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> SignInCancelledException()
            CommonStatusCodes.NETWORK_ERROR -> NoNetworkConnectionException()
            else -> e.also { it.printStackTrace() }
        }
    }

    fun isPermissionsGranted(): Boolean {
        val currentAccount = GoogleSignIn.getLastSignedInAccount(context)
        return currentAccount?.let {
            GoogleSignIn.hasPermissions(it, Scope(GmailScopes.GMAIL_READONLY))
        } ?: false
    }

    fun signOut() {
        signInState = SignInState.InProgress
        googleSignIn.signOut().addOnSuccessListener {
            signInState = SignInState.SignOut
        }
    }

    fun signInIsIncomplete() = signInState == SignInState.SignedIn && !isPermissionsGranted()
}

sealed interface SignInState {
    object SignedIn : SignInState
    object InProgress : SignInState
    object SignOut : SignInState
}

class SignInFailedException : Exception()
class NoNetworkConnectionException : Exception()
class SignInCancelledException : Exception()

@Composable
fun rememberGoogleSignInState(
    clientId: String
): GoogleSignInState {
    val context = LocalContext.current
    return remember { GoogleSignInState(context, clientId) }
}
