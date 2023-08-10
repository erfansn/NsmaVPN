package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NsmaVpnTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            // For transform color without flicker from transparent to target color
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        windowInsets = WindowInsets.safeContent.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
fun NsmaVpnTopBarPreview() {
    NsmaVpnTheme {
        NsmaVpnTopBar(
            title = {
                Text(text = "Test")
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null,
                    )
                }
            }
        )
    }
}
