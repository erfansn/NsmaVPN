@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.settings.tunnelsplitting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBar
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBarDefaults
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.TunnelSplittingPreviews
import kotlin.random.Random

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
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAppAllowingChange = viewModel::toggleAllowedApp,
        onAllAppsSplitTunnelingChange = viewModel::changeAllAppsSplitTunnelingStatus,
        onNavigateToBack = onNavigateToBack,
    )
}

@Composable
private fun TunnelSplittingScreen(
    uiState: TunnelSplittingUiState,
    windowClass: WindowSizeClass,
    onSearchQueryChange: (String) -> Unit,
    onAppAllowingChange: (AppInfo, Boolean) -> Unit,
    onAllAppsSplitTunnelingChange: (Boolean) -> Unit,
    onNavigateToBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberSaveable(uiState.searchQuery, saver = LazyGridState.Saver) {
        LazyGridState()
    }
    NsmaVpnScaffold(
        modifier = modifier,
        topBar = {
            TunnelSplittingTopBar(
                windowWidthClass = windowClass.widthSizeClass,
                query = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                overlappedWithContent = {
                    lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
                },
                onNavigateToBack = onNavigateToBack,
                onAllAppsSplitTunnelingChange = onAllAppsSplitTunnelingChange,
                showSearchBar = uiState.appItems != null,
            )
        },
    ) { contentPadding ->
        AnimatedContent(
            targetState = uiState.appItems,
            label = "apps_items",
            contentKey = { it?.isNotEmpty() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val topPaddingModifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                when {
                    it == null -> {
                        CircularProgressIndicator(
                            modifier = topPaddingModifier
                        )
                    }

                    it.isEmpty() -> {
                        Text(
                            modifier = topPaddingModifier,
                            text = stringResource(R.string.no_such_app),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    else -> {
                        val columnsCount = when (windowClass.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> 1
                            WindowWidthSizeClass.Medium -> 2
                            else -> when (windowClass.heightSizeClass) {
                                WindowHeightSizeClass.Compact -> 2
                                else -> 3
                            }
                        }
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.TopCenter),
                            contentPadding = contentPadding,
                            state = lazyGridState,
                            columns = GridCells.Fixed(columnsCount)
                        ) {
                            items(it) { item ->
                                AppItem(
                                    itemState = item,
                                    onChangeAllowed = onAppAllowingChange,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TunnelSplittingTopBar(
    windowWidthClass: WindowWidthSizeClass,
    query: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToBack: () -> Unit,
    onAllAppsSplitTunnelingChange: (allowed: Boolean) -> Unit,
    overlappedWithContent: () -> Boolean,
    showSearchBar: Boolean = true,
) {
    val containerColor by NsmaVpnTopBarDefaults.animatedContainerColor(overlappedWithContent())
    Column(
        modifier = Modifier
            .drawBehind { drawRect(color = containerColor) }
            // Prevent propagate touch event to lower layers
            .pointerInput(Unit) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NsmaVpnTopBar(
            title = {
                Text(text = stringResource(R.string.item_title_tunnel_splitting))
            },
            navigationIcon = {
                IconButton(onClick = onNavigateToBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(id = R.string.cd_back)
                    )
                }
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset((-12).dp, 8.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.allow_all)) },
                        onClick = {
                            onAllAppsSplitTunnelingChange(true)
                            expanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.disallow_all)) },
                        onClick = {
                            onAllAppsSplitTunnelingChange(false)
                            expanded = false
                        }
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.cd_more)
                    )
                }
            },
        )
        AnimatedVisibility(visible = showSearchBar) {
            OutlinedTextField(
                modifier = Modifier
                    .run {
                        when (windowWidthClass) {
                            WindowWidthSizeClass.Medium -> width(360.dp)
                            WindowWidthSizeClass.Expanded -> width(440.dp)
                            else -> fillMaxWidth(0.9f)
                        }
                    }
                    .padding(
                        horizontal = 16.dp
                    )
                    .padding(
                        top = 24.dp,
                        bottom = 16.dp,
                    )
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Horizontal)),
                value = query,
                onValueChange = onSearchQueryChange,
                shape = CircleShape,
                placeholder = {
                    Text(text = stringResource(R.string.searchbar_placeholder))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.padding(start = 4.dp),
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.cd_search),
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            modifier = Modifier.padding(end = 4.dp),
                            onClick = { onSearchQueryChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun AppItem(
    itemState: AppItemUiState,
    onChangeAllowed: (AppInfo, Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = itemState.appInfo.name)
        },
        leadingContent = {
            Image(
                modifier = Modifier.size(56.dp),
                painter = rememberDrawablePainter(drawable = itemState.appInfo.icon),
                contentDescription = null
            )
        },
        trailingContent = {
            Switch(
                checked = itemState.allowed,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.clickable {
            onChangeAllowed(itemState.appInfo, !itemState.allowed)
        }
    )
}

@TunnelSplittingPreviews.EmptyAppList
@Composable
fun SplittingTunnelScreenPreview_EmptyAppList() {
    BoxWithConstraints {
        val windowClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                TunnelSplittingScreen(
                    uiState = TunnelSplittingUiState(appItems = emptyList()),
                    onSearchQueryChange = { },
                    onAppAllowingChange = { _, _ -> },
                    onNavigateToBack = { },
                    onAllAppsSplitTunnelingChange = { },
                    windowClass = windowClass
                )
            }
        }
    }
}

@TunnelSplittingPreviews.Loading
@Composable
fun SplittingTunnelScreenPreview_Loading() {
    BoxWithConstraints {
        val windowClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                TunnelSplittingScreen(
                    uiState = TunnelSplittingUiState(appItems = null),
                    onSearchQueryChange = { },
                    onAppAllowingChange = { _, _ -> },
                    onNavigateToBack = { },
                    onAllAppsSplitTunnelingChange = { },
                    windowClass = windowClass
                )
            }
        }
    }
}

@TunnelSplittingPreviews.AppItems
@Composable
fun SplittingTunnelScreenPreview_AppItems() {
    BoxWithConstraints {
        val windowClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        NsmaVpnTheme {
            NsmaVpnBackground {
                val resource = LocalContext.current.resources
                TunnelSplittingScreen(
                    uiState = TunnelSplittingUiState(
                        appItems = buildList {
                            repeat(12) {
                                this += AppItemUiState(
                                    appInfo = AppInfo(
                                        id = "com.example.$it",
                                        name = "Example $it",
                                        icon = resource.getDrawable(
                                            R.drawable.ic_launcher_background,
                                            null
                                        ),
                                    ),
                                    allowed = Random.nextBoolean(),
                                )
                            }
                        },
                    ),
                    onSearchQueryChange = { },
                    onAppAllowingChange = { _, _ -> },
                    onNavigateToBack = { },
                    onAllAppsSplitTunnelingChange = { },
                    windowClass = windowClass
                )
            }
        }
    }
}
