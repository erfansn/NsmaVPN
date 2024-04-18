package ir.erfansn.nsmavpn.feature.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.modifier.FractionalThreshold
import ir.erfansn.nsmavpn.ui.component.modifier.SwipeableDefaults
import ir.erfansn.nsmavpn.ui.component.modifier.rememberSwipeableStateFor
import ir.erfansn.nsmavpn.ui.component.modifier.swipeable
import ir.erfansn.nsmavpn.ui.theme.Gray
import ir.erfansn.nsmavpn.ui.theme.Green
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.theme.Red
import ir.erfansn.nsmavpn.ui.theme.Yellow
import ir.erfansn.nsmavpn.ui.util.whenever
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnSwitch(
    state: VpnSwitchState,
    onStateChange: (VpnSwitchState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    connected: Boolean = false,
) {
    var previousEnabledValue by remember { mutableStateOf(enabled) }
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(enabled) {
        if (!previousEnabledValue && enabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousEnabledValue = enabled
    }

    val isInspectionMode = LocalInspectionMode.current
    val prepareVpnService = rememberLauncherForActivityResult(PrepareVpnService()) { isPrepared ->
        onStateChange(if (isPrepared) VpnSwitchState.On else VpnSwitchState.Off)
    }
    val swipeableState = rememberSwipeableStateFor(
        value = state,
        onValueChange = {
            if (!isInspectionMode && it == VpnSwitchState.On) {
                prepareVpnService.launch()
                return@rememberSwipeableStateFor
            }

            onStateChange(it)
        }
    )

    val density = LocalDensity.current
    val vpnSwitchAnchors = remember(density) {
        val anchorsDelta = VpnSwitchDefaults.VpnSwitchHeightDp - VpnSwitchDefaults.VpnSwitchThumbHeight
        mapOf(
            0f to VpnSwitchState.On,
            with(density) { anchorsDelta.toPx() } to VpnSwitchState.Off,
        )
    }
    Box(
        modifier = modifier
            .requiredSize(
                width = VpnSwitchDefaults.VpnSwitchWidthDp,
                height = VpnSwitchDefaults.VpnSwitchHeightDp,
            )
            .clip(CircleShape)
            .background(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            )
            .swipeable(
                enabled = enabled,
                state = swipeableState,
                anchors = vpnSwitchAnchors,
                orientation = Orientation.Vertical,
                resistance = SwipeableDefaults.resistanceConfig(
                    anchors = vpnSwitchAnchors.keys,
                    factorAtMin = VpnSwitchDefaults.StandardResistanceFactor,
                    factorAtMax = VpnSwitchDefaults.StandardResistanceFactor
                ),
                thresholds = { _, _ -> FractionalThreshold(0.5f) }
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                DownSideArrows(modifier = Modifier.rotate(180f))
                DownSideArrows()
            }

            val scope = rememberCoroutineScope()
            VpnSwitchThumb(
                enabled = enabled,
                state = state,
                connected = connected,
                yOffset = {
                    swipeableState.offset.value.roundToInt()
                },
                onClick = {
                    scope.launch { swipeableState.animateTo(!state) }
                }
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun VpnSwitchThumb(
    onClick: () -> Unit,
    yOffset: () -> Int,
    state: VpnSwitchState,
    enabled: Boolean,
    connected: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(VpnSwitchDefaults.VpnSwitchThumbHeight)
            .padding(VpnSwitchDefaults.VpnSwitchAroundPadding)
            .offset { IntOffset(x = 0, y = yOffset()) }
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            )
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                onClickLabel = "Start vpn",
                onClick = onClick,
            )
            .padding(vertical = 16.dp)
            .testTag("vpn_switch_thumb"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
            val indicatorColor = when {
                enabled && state == VpnSwitchState.On -> if (connected) Green else Yellow
                enabled && state == VpnSwitchState.Off -> if (!connected) Gray else Yellow
                else -> Red
            }
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(4.dp)
                    .background(
                        color = indicatorColor,
                        shape = CircleShape,
                    )
                    .whenever(indicatorColor != Gray) {
                        drawBehind {
                            drawIntoCanvas {
                                val paint = Paint()
                                val frameworkPaint = paint.asFrameworkPaint()
                                frameworkPaint.color = Color.TRANSPARENT
                                frameworkPaint.setShadowLayer(
                                    20f,
                                    0f,
                                    0f,
                                    indicatorColor
                                        .copy(0.5f)
                                        .toArgb(),
                                )

                                val spreetShadowDp = 2.dp.toPx()
                                it.drawRoundRect(
                                    left = -spreetShadowDp,
                                    top = -spreetShadowDp,
                                    right = size.width + spreetShadowDp,
                                    bottom = size.height + spreetShadowDp,
                                    radiusX = (size.height + spreetShadowDp) / 2f,
                                    radiusY = (size.height + spreetShadowDp) / 2f,
                                    paint = paint,
                                )
                            }
                        }
                    }
            )
            val stateIndicatorText =
                if (state == VpnSwitchState.Off) {
                    stringResource(R.string.switch_off)
                } else {
                    stringResource(R.string.switch_on)
                }
            Text(stateIndicatorText.uppercase())
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(id = R.drawable.round_power_24),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun DownSideArrows(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-20).dp)
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.alpha(0.4f),
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
        )
    }
}

object VpnSwitchDefaults {
    val VpnSwitchWidthDp = 124.dp
    val VpnSwitchHeightDp = 288.dp
    val VpnSwitchAroundPadding = 8.dp
    val VpnSwitchThumbHeight = (VpnSwitchHeightDp * 0.65f) - (VpnSwitchAroundPadding * 2)

    const val StandardResistanceFactor = 55f
}

private class PrepareVpnService : ActivityResultContract<Unit, Boolean>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return VpnService.prepare(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }

    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Boolean>? {
        return if (VpnService.prepare(context) == null) SynchronousResult(true) else null
    }
}

enum class VpnSwitchState { On, Off }

private operator fun VpnSwitchState.not(): VpnSwitchState {
    return if (this == VpnSwitchState.On) VpnSwitchState.Off else VpnSwitchState.On
}

@PreviewLightDark
@Composable
private fun VpnSwitchPreview() {
    NsmaVpnTheme {
        var state by remember { mutableStateOf(VpnSwitchState.Off) }
        var enabled by remember { mutableStateOf(false) }
        var connected by remember { mutableStateOf(false) }
        LaunchedEffect(state, enabled) {
            connected = false
            if (state == VpnSwitchState.On) {
                delay(5000)
                connected = true
            } else {
                delay(3000)
                enabled = true
            }
        }

        VpnSwitch(
            state = state,
            onStateChange = { state = it },
            enabled = enabled,
            connected = connected,
        )
    }
}
