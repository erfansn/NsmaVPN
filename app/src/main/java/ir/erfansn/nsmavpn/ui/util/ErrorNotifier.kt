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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DefaultErrorNotifier(
    private val context: Context,
    private val snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) : ErrorNotifier, CoroutineScope by coroutineScope {

    private var messageJob: Job? = null

    override fun showErrorMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int?,
        duration: SnackbarDuration,
        priority: MessagePriority,
        action: () -> Unit,
    ) {
        if (priority == MessagePriority.High) messageJob?.cancel()
        messageJob = launch {
            snackbarHostState.showSnackbar(
                message = context.getString(messageId),
                actionLabel = actionLabelId?.let(context::getString),
                duration = duration,
            ).takeIf {
                it == SnackbarResult.ActionPerformed
            }?.run {
                action()
            }
        }
    }
}

enum class MessagePriority { High, Low }

interface ErrorNotifier {
    fun showErrorMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int? = null,
        duration: SnackbarDuration = SnackbarDuration.Long,
        priority: MessagePriority = MessagePriority.Low,
        action: () -> Unit = { },
    )
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
