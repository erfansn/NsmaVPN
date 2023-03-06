package ir.erfansn.nsmavpn.feature.auth.google

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes

interface GoogleAuthState {
    val authStatus: AuthenticationStatus
    val currentAccount: GoogleSignInAccount?
    fun getSignedInAccountData(data: Intent?): GoogleSignInAccount?
    fun requestPermissions(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>)
    fun signIn(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>)
    fun signOut()
}

@Stable
class DefaultGoogleAuthState(
    private val context: Context,
    @StringRes clientId: Int,
    private val scopes: List<Scope>,
    initStatus: AuthenticationStatus? = null,
) : GoogleAuthState {

    override var authStatus by mutableStateOf(AuthenticationStatus.InProgress)
        private set

    override val currentAccount get() = GoogleSignIn.getLastSignedInAccount(context)

    init {
        authStatus = when {
            initStatus != null -> {
                initStatus
            }
            currentAccount != null && currentAccount!!.allPermissionsGranted -> {
                AuthenticationStatus.PreSignedIn
            }
            currentAccount != null -> {
                AuthenticationStatus.PermissionsRequest
            }
            else -> {
                AuthenticationStatus.SignedOut
            }
        }
    }

    override fun getSignedInAccountData(data: Intent?): GoogleSignInAccount? = try {
        val result =
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        if (result.allPermissionsGranted) {
            authStatus = AuthenticationStatus.SignedIn
            result
        } else {
            authStatus = AuthenticationStatus.PermissionsRequest
            null
        }
    } catch (e: ApiException) {
        when (e.statusCode) {
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> throw SignInIsFailedException()
            CommonStatusCodes.NETWORK_ERROR -> throw NoNetworkConnectionException()
            else -> null
        }
    }

    private val GoogleSignInAccount.allPermissionsGranted
        get() = GoogleSignIn.hasPermissions(this, *scopes.toTypedArray())

    override fun requestPermissions(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        signIn(resultReceiver)
    }

    private val googleSignInOptions = GoogleSignInOptions.Builder()
        .requestIdToken(context.getString(clientId))
        .apply {
            if (scopes.isEmpty()) return@apply

            if (scopes.size == 1) {
                requestScopes(scopes.single())
            } else {
                requestScopes(scopes.first(), *scopes.drop(1).toTypedArray())
            }
        }
        .build()

    private val googleSignIn = GoogleSignIn.getClient(context, googleSignInOptions)

    override fun signIn(resultReceiver: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        resultReceiver.launch(googleSignIn.signInIntent)
    }

    override fun signOut() {
        authStatus = AuthenticationStatus.InProgress
        googleSignIn.signOut().addOnSuccessListener {
            authStatus = AuthenticationStatus.SignedOut
        }
    }
}

/**
 * State is the condition of an object at any given time, while status is a description of the
 * state. State is the actual condition of something, while status is a description of that condition.
 * For example, an object can have a state of "on" and a status of "active".
 */
enum class AuthenticationStatus { PreSignedIn, SignedIn, InProgress, SignedOut, PermissionsRequest }

class SignInIsFailedException : Throwable()
class NoNetworkConnectionException : Throwable()

@Composable
fun rememberGoogleAuthState(
    @StringRes clientId: Int,
    vararg scopes: Scope,
): GoogleAuthState {
    val context = LocalContext.current
    return rememberSaveable(
        clientId,
        saver = listSaver(
            save = {
                listOf(it.authStatus)
            },
            restore = {
                DefaultGoogleAuthState(context, clientId, scopes.toList(), it.first())
            }
        )
    ) {
        DefaultGoogleAuthState(context, clientId, scopes.toList())
    }
}
