package ir.erfansn.nsmavpn.ui.feature.signin.onetap

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OneTapSignInWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
) : OneTapSignIn, OneTapSignOut {
    private val oneTapClient = Identity.getSignInClient(context)

    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(true)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()
    private val signUpRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .build()

    override suspend fun prepareSignInOrUpIntent() =
        runCatching {
            beginProcess(signInRequest)
        }.recoverCatching {
            beginProcess(signUpRequest)
        }.getOrThrow()

    private suspend fun beginProcess(request: BeginSignInRequest) =
        suspendCancellableCoroutine { continuation ->
            oneTapClient.beginSignIn(request).addOnSuccessListener {
                continuation.resume(it.pendingIntent)
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }

    override suspend fun getUserAccountName(data: Intent): String {
        return try {
            val credentials = oneTapClient.getSignInCredentialFromIntent(data)
            credentials.id
        } catch (e: ApiException) {
            throw when (e.statusCode) {
                CommonStatusCodes.NETWORK_ERROR -> GetAccountNameException(SignInStatus.NetworkError)
                else -> GetAccountNameException(SignInStatus.Canceled)
            }
        }
    }

    override fun signOut() {
        oneTapClient.signOut()
    }
}

interface OneTapSignIn {
    suspend fun prepareSignInOrUpIntent(): PendingIntent
    suspend fun getUserAccountName(data: Intent): String
}

interface OneTapSignOut {
    fun signOut()
}

class GetAccountNameException(val signInStatus: SignInStatus) : Exception()

sealed interface SignInStatus {
    object Canceled : SignInStatus
    object NetworkError : SignInStatus
}