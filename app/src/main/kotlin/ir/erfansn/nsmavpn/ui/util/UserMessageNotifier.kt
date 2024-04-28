/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class DefaultUserMessageNotifier(
    private val context: Context,
) : UserMessageNotifier {

    override val snackbarHostState = SnackbarHostState()

    override suspend fun showMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int?,
        duration: SnackbarDuration,
        priority: UserMessagePriority,
    ): SnackbarResult {
        if (priority == UserMessagePriority.High) snackbarHostState.currentSnackbarData?.dismiss()
        return snackbarHostState.showSnackbar(
            message = context.getString(messageId),
            actionLabel = actionLabelId?.let(context::getString),
            duration = duration,
        )
    }
}

enum class UserMessagePriority { High, Low }

interface UserMessageNotifier {
    val snackbarHostState: SnackbarHostState
    suspend fun showMessage(
        @StringRes messageId: Int,
        @StringRes actionLabelId: Int? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        priority: UserMessagePriority = UserMessagePriority.Low,
    ): SnackbarResult
}

@Composable
fun rememberUserMessageNotifier(): UserMessageNotifier {
    val context = LocalContext.current
    return remember(context) {
        DefaultUserMessageNotifier(context = context)
    }
}
