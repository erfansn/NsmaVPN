package ir.erfansn.nsmavpn.feature.auth.google.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import ir.erfansn.nsmavpn.feature.auth.google.NoNetworkConnectionException
import ir.erfansn.nsmavpn.feature.auth.google.SignInIsFailedException

class ConnectToGoogleAccount(
    private val googleAuthState: GoogleAuthState
) : ActivityResultContract<ConnectToGoogleAccount.RequestType, ConnectToGoogleAccount.Result>() {

    override fun createIntent(context: Context, input: RequestType): Intent = when (input) {
        RequestType.RequestPermissions -> googleAuthState.obtainRequestPermissionsIntent()
        RequestType.SignIn -> googleAuthState.obtainSignInIntent()
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ) = try {
        val googleSignInAccount = googleAuthState.getSignedInAccountData(intent)

        Result.Success(googleSignInAccount)
    } catch (e: SignInIsFailedException) {
        Result.Error(R.string.sign_in_failed)
    } catch (e: NoNetworkConnectionException) {
        Result.Error(R.string.network_problem)
    }

    enum class RequestType {
        RequestPermissions, SignIn
    }

    sealed class Result {
        data class Error(@StringRes val messageId: Int) : Result()
        data class Success(val googleSignInAccount: GoogleSignInAccount?) : Result()
    }
}
