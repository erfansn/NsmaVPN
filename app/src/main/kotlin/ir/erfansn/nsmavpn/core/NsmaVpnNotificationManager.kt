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
package ir.erfansn.nsmavpn.core

import android.Manifest
import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager.Companion.CONNECTION_NOTIFICATION_ID
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager.Companion.ERROR_NOTIFICATION_ID
import ir.erfansn.nsmavpn.core.application.MainActivity
import ir.erfansn.nsmavpn.feature.home.vpn.ConnectionState
import ir.erfansn.nsmavpn.feature.home.vpn.CountryCode
import ir.erfansn.nsmavpn.feature.home.vpn.DefaultSstpVpnEventHandler.Companion.ACTION_DELEGATOR_VPN_DISCONNECT
import ir.erfansn.nsmavpn.ui.util.toCountryName
import javax.inject.Inject

interface NsmaVpnNotificationManager {
    fun notifyOrUpdateNotification(connectionState: ConnectionState)
    fun initialNotification(): Notification
    fun cancelErrorNotification()
    companion object {
        const val CONNECTION_NOTIFICATION_ID = 1
        const val ERROR_NOTIFICATION_ID = 2
    }
}

class DefaultNsmaVpnNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : NsmaVpnNotificationManager {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(ContextCompat.getString(context, R.string.nsmavpn_notification_channel_name))
            .setShowBadge(false)
            .build()

        notificationManager.createNotificationChannel(channel)
    }

    override fun notifyOrUpdateNotification(connectionState: ConnectionState) {
        when (connectionState) {
            is ConnectionState.Error -> {
                val notification = buildPlatformNotification(contentText = ContextCompat.getString(context, connectionState.message.id))
                notifyOrUpdateNotification(targetNotificationId = ERROR_NOTIFICATION_ID, notification = notification)
            }
            else -> {
                val notification = buildNotificationByState(connectionState.toNotificationState() ?: return)
                notifyOrUpdateNotification(targetNotificationId = CONNECTION_NOTIFICATION_ID, notification = notification)
            }
        }
    }

    override fun initialNotification() =
        buildNotificationByState(NotificationState.Connecting)

    private fun buildNotificationByState(notificationState: NotificationState): Notification {
        val action = when (notificationState) {
            NotificationState.Validating,
            NotificationState.Connecting,
            is NotificationState.Connected,
            -> {
                NotificationCompat.Action.Builder(
                    R.drawable.baseline_close_24,
                    ContextCompat.getString(context, R.string.disconnect),
                    PendingIntentCompat.getBroadcast(
                        context,
                        0,
                        Intent(ACTION_DELEGATOR_VPN_DISCONNECT),
                        PendingIntent.FLAG_ONE_SHOT,
                        false,
                    )
                ).build()
            }
            NotificationState.Disconnecting -> {
                null
            }
        }
        val contentText = when (notificationState) {
            is NotificationState.Connected -> ContextCompat.getContextForLanguage(context).getString(notificationState.messageId, notificationState.serverCountryCode.toCountryName())
            else -> ContextCompat.getString(context, notificationState.messageId)
        }

        return buildPlatformNotification(contentText, action)
    }

    private fun buildPlatformNotification(
        contentText: String,
        actions: NotificationCompat.Action? = null,
    ): Notification {
        val launchAppPendingIntent = PendingIntentCompat.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT,
            ActivityOptions.makeBasic().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
            }.toBundle(),
            false
        )

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(ContextCompat.getString(context, R.string.vpn_service_notification_title))
            setContentText(contentText)
            setContentIntent(launchAppPendingIntent)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_broken_shield)
            actions?.let(::addAction)
        }.build()
    }

    private fun notifyOrUpdateNotification(targetNotificationId: Int, notification: Notification) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(targetNotificationId, notification)
        }
    }

    override fun cancelErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "vpn_state"
    }
}

private sealed class NotificationState(@StringRes val messageId: Int) {
    data object Connecting : NotificationState(R.string.connecting)
    data class Connected(val serverCountryCode: CountryCode) : NotificationState(R.string.connected)
    data object Disconnecting : NotificationState(R.string.disconnecting)
    data object Validating : NotificationState(R.string.validating)
}

private fun ConnectionState.toNotificationState() = when(this) {
    is ConnectionState.Connected -> NotificationState.Connected(serverCountryCode)
    ConnectionState.Connecting -> NotificationState.Connecting
    ConnectionState.Disconnecting -> NotificationState.Disconnecting
    ConnectionState.Validating -> NotificationState.Validating
    ConnectionState.Disconnected,
    is ConnectionState.Error,
    -> null
}
