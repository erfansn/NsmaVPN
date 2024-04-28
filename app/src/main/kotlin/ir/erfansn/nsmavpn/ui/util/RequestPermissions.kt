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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.edit

@Composable
fun rememberRequestPermissionsLauncher(
    onGranted: () -> Unit,
    onRationaleShow: (permissions: List<String>) -> Unit = { },
    onPermanentlyDenied: (permissions: List<String>) -> Unit = { },
    onPartiallyGranted: (permissions: List<String>) -> Unit = { },
): ManagedActivityResultLauncher<Array<String>, *> {
    val activity = LocalContext.current.findActivity()
    val permissionsStatusDetermined = remember {
        activity.getSharedPreferences("permissions_status_determined", Context.MODE_PRIVATE)
    }

    operator fun SharedPreferences.set(keys: List<String>, value: Boolean) = edit(commit = true) {
        keys.forEach { putBoolean(it, value) }
    }

    operator fun SharedPreferences.get(keys: List<String>, default: Boolean = false) =
        keys.isNotEmpty() && keys.all { getBoolean(it, default) }

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsResult ->
        val result = permissionsResult.mapValues { (_, isGranted) -> PermissionStatus(isGranted) }
        val permissions = result.keys.toList()
        val permissionsStatus = result.values.toList()

        if (result.isNotEmpty() && permissions.size > 1 && permissionsStatus.count(PermissionStatus::isGranted) in 1..<permissions.size) {
            onPartiallyGranted(result.filterValues { it.isGranted }.map { it.key })
        }
        when {
            permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            } -> {
                permissionsStatusDetermined[permissions] = true
                onRationaleShow(permissions.filter { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) })
            }

            permissionsStatusDetermined[permissions] && permissionsStatus.any {
                !it.isGranted
            } -> {
                permissionsStatusDetermined[permissions] = true
                onPermanentlyDenied(result.filterValues { !it.isGranted }.map { it.key })
            }

            // When requests are repeatedly sent the result maybe be empty
            permissionsStatus.isNotEmpty() && permissionsStatus.all(PermissionStatus::isGranted) -> {
                permissionsStatusDetermined[permissions] = true
                onGranted()
            }
        }
    }
}

@JvmInline
value class PermissionStatus(val isGranted: Boolean)

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException()
}
