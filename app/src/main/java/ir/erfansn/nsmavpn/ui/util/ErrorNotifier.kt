package ir.erfansn.nsmavpn.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class DefaultErrorNotifier(
    private val context: Context,
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
) : ErrorNotifier {

    private lateinit var currentSnackbar: Deferred<SnackbarResult>

    override suspend fun showErrorMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int?,
        duration: SnackbarDuration,
        priority: MessagePriority,
    ): SnackbarResult {
        if (priority == MessagePriority.High) currentSnackbar.cancel()
        currentSnackbar = coroutineScope.async {
            snackbarHostState.showSnackbar(
                message = context.getString(messageId),
                actionLabel = actionLabelId?.let(context::getString),
                duration = duration,
            )
        }
        return currentSnackbar.await()
    }
}

enum class MessagePriority { High, Low }

interface ErrorNotifier {
    suspend fun showErrorMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        priority: MessagePriority = MessagePriority.Low,
    ): SnackbarResult
}

@Composable
fun rememberErrorNotifier(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): ErrorNotifier {
    val context = LocalContext.current

    return remember {
        DefaultErrorNotifier(
            context = context,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
        )
    }
}
