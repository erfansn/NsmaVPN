package ir.erfansn.nsmavpn.feature.home

import android.app.AppOpsManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.feature.home.util.GetUsageAccess
import ir.erfansn.nsmavpn.feature.home.vpn.ConnectionState
import ir.erfansn.nsmavpn.feature.home.vpn.CountryCode
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnService
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceState
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBar
import ir.erfansn.nsmavpn.ui.component.UserAvatar
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.UserMessageNotifier
import ir.erfansn.nsmavpn.ui.util.UserMessagePriority
import ir.erfansn.nsmavpn.ui.util.preview.HomeStates
import ir.erfansn.nsmavpn.ui.util.rememberRequestPermissionsLauncher
import ir.erfansn.nsmavpn.ui.util.rememberUserMessageNotifier
import ir.erfansn.nsmavpn.ui.util.toCountryFlagEmoji
import ir.erfansn.nsmavpn.ui.util.toHumanReadableByteSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    networkMonitor: NetworkMonitor,
    windowSize: WindowSizeClass,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkUsage by viewModel.dataTraffic.collectAsStateWithLifecycle()
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(false)

    HomeScreen(
        uiState = uiState,
        windowSize = windowSize,
        modifier = modifier,
        isOnline = isOnline,
        dataTraffic = networkUsage,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToProfile = onNavigateToProfile,
        onChangeVpnServiceState = viewModel::updateVpnServiceState
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    isOnline: Boolean = false,
    dataTraffic: DataTraffic? = null,
    onNavigateToSettings: () -> Unit = { },
    onNavigateToProfile: () -> Unit = { },
    onChangeVpnServiceState: (SstpVpnServiceState) -> Unit = { },
) {
    val userNotifier = rememberUserMessageNotifier()
    val scrollState = rememberScrollState()

    NsmaVpnScaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(userNotifier.snackbarHostState)
        },
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
                        UserAvatar(
                            avatarUrl = uiState.userAvatarUrl,
                            borderWidth = 0.5.dp,
                            contentDescription = stringResource(R.string.cd_profile)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.cd_settings),
                        )
                    }
                },
                overlappedWithContent = {
                    scrollState.value > 0f
                }
            )
        }
    ) {
        HomeContent(
            uiState = uiState,
            isOnline = isOnline,
            dataTraffic = dataTraffic,
            windowSize = windowSize,
            userMessageNotifier = userNotifier,
            onChangeVpnServiceState = onChangeVpnServiceState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 32.dp)
                .padding(it),
        )
    }
}

@Composable
private fun HomeContent(
    isOnline: Boolean,
    uiState: HomeUiState,
    dataTraffic: DataTraffic?,
    windowSize: WindowSizeClass,
    userMessageNotifier: UserMessageNotifier,
    modifier: Modifier = Modifier,
    onChangeVpnServiceState: (SstpVpnServiceState) -> Unit,
) {
    ConstraintLayout(
        modifier = modifier,
        constraintSet = windowSize.createConstraintSet,
    ) {
        var vpnSwitchState by remember(uiState.vpnServiceState.started, isOnline) {
            mutableStateOf(
                if (uiState.vpnServiceState.started && isOnline) {
                    VpnSwitchState.On
                } else {
                    VpnSwitchState.Off
                }
            )
        }

        CurrentStateText(
            modifier = Modifier.layoutId("current_state_text"),
            vpnSwitchState = vpnSwitchState,
            isSyncing = uiState.isSyncing,
            connectionState = uiState.vpnServiceState.state,
        )
        DataTrafficDisplay(
            modifier = Modifier
                .layoutId("data_traffic_display")
                .height(IntrinsicSize.Min),
            stats = if (vpnSwitchState == VpnSwitchState.Off) null else dataTraffic
        )

        val context = LocalContext.current
        LaunchedEffect(context, isOnline) {
            if (!isOnline) {
                userMessageNotifier.showMessage(
                    messageId = R.string.network_problem,
                    duration = SnackbarDuration.Indefinite,
                    priority = UserMessagePriority.High,
                )
            }
        }

        val scope = rememberCoroutineScope()
        val serviceConnectionCallback = remember(scope, onChangeVpnServiceState) {
            object : ServiceConnection {
                var vpnStartedStateCollectorJob: Job? = null

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    if (service !is SstpVpnService.LocalBinder) return

                    vpnStartedStateCollectorJob = service
                        .sstpVpnServiceState
                        .onEach(onChangeVpnServiceState)
                        .launchIn(scope)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    vpnStartedStateCollectorJob?.cancel()
                }
            }
        }
        DisposableEffect(serviceConnectionCallback, context) {
            context.bindService(
                Intent(context, SstpVpnService::class.java),
                serviceConnectionCallback,
                Service.BIND_AUTO_CREATE
            )
            onDispose {
                context.unbindService(serviceConnectionCallback)
            }
        }

        VpnSwitch(
            modifier = Modifier.layoutId("vpn_switch"),
            state = vpnSwitchState,
            onStateChange = {
                if (!isOnline) return@VpnSwitch

                when (it) {
                    VpnSwitchState.On -> {
                        if (!context.isGrantedGetUsageStatsPermission) {
                            scope.launch {
                                userMessageNotifier.showMessage(
                                    messageId = R.string.usage_access_permission,
                                    duration = SnackbarDuration.Long,
                                    actionLabelId = R.string.ok,
                                ).also { result ->
                                    if (result == SnackbarResult.ActionPerformed) {
                                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    }
                                }
                            }
                        }

                        ContextCompat.startForegroundService(
                            context,
                            Intent(context, SstpVpnService::class.java).apply {
                                action = SstpVpnService.ACTION_VPN_CONNECT
                            }
                        )
                    }

                    VpnSwitchState.Off -> {
                        context.startService(
                            Intent(context, SstpVpnService::class.java).apply {
                                action = SstpVpnService.ACTION_VPN_DISCONNECT
                            }
                        )
                    }
                }
                vpnSwitchState = it
            },
            enabled = !uiState.isSyncing || vpnSwitchState == VpnSwitchState.On,
            connected = uiState.vpnServiceState.state is ConnectionState.Connected,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PostNotificationPermissionEffect(
                userMessageNotifier = userMessageNotifier
            )
        }
    }
}

