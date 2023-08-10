package ir.erfansn.nsmavpn.feature.auth.google

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope

@Stable
class DefaultGoogleAuthState(
    @StringRes clientId: Int,
    initStatus: AuthenticationStatus? = null,
    private val context: Context,
    private val scopes: List<Scope>,
) : GoogleAuthState {

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
                AuthenticationStatus.PermissionsNotGranted
            }
            else -> {
                AuthenticationStatus.SignedOut
            }
        }
    }

    override fun getSignedInAccountData(data: Intent?): GoogleSignInAccount? = try {
        val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            .getResult(ApiException::class.java)

        if (!result.allPermissionsGranted) {
            authStatus = AuthenticationStatus.PermissionsNotGranted
            null
        } else {
            authStatus = AuthenticationStatus.SignedIn
            result
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

    override fun obtainRequestPermissionsIntent() = obtainSignInIntent()

    override fun obtainSignInIntent() = googleSignIn.signInIntent

    override fun signOut() {
        authStatus = AuthenticationStatus.InProgress
        googleSignIn.signOut().addOnSuccessListener {
            authStatus = AuthenticationStatus.SignedOut
        }
    }

    companion object {
        fun Saver(
            @StringRes clientId: Int,
            context: Context,
            scopes: List<Scope>,
        ) = Saver<GoogleAuthState, AuthenticationStatus>(
            save = { it.authStatus },
            restore = { DefaultGoogleAuthState(clientId, it, context, scopes) }
        )
    }
}

interface GoogleAuthState {
    val authStatus: AuthenticationStatus
    val currentAccount: GoogleSignInAccount?
    fun getSignedInAccountData(data: Intent?): GoogleSignInAccount?
    fun obtainRequestPermissionsIntent(): Intent
    fun obtainSignInIntent(): Intent
    fun signOut()
}

/**
 * State is the condition of an object at any given time, while status is a description of the
 * state. State is the actual condition of something, while status is a description of that condition.
 * For example, an object can have a state of "on" and a status of "active".
 */
enum class AuthenticationStatus { PreSignedIn, SignedIn, InProgress, SignedOut, PermissionsNotGranted }

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
        saver = DefaultGoogleAuthState.Saver(
            clientId,
            context,
            scopes.toList(),
        )
    ) {
        DefaultGoogleAuthState(
            context = context,
            clientId = clientId,
            scopes = scopes.toList(),
        )
    }
}


