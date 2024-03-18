package ir.erfansn.nsmavpn.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

@Composable
fun whenResumed(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onState: () -> Unit,
): () -> Unit {
    return {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            onState()
        }
    }
}
