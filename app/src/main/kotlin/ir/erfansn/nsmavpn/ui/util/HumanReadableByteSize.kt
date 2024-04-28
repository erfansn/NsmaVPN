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
package ir.erfansn.nsmavpn.ui.util

import java.text.DecimalFormat

fun Long.toHumanReadableByteSizeAndUnit(
    unitsName: Array<String> = arrayOf(
        "Bytes",
        "KiB",
        "MiB",
        "GiB",
        "TiB",
        "PiB",
        "EiB",
    )
): Pair<String, String> {
    require(unitsName.size == 7) { "Count of unit names must be exactly seven" }
    require(this >= 0) { "Invalid Byte size: $this" }

    val unitIdx = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
    return DecimalFormat("#.##").format(toDouble() / (1L shl unitIdx * 10)) to unitsName[unitIdx]
}
