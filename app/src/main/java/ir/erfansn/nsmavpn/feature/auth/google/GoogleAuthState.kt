package ir.erfansn.nsmavpn.feature.auth.google

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import ir.erfansn.nsmavpn.R

@Stable
interface GoogleAuthState {
    val authStatus: AuthenticationStatus
    var onSignInResult: ((GoogleAccountSignInResult) -> Unit)?
    fun signIn()
    fun requestPermissions()
    fun signOut()
}

class MutableGoogleAuthState(
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

    override var authStatus by mutableStateOf(initStatus ?: AuthenticationStatus.InProgress)
        private set

    private val currentAccount get() = GoogleSignIn.getLastSignedInAccount(context)

    override var onSignInResult: ((GoogleAccountSignInResult) -> Unit)? = null

    init {
        if (initStatus == null) {
            authStatus = when {
                currentAccount?.allPermissionsGranted == true -> {
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
    }

    fun handleSigningToAccount(data: Intent?) {
        try {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)

            val account = if (!result.allPermissionsGranted) {
                authStatus = AuthenticationStatus.PermissionsNotGranted
                null
            } else {
                authStatus = AuthenticationStatus.SignedIn
                result
            }
            onSignInResult?.invoke(GoogleAccountSignInResult.Success(account))
        } catch (e: ApiException) {
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> onSignInResult?.invoke(GoogleAccountSignInResult.Error(R.string.sign_in_failed))
                CommonStatusCodes.NETWORK_ERROR -> onSignInResult?.invoke(GoogleAccountSignInResult.Error(R.string.network_problem))
            }
        }
    }

    private val GoogleSignInAccount.allPermissionsGranted
        get() = GoogleSignIn.hasPermissions(this, *scopes.toTypedArray())

    var launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    override fun signIn() {
        launcher?.launch(googleSignIn.signInIntent)
    }

    override fun requestPermissions() = signIn()

    override fun signOut() {
        authStatus = AuthenticationStatus.InProgress
        googleSignIn.signOut().addOnCompleteListener {
            authStatus = AuthenticationStatus.SignedOut
        }
    }

    companion object {
        fun Saver(
            @StringRes clientId: Int,
            context: Context,
            scopes: List<Scope>,
        ) = Saver<MutableGoogleAuthState, AuthenticationStatus>(
            save = { it.authStatus },
            restore = { MutableGoogleAuthState(clientId, it, context, scopes) }
        )
    }
}

@Composable
fun rememberGoogleAuthState(
    @StringRes clientId: Int,
    vararg scopes: Scope,
): MutableGoogleAuthState {
    val context = LocalContext.current
    val googleAuthState = rememberSaveable(clientId, saver = MutableGoogleAuthState.Saver(clientId, context, scopes.toList())) {
        MutableGoogleAuthState(
            context = context,
            clientId = clientId,
            scopes = scopes.toList(),
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        googleAuthState.handleSigningToAccount(it.data)
    }
    DisposableEffect(clientId) {
        googleAuthState.launcher = launcher
        onDispose {
            googleAuthState.launcher = null
        }
    }
    return googleAuthState
}

/**
 * State is the condition of an object at any given time, while status is a description of the
 * state. State is the actual condition of something, while status is a description of that condition.
 * For example, an object can have a state of "on" and a status of "active".
 */
enum class AuthenticationStatus { PreSignedIn, SignedIn, InProgress, SignedOut, PermissionsNotGranted }

sealed class GoogleAccountSignInResult {
    data class Error(@StringRes val messageId: Int) : GoogleAccountSignInResult()
    data class Success(val googleSignInAccount: GoogleSignInAccount?) : GoogleAccountSignInResult()
}
