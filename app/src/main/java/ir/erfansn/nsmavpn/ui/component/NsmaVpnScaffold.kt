package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun NsmaVpnScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = { },
    snackbarHost: @Composable () -> Unit = { },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        snackbarHost = snackbarHost,
        contentWindowInsets = WindowInsets.safeContent,
        containerColor = Color.Transparent,
        content = content,
    )
}
