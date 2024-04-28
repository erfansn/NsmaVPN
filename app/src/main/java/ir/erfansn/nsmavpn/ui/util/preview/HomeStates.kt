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
package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class HomeStates {

    @Preview(
        group = "StoppedAndSyncing",
        name = "Light"
    )
    @Preview(
        group = "StoppedAndSyncing",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedAndSyncing

    @Preview(
        group = "StartedAndSyncing",
        name = "Light"
    )
    @Preview(
        group = "StartedAndSyncing",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedAndSyncing

    @Preview(
        group = "StartedInConnecting",
        name = "Light"
    )
    @Preview(
        group = "StartedInConnecting",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInConnecting

    @Preview(
        group = "StartedInConnected",
        name = "Light"
    )
    @Preview(
        group = "StartedInConnected",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInConnected

    @Preview(
        group = "StoppedInDisconnecting",
        name = "Light"
    )
    @Preview(
        group = "StoppedInDisconnecting",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInDisconnecting

    @Preview(
        group = "StoppedInDisconnected",
        name = "Light"
    )
    @Preview(
        group = "StoppedInDisconnected",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInDisconnected

    @Preview(
        group = "StoppedInNetworkError",
        name = "Light"
    )
    @Preview(
        group = "StoppedInNetworkError",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInNetworkError

    @Preview(
        group = "StoppedInSystemError",
        name = "Light"
    )
    @Preview(
        group = "StoppedInSystemError",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStoppedInSystemError

    @Preview(
        group = "StartedInValidating",
        name = "Light"
    )
    @Preview(
        group = "StartedInValidating",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewStartedInValidating
}
