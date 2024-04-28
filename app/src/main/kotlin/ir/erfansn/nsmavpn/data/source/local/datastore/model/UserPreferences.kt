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
package ir.erfansn.nsmavpn.data.source.local.datastore.model

import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.source.local.datastore.ThemeModeProto
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences

fun UserPreferences.toConfigurations() = Configurations(
    themeMode = when (themeModeProto) {
        null,
        ThemeModeProto.UNRECOGNIZED,
        ThemeModeProto.SYSTEM -> Configurations.ThemeMode.System
        ThemeModeProto.LIGHT -> Configurations.ThemeMode.Light
        ThemeModeProto.DARK -> Configurations.ThemeMode.Dark
    },
    isEnableDynamicScheme = enableDynamicScheme,
    splitTunnelingAppIds = splitTunnelingAppIdList
)
