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

annotation class AuthStates {

    @Preview(
        group = "SignedOut",
        name = "Light"
    )
    @Preview(
        group = "SignedOut",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewSignedOut


    @Preview(
        group = "InProgress",
        name = "Light"
    )
    @Preview(
        group = "InProgress",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewInProgress

    @Preview(
        group = "PermissionsNotGranted",
        name = "Light"
    )
    @Preview(
        group = "PermissionsNotGranted",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewPermissionsNotGranted

    @Preview(
        group = "SignedIn",
        name = "Light"
    )
    @Preview(
        group = "SignedIn",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewSignedIn
}