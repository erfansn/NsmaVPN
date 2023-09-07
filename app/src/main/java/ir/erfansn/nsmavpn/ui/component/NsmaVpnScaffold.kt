package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NsmaVpnScaffold(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    topBar: @Composable () -> Unit = { },
    snackbarHost: @Composable () -> Unit = { },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .run {
                if (scrollBehavior != null) {
                    nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    this
                }
            },
        topBar = topBar,
        snackbarHost = snackbarHost,
        contentWindowInsets = WindowInsets.safeContent,
        containerColor = Color.Transparent,
        content = content,
    )
}
