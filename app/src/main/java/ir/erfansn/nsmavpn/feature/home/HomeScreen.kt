@file:OptIn(ExperimentalMaterial3Api::class)

package ir.erfansn.nsmavpn.feature.home

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.get
import ir.erfansn.nsmavpn.core.hasRationaleShownPreferences
import ir.erfansn.nsmavpn.core.set
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.util.DataUsageTracker
import ir.erfansn.nsmavpn.data.util.monitor.VpnConnectionStatus
import ir.erfansn.nsmavpn.data.util.monitor.VpnNetworkMonitor
import ir.erfansn.nsmavpn.feature.home.vpn.service.SstpVpnService
import ir.erfansn.nsmavpn.ui.*
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBar
import ir.erfansn.nsmavpn.ui.component.UserAvatar
import ir.erfansn.nsmavpn.ui.component.modifier.*
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.ErrorNotifier
import ir.erfansn.nsmavpn.ui.util.rememberErrorNotifier

@Composable
fun HomeRoute(
    /*networkMonitor: NetworkMonitor,*/
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    /*val isOffline by networkMonitor.isOffline.collectAsStateWithLifecycle()*/

    HomeScreen(
        /*isOffline = isOffline,*/
        uiState = uiState,
        windowSize = windowSize,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = { },
    onNavigateToProfile: () -> Unit = { },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorNotifier = rememberErrorNotifier(snackbarHostState)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    NsmaVpnScaffold(
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        topBar = {
            NsmaVpnTopBar(
                title = {
                    val (part1, part2) = stringResource(id = R.string.app_name).split(" ")
                    Text(
                        text = buildAnnotatedString {
                            append(part1)
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) {
                                append(part2)
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToProfile) {
                        // Obtain user avatar url from UiState
                        UserAvatar()
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) {
        HomeContent(
            uiState = uiState,
            windowSize = windowSize,
            contentPadding = it,
            errorNotifier = errorNotifier,
        )
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    windowSize: WindowSizeClass,
    errorNotifier: ErrorNotifier,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 32.dp)
            .padding(contentPadding),
        constraintSet = windowSize.createConstraintSet,
    ) {
        var state by remember { mutableStateOf(VpnSwitchState.Off) }

        var connected by remember { mutableStateOf(false) }

        // Text determined by below status values
        Text(
            modifier = Modifier.layoutId("vpn_status_message"),
            // text = vpnStatusMessage,
            text = "Connected To \uD83C\uDDEF\uD83C\uDDF5",
            // text = "Disconnected",
            // text = "Connecting...",
            // text = "Collecting Servers",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        val context = LocalContext.current
        // Determined by HomeState and VpnService and VpnNetworkMonitor
        var usageStats by remember { mutableStateOf<UsageStats?>(null) }

        // Download and upload speed passed as bits count and write Ui Logic to convert it to
        // readable text info
        // Display --- when switch is off
        DataUsageStatsDisplay(
            modifier = Modifier
                .layoutId("connection_speed_display")
                .height(IntrinsicSize.Min),
            stats = usageStats,
        )

        val serviceConnectionCallback = remember {
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    service as SstpVpnService.VpnStateExposer
                    service.onEstablishment = {
                        connected = true
                        // TODO: Save vpn server to use by Tile button
                    }
                    service.onInvalid = {
                        // TODO: block vpn server then request new one
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    connected = false
                    usageStats = null
                }
            }
        }

        LaunchedEffect(uiState) {
            val vpnIntent = Intent(context, SstpVpnService::class.java)

            with(context) {
                if (uiState.vpnServer != null) {
                    bindService(vpnIntent, serviceConnectionCallback, Context.BIND_AUTO_CREATE)
                    ContextCompat.startForegroundService(this, vpnIntent
                        .setAction(SstpVpnService.ACTION_VPN_CONNECT)
                        .putExtra("server", Server.ADDRESS_FIELD_NUMBER)
                    )
                } else {
                    unbindService(serviceConnectionCallback)
                    ContextCompat.startForegroundService(this, vpnIntent
                        .setAction(SstpVpnService.ACTION_VPN_DISCONNECT)
                    )
                }
            }
        }

        VpnSwitch(
            modifier = Modifier.layoutId("vpn_switch"),
            state = state,
            onStateChange = {
                if (it == VpnSwitchState.On) {
                    /*findBestVpnServer()*/
                } else {
                    /*invalidateCurrentServer()*/
                }
                state = it
            },
            enabled = !uiState.isCollectingServers,
            connected = connected,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PostNotificationPermissionChecker(
                vpnSwitchState = state,
                errorNotifier = errorNotifier,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PostNotificationPermissionChecker(
    vpnSwitchState: VpnSwitchState,
    errorNotifier: ErrorNotifier,
) {
    var permissionCheckingRequest by remember { mutableStateOf(0) }
    val requestPostNotificationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) permissionCheckingRequest++
        }

    LaunchedEffect(vpnSwitchState) {
        if (vpnSwitchState == VpnSwitchState.On) {
            requestPostNotificationPermission.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    val context = LocalContext.current
    LaunchedEffect(permissionCheckingRequest) {
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context.findActivity(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                errorNotifier.showErrorMessage(
                    messageId = R.string.post_notification_permission_required,
                    actionLabelId = R.string.ok,
                ) {
                    context.hasRationaleShownPreferences[android.Manifest.permission.POST_NOTIFICATIONS] =
                        true
                    requestPostNotificationPermission.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }

            context.hasRationaleShownPreferences[android.Manifest.permission.POST_NOTIFICATIONS]
                    && !context.isGrantedPostNotificationPermission -> {
                errorNotifier.showErrorMessage(
                    messageId = R.string.refer_user_to_settings_for_permission_granting,
                    actionLabelId = R.string.go,
                ) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", context.packageName, null))
                        .also(context::startActivity)
                }
            }
        }
    }
}

private val Context.isGrantedPostNotificationPermission: Boolean
    get() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException()
}

data class UsageStats(
    val upload: String,
    val download: String,
)

@Composable
fun DataUsageStatsDisplay(
    modifier: Modifier = Modifier,
    stats: UsageStats? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        ConnectionMetric(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            icon = painterResource(id = R.drawable.round_upload_24),
            text = "Upload",
            value = stats?.upload ?: "--- Bit/s"
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outline
                )
        )
        ConnectionMetric(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            icon = painterResource(id = R.drawable.round_download_24),
            text = "Download",
            value = stats?.download ?: "--- Bit/s"
        )
    }
}

@Composable
fun ConnectionMetric(
    icon: Painter,
    text: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                )
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        Text(
            text = value,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

enum class VpnSwitchState { On, Off }

operator fun VpnSwitchState.not(): VpnSwitchState {
    return if (this == VpnSwitchState.On) VpnSwitchState.Off else VpnSwitchState.On
}

private val WindowSizeClass.createConstraintSet: ConstraintSet
    get() = ConstraintSet {
        val vpnStatusMessage = createRefFor("vpn_status_message")
        val connectionSpeedDisplay = createRefFor("connection_speed_display")
        val vpnSwitch = createRefFor("vpn_switch")

        val guidelineTop50 = createGuidelineFromTop(0.3f)

        constrain(vpnStatusMessage) {
            bottom.linkTo(connectionSpeedDisplay.top, margin = 28.dp)
            linkTo(
                start = parent.start,
                end = parent.end,
            )
        }
        constrain(connectionSpeedDisplay) {
            bottom.linkTo(guidelineTop50)
            linkTo(
                start = parent.start,
                end = parent.end,
            )

            val displayWidthPercent = with(this@createConstraintSet) {
                when {
                    widthSizeClass == WindowWidthSizeClass.Compact -> 0.75f
                    widthSizeClass == WindowWidthSizeClass.Medium &&
                            heightSizeClass != WindowHeightSizeClass.Compact -> 0.6f

                    else -> 0.30f
                }
            }
            width = Dimension.percent(displayWidthPercent)
        }
        constrain(vpnSwitch) {
            top.linkTo(guidelineTop50, margin = 64.dp)
            linkTo(
                start = parent.start,
                end = parent.end,
            )
        }
    }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    device = "spec:width=411dp,height=891dp", wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun HomeScreenPreview() {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                HomeScreen(
                    uiState = HomeUiState(),
                    windowSize = windowSize,
                )
            }
        }
    }
}
