package ir.erfansn.nsmavpn.core.initializer

import android.content.Context
import androidx.startup.Initializer
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R

class SentryInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        SentryAndroid.init(context) { options ->
            options.setDsn(context.getString(R.string.sentry_dsn))
            options.setBeforeSend { event, _ ->
                if (SentryLevel.DEBUG == event.level) null else event
            }

            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true

            options.tracesSampleRate = 1.0

            options.isEnableAutoActivityLifecycleTracing = true
            options.isEnableActivityLifecycleBreadcrumbs = true

            options.environment = if (BuildConfig.DEBUG) "stage" else "production"
        }
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