@Composable
private fun CurrentStateText(
    connectionState: ConnectionState,
    vpnSwitchState: VpnSwitchState,
    isSyncing: Boolean,
    modifier: Modifier = Modifier,
) {
    val vpnStateMessage = when {
        vpnSwitchState == VpnSwitchState.Off && isSyncing -> {
            stringResource(R.string.collecting_servers)
        }

        connectionState is ConnectionState.Connected -> {
            stringResource(connectionState.messageId, connectionState.serverCountryCode.toCountryFlagEmoji())
        }

        else -> {
            stringResource(connectionState.messageId)
        }
    }

    Text(
        modifier = modifier,
        text = vpnStateMessage,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun PostNotificationPermissionEffect(
    userMessageNotifier: UserMessageNotifier
) {
    var trigger by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val permissionsRequest = rememberRequestPermissionsLauncher(
        onGranted = {
            Log.i("HomeScreen", "Notification permission has granted")
        },
        onRationaleShow = {
            scope.launch {
                userMessageNotifier.showMessage(
                    messageId = R.string.notification_permission_rationale,
                    actionLabelId = R.string.ok
                ).also {
                    if (it == SnackbarResult.ActionPerformed) {
                        trigger = !trigger
                    }
                }
            }
        },
        onPermanentlyDenied = {
            scope.launch {
                userMessageNotifier.showMessage(
                    messageId = R.string.notification_permission_denied,
                )
            }
        },
    )

    val context = LocalContext.current
    LaunchedEffect(trigger) {
        if (!context.isGrantedPostNotificationPermission) {
            permissionsRequest.launch(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
            )
        }
    }
}

private val Context.isGrantedPostNotificationPermission: Boolean
    @RequiresApi(Build.VERSION_CODES.TIRAMISU) get() =
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

@Composable
private fun DataTrafficDisplay(
    modifier: Modifier = Modifier,
    stats: DataTraffic? = null,
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
            text = stringResource(R.string.upload),
            value = stats?.upload.toHumanReadableByteSize()
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
            text = stringResource(R.string.download),
            value = stats?.download.toHumanReadableByteSize()
        )
    }
}

@Composable
private fun ConnectionMetric(
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

private val WindowSizeClass.createConstraintSet: ConstraintSet
    get() = ConstraintSet {
        val vpnStateMessage = createRefFor("current_state_text")
        val connectionSpeedDisplay = createRefFor("data_traffic_display")
        val vpnSwitch = createRefFor("vpn_switch")

        val guidelineTop50 = createGuidelineFromTop(0.3f)

        constrain(vpnStateMessage) {
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


@HomeStates.PreviewStoppedAndSyncing
@Composable
private fun HomeScreenPreview_StoppedAndSyncing() {
    HomeScreenPreview(
        uiState = HomeUiState(
            isSyncing = true,
            vpnServiceState = VpnServiceState(
                started = false,
            )
        ),
    )
}

@HomeStates.PreviewStartedAndSyncing
@Composable
private fun HomeScreenPreview_StartedAndSyncing() {
    HomeScreenPreview(
        uiState = HomeUiState(
            isSyncing = true,
            vpnServiceState = VpnServiceState(
                started = true,
                state = ConnectionState.Connecting
            )
        ),
    )
}

@HomeStates.PreviewStartedInConnecting
@Composable
private fun HomeScreenPreview_StartedInConnecting() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = true,
                state = ConnectionState.Connecting,
            ),
        ),
    )
}

@HomeStates.PreviewStartedInConnected
@Composable
private fun HomeScreenPreview_StartedInConnected() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = true,
                state = ConnectionState.Connected(CountryCode("IR")),
            ),
        ),
    )
}

@HomeStates.PreviewStoppedInDisconnecting
@Composable
private fun HomeScreenPreview_StoppedInDisconnecting() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = false,
                state = ConnectionState.Disconnecting,
            ),
        ),
    )
}

@HomeStates.PreviewStoppedInDisconnected
@Composable
private fun HomeScreenPreview_StoppedInDisconnected() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = false,
                state = ConnectionState.Disconnected,
            ),
        ),
    )
}

@HomeStates.PreviewStoppedInNetworkError
@Composable
private fun HomeScreenPreview_StoppedInNetworkError() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = false,
                state = ConnectionState.NetworkError,
            ),
        ),
    )
}

@HomeStates.PreviewStartedInValidating
@Composable
private fun HomeScreenPreview_StartedInValidating() {
    HomeScreenPreview(
        uiState = HomeUiState(
            vpnServiceState = VpnServiceState(
                started = true,
                state = ConnectionState.Validating,
            ),
        )
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                HomeScreen(
                    uiState = uiState,
                    windowSize = windowSize,
                )
            }
        }
    }
}
