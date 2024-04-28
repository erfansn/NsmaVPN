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

annotation class SettingsStates {

    @Preview(
        group = "Api30AndEarlier",
        name = "Light",
        apiLevel = 30
    )
    @Preview(
        group = "Api30AndEarlier",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
        apiLevel = 30
    )
    annotation class PreviewApi30AndEarlier

    @Preview(
        group = "Api30Later",
        name = "Light"
    )
    @Preview(
        group = "Api30Later",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewApi30Later

    annotation class TunnelSplittingStates {

        @Preview(
            group = "EmptyAppList",
            name = "Light"
        )
        @Preview(
            group = "EmptyAppList",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewEmptyAppList

        @Preview(
            group = "Loading",
            name = "Light"
        )
        @Preview(
            group = "Loading",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewLoading

        @Preview(
            group = "AppItems",
            name = "Light"
        )
        @Preview(
            group = "AppItems",
            name = "Dark",
            uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
        )
        annotation class PreviewAppItems
    }

}