package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = scrollBehavior.containerColor,
        scrolledContainerColor = scrolledContainerColor
    )
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = colors,
        windowInsets = windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NsmaVpnLargeTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = scrollBehavior.containerColor,
        scrolledContainerColor = scrolledContainerColor,
    )
) {
    LargeTopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        scrollBehavior = scrollBehavior,
        colors = colors,
        windowInsets = windowInsets,
    )
}

// For transform color without flicker from transparent to target color
@OptIn(ExperimentalMaterial3Api::class)
val TopAppBarScrollBehavior?.containerColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface.copy(alpha = if (this == null) 1f else 0f)
val scrolledContainerColor @Composable
    get() = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

private val windowInsets @Composable
    get() = WindowInsets.safeContent.only(
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
fun NsmaVpnLargeTopBarPreview() {
    NsmaVpnTheme {
        NsmaVpnLargeTopBar(
            title = {
                Text(text = "Test")
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
    }
}
