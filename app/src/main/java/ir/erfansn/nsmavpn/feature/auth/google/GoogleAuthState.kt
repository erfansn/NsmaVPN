package ir.erfansn.nsmavpn.feature.auth.google

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import io.sentry.Sentry
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.AndroidString
import kotlinx.parcelize.Parcelize

@Stable
interface GoogleAuthState {
    val authStatus: AuthenticationStatus
    var onErrorOccur: ((AndroidString) -> Unit)?
    fun signIn()
    fun requestPermissions()
    fun signOut()
}

private class MutableGoogleAuthState(
    clientId: String,
    initStatus: AuthenticationStatus = AuthenticationStatus.InProgress,
    private val context: Context,
    private val scopes: List<Scope>,
) : GoogleAuthState {

    private val googleSignInOptions = GoogleSignInOptions.Builder()
        .requestIdToken(clientId)
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

    override var authStatus by mutableStateOf(initStatus)
        private set

    init {
        updateStatus()
    }

    fun refreshSignInToken() {
        googleSignIn.silentSignIn().addOnCompleteListener {
            synchronized(this) {
                updateStatus()
            }
        }
    }

    private fun updateStatus() {
        authStatus = when {
            currentAccount?.allPermissionsGranted == true -> {
                AuthenticationStatus.SignedIn(currentAccount!!)
            }

            currentAccount != null -> {
                AuthenticationStatus.PermissionsNotGranted
            }

            else -> {
                AuthenticationStatus.SignedOut
            }
        }
    }

    override var onErrorOccur: ((AndroidString) -> Unit)? = null

    fun handleSigningToAccount(data: Intent?) {
        try {
            val result =
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)

            authStatus = if (result.allPermissionsGranted) {
                AuthenticationStatus.SignedIn(result)
            } else {
                AuthenticationStatus.PermissionsNotGranted
            }
        } catch (e: ApiException) {
            Sentry.captureException(e)

            when (e.statusCode) {
                CommonStatusCodes.NETWORK_ERROR -> onErrorOccur?.invoke(AndroidString(R.string.network_problem))
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> onErrorOccur?.invoke(AndroidString(R.string.sign_in_failed))
            }
        }
    }

    private val GoogleSignInAccount.allPermissionsGranted
        get() = GoogleSignIn.hasPermissions(this, *scopes.toTypedArray())

    val currentAccount
        get() = GoogleSignIn.getLastSignedInAccount(context)

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
            clientId: String,
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
    clientId: String,
    vararg scopes: Scope,
): GoogleAuthState {
    val context = LocalContext.current
    val googleAuthState = rememberSaveable(
        clientId,
        saver = MutableGoogleAuthState.Saver(clientId, context, scopes.toList())
    ) {
        MutableGoogleAuthState(
            context = context,
            clientId = clientId,
            scopes = scopes.toList(),
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, googleAuthState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            googleAuthState.refreshSignInToken()
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            googleAuthState.handleSigningToAccount(it.data)
        }
    DisposableEffect(googleAuthState, launcher) {
        googleAuthState.launcher = launcher
        onDispose {
            googleAuthState.launcher = null
        }
    }
    return googleAuthState
}

@Parcelize
sealed class AuthenticationStatus : Parcelable {
    data object InProgress : AuthenticationStatus()
    data object SignedOut : AuthenticationStatus()
    data object PermissionsNotGranted : AuthenticationStatus()
    data class SignedIn(val account: GoogleSignInAccount) : AuthenticationStatus()
}
