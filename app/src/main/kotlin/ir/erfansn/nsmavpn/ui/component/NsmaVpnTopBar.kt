/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package ir.erfansn.nsmavpn.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.ui.component.NsmaVpnTopBarDefaults.animatedContainerColor
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun NsmaVpnTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    overlappedWithContent: () -> Boolean = { false },
) {
    val containerColor by animatedContainerColor(overlappedWithContent())
    CenterAlignedTopAppBar(
        modifier = modifier.drawBehind {
            drawRect(color = containerColor)
        },
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        windowInsets = windowInsets,
    )
}

@Composable
fun NsmaVpnLargeTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = NsmaVpnTopBarDefaults.containerColor,
        scrolledContainerColor = NsmaVpnTopBarDefaults.scrolledContainerColor,
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

object NsmaVpnTopBarDefaults {

    // For transform color without flicker from transparent to target color
    val containerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface.copy(0f)

    val scrolledContainerColor
        @Composable
        get() = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

    @Composable
    fun animatedContainerColor(isOverlapped: Boolean): State<Color> {
        return animateColorAsState(
            targetValue = if (isOverlapped) scrolledContainerColor else containerColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "NsmaVpnTopBarContainerColor"
        )
    }
}

private val windowInsets
    @Composable
    get() = WindowInsets.safeContent.only(
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )

@PreviewLightDark
@Composable
private fun NsmaVpnTopBarPreview() {
    NsmaVpnTheme {
        NsmaVpnTopBar(
            title = {
                Text(text = "Test")
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.round_account_circle_24),
                        contentDescription = null,
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.round_clear_24),
                        contentDescription = null,
                    )
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun NsmaVpnLargeTopBarPreview() {
    NsmaVpnTheme {
        NsmaVpnLargeTopBar(
            title = {
                Text(text = "Test")
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = null,
                    )
                }
            },
        )
    }
}
