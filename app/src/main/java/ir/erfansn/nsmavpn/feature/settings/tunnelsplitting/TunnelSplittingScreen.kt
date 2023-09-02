@file:OptIn(ExperimentalMaterial3Api::class)

package ir.erfansn.nsmavpn.feature.settings.tunnelsplitting

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBar
import ir.erfansn.nsmavpn.ui.component.containerColor
import ir.erfansn.nsmavpn.ui.component.scrolledContainerColor
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun TunnelSplittingRoute(
    windowClass: WindowSizeClass,
    onNavigateToBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TunnelSplittingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TunnelSplittingScreen(
        modifier = modifier,
        uiState = uiState,
        windowClass = windowClass,
        onFilterTextChange = viewModel::updateFilter,
        onAppAllowingChange = viewModel::toggleAllowedApp,
        onAllAppsTrafficChange = viewModel::changeAllAppsTrafficUsingStatus,
        onNavigateToBack = onNavigateToBack,
    )
}

@Composable
fun TunnelSplittingScreen(
    uiState: TunnelSplittingUiState,
    windowClass: WindowSizeClass,
    onFilterTextChange: (String) -> Unit,
    onAppAllowingChange: (Boolean) -> Unit,
    onAllAppsTrafficChange: (Boolean) -> Unit,
    onNavigateToBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val packageManager = LocalContext.current.packageManager
    val currentInstalledApps = remember { mutableStateListOf<AppInfo>() }
    LaunchedEffect(packageManager) {
        currentInstalledApps.addAll(
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA).sortedBy {
                it.applicationInfo.loadLabel(packageManager).toString()
            }.map {
                it.applicationInfo
            }.map {
                AppInfo(
                    id = it.packageName,
                    name = it.loadLabel(packageManager).toString(),
                    icon = it.loadIcon(packageManager),
                )
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    NsmaVpnScaffold(
        modifier = modifier,
        topBar = {
            SplittingTunnelTopBar(
                windowWidthClass = windowClass.widthSizeClass,
                filterText = uiState.filterText,
                onFilterChanged = onFilterTextChange,
                scrollBehavior = scrollBehavior,
                onNavigateToBack = onNavigateToBack,
                onAllAppsTrafficChange = onAllAppsTrafficChange,
            )
        },
        scrollBehavior = scrollBehavior
    ) {
        val columnsCount = when (windowClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 1
            WindowWidthSizeClass.Medium -> 2
            else -> when (windowClass.heightSizeClass) {
                WindowHeightSizeClass.Compact -> 2
                else -> 3
            }
        }
        LazyVerticalGrid(
            contentPadding = it,
            columns = GridCells.Fixed(columnsCount)
        ) {
            items(uiState.appItems) { item ->
                AppItem(
                    allowed = item.allowed,
                    onChangeAllowed = onAppAllowingChange,
                    appInfo = item.appInfo
                )
            }
        }
    }
}

@Composable
fun SplittingTunnelTopBar(
    windowWidthClass: WindowWidthSizeClass,
    filterText: String,
    onFilterChanged: (String) -> Unit,
    onNavigateToBack: () -> Unit,
    onAllAppsTrafficChange: (allowed: Boolean) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val colorTransitionFraction = scrollBehavior.state.contentOffset
    val appBarContainerColor by animateColorAsState(
        targetValue = if (colorTransitionFraction < 0.0f) {
            scrolledContainerColor
        } else {
            scrollBehavior.containerColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "appBarContainerColor"
    )
    Column(
        modifier = Modifier.background(color = appBarContainerColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NsmaVpnTopBar(
            title = {
                Text(text = stringResource(R.string.title_tunnel_splitting))
            },
            navigationIcon = {
                IconButton(onClick = onNavigateToBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(id = R.string.cd_back)
                    )
                }
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Allow all") },
                        onClick = { onAllAppsTrafficChange(true) }
                    )
                    DropdownMenuItem(
                        text = { Text("Disallow all") },
                        onClick = { onAllAppsTrafficChange(false) }
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.cd_more)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            scrollBehavior = scrollBehavior,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            modifier = Modifier
                .run {
                    when (windowWidthClass) {
                        WindowWidthSizeClass.Medium -> width(360.dp)
                        WindowWidthSizeClass.Expanded -> width(440.dp)
                        else -> fillMaxWidth(0.8f)
                    }
                }
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal)),
            value = filterText,
            onValueChange = onFilterChanged,
            shape = CircleShape,
            placeholder = {
                Text(text = "App name")
            },
            leadingIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 4.dp),
                    onClick = { /*TODO*/ }
                ) {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                }
            },
            trailingIcon = {
                AnimatedVisibility(visible = filterText.isNotEmpty()) {
                    IconButton(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppItem(
    allowed: Boolean,
    onChangeAllowed: (Boolean) -> Unit,
    appInfo: AppInfo,
) {
    ListItem(
        headlineContent = {
            Text(text = appInfo.name)
        },
        leadingContent = {
            Image(
                modifier = Modifier.size(56.dp),
                painter = rememberDrawablePainter(drawable = appInfo.icon),
                contentDescription = "App icon"
            )
        },
        trailingContent = {
            Switch(
                checked = allowed,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.clickable {
            onChangeAllowed(!allowed)
        }
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PreviewScreenSizes
@Composable
fun SplittingTunnelScreenPreview() {
    BoxWithConstraints {
        val windowClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                TunnelSplittingScreen(
                    uiState = TunnelSplittingUiState(),
                    onFilterTextChange = { },
                    onAppAllowingChange = { },
                    onNavigateToBack = { },
                    onAllAppsTrafficChange = { },
                    windowClass = windowClass
                )
            }
        }
    }
}
