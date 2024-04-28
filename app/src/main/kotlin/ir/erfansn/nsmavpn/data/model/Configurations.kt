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
package ir.erfansn.nsmavpn.data.model

/**
 * The reason this name was selected for this model is that it consists of anything that can
 * be changed individually by users
 *
 * [Reference](https://stackoverflow.com/questions/2074384/options-settings-properties-configuration-preferences-when-and-why)
 */
data class Configurations(
    val themeMode: ThemeMode,
    val isEnableDynamicScheme: Boolean,
    val splitTunnelingAppIds: List<String>,
) {
    enum class ThemeMode { System, Light, Dark }
}
