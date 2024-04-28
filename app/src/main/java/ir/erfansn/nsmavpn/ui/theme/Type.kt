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
package ir.erfansn.nsmavpn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.erfansn.nsmavpn.R

private val fontFamily = FontFamily(
    Font(R.font.baloo_bhaijaan2, FontWeight.Normal)
)

val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = fontFamily
        ),
        displayMedium = displayMedium.copy(
            fontFamily = fontFamily
        ),
        displaySmall = displaySmall.copy(
            fontFamily = fontFamily
        ),
        headlineLarge = headlineLarge.copy(
            fontFamily = fontFamily
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = fontFamily
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = fontFamily
        ),
        titleLarge = titleLarge.copy(
            fontFamily = fontFamily
        ),
        titleMedium = titleMedium.copy(
            fontFamily = fontFamily
        ),
        titleSmall = titleSmall.copy(
            fontFamily = fontFamily
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = fontFamily
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = fontFamily
        ),
        bodySmall = bodySmall.copy(
            fontFamily = fontFamily
        ),
        labelLarge = labelLarge.copy(
            fontFamily = fontFamily
        ),
        labelMedium = labelMedium.copy(
            fontFamily = fontFamily
        ),
        labelSmall = labelSmall.copy(
            fontFamily = fontFamily
        )
    )
}
