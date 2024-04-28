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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

annotation class ProfileStates {

    @Preview(
        group = "Initial",
        name = "Light"
    )
    @Preview(
        group = "Initial",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewInitial

    @Preview(
        group = "WithoutSubscription",
        name = "Light"
    )
    @Preview(
        group = "WithoutSubscription",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreviewWithoutSubscription


    @Preview(
        name = "Phone",
        group = "Device type",
        showBackground = true
    )
    @Preview(
        name = "Foldable",
        group = "Device type",
        showBackground = true,
        device = Devices.FOLDABLE
    )
    @Preview(
        name = "Tablet",
        group = "Device type",
        showBackground = true,
        device = Devices.TABLET
    )
    annotation class PreviewDeviceType
}