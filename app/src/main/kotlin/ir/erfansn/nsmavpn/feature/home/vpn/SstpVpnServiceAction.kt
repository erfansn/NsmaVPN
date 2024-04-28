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
package ir.erfansn.nsmavpn.feature.home.vpn

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SstpVpnServiceAction {
    fun connect()
    fun disconnect(quietly: Boolean = false)
}

class DefaultSstpVpnServiceAction @Inject constructor(
    @ApplicationContext private val context: Context
) : SstpVpnServiceAction {

    override fun connect() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_CONNECT
            }
        )
    }

    override fun disconnect(quietly: Boolean) {
        context.startService(
            Intent(context, SstpVpnService::class.java).apply {
                action = SstpVpnService.ACTION_VPN_DISCONNECT
                putExtra(SstpVpnService.EXTRA_QUIET, quietly)
            }
        )
    }
}
