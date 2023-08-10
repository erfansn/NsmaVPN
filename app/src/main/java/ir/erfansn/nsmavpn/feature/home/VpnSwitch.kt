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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.modifier.FractionalThreshold
import ir.erfansn.nsmavpn.ui.component.modifier.rememberSwipeableStateFor
import ir.erfansn.nsmavpn.ui.component.modifier.swipeable
import ir.erfansn.nsmavpn.ui.theme.Gray
import ir.erfansn.nsmavpn.ui.theme.Green
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.theme.Red
import ir.erfansn.nsmavpn.ui.theme.Yellow
import ir.erfansn.nsmavpn.ui.util.preview.ThemePreviews
import ir.erfansn.nsmavpn.ui.util.whenever
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val prepareVpnService = rememberLauncherForActivityResult(PrepareVpnService()) { isPrepared ->
        if (isPrepared) onStateChange(VpnSwitchState.On)
    }
    val isInspectionMode = LocalInspectionMode.current
    val swipeableState = rememberSwipeableStateFor(
        value = state,
        onValueChange = {
            if (isInspectionMode) {
                onStateChange(it)
                return@rememberSwipeableStateFor
            }

            if (it == VpnSwitchState.On) {
                prepareVpnService.launch()
            } else {
                onStateChange(VpnSwitchState.Off)
            }
        }
    )
    val swipeableAnchors = mapOf(
        0f to VpnSwitchState.On,
        (VpnSwitchHeightDp.value * (1f - 0.6f)) to VpnSwitchState.Off,
    )
    Box(
        modifier = modifier
            .requiredSize(
                width = VpnSwitchWidthDp,
                height = VpnSwitchHeightDp,
            )
            .clip(CircleShape)
            .background(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            )
            .swipeable(
                enabled = enabled,
                state = swipeableState,
                anchors = swipeableAnchors,
                orientation = Orientation.Vertical,
                resistance = null,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(8.dp)
                    .offset { IntOffset(x = 0, y = swipeableState.offset.value.dp.roundToPx()) }
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .clickable(
                        enabled = enabled,
                        role = Role.Switch,
                        onClickLabel = "Start vpn"
                    ) {
                        scope.launch {
                            swipeableState.animateTo(!swipeableState.currentValue)
                        }
                    }
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(4.dp)
                            .background(
                                color = when {
                                    enabled && state == VpnSwitchState.On -> {
                                        if (connected) Green else Yellow
                                    }

                                    enabled && state == VpnSwitchState.Off -> Gray
                                    else -> Red
                                },
                                shape = CircleShape,
                            )
                            .whenever(!enabled || state == VpnSwitchState.On) {
                                drawBehind {
                                    drawIntoCanvas {
                                        val paint = Paint()

                                        val color = when {
                                            enabled && state == VpnSwitchState.On -> {
                                                if (connected) Green else Yellow
                                            }

                                            else -> Red
                                        }

                                        val frameworkPaint = paint.asFrameworkPaint()
                                        frameworkPaint.color = Color.TRANSPARENT
                                        frameworkPaint.setShadowLayer(
                                            20f,
                                            0f,
                                            0f,
                                            color
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
                        if (swipeableState.currentValue == VpnSwitchState.Off) {
                            "Start"
                        } else {
                            "Stop"
                        }
                    Text(stateIndicatorText.uppercase())
                    Icon(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = R.drawable.ic_round_power),
                        contentDescription = null,
                    )
                }
            }
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

private val VpnSwitchWidthDp = 124.dp
private val VpnSwitchHeightDp = 288.dp

@ThemePreviews
@Composable
fun VpnSwitchPreview() {
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
            modifier = Modifier.padding(16.dp),
            state = state,
            onStateChange = { state = it },
            enabled = enabled,
            connected = connected,
        )
    }
}
