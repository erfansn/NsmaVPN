package ir.erfansn.nsmavpn.core.initializer

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent

class WorkManagerInitializer : Initializer<WorkManager> {

    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkManagerInitializerEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override fun create(context: Context): WorkManager {
        val workManagerInitializerEntryPoint =
            EarlyEntryPoints.get(context, WorkManagerInitializerEntryPoint::class.java)

        val configuration = Configuration.Builder()
            .setWorkerFactory(workManagerInitializerEntryPoint.workerFactory())
            .build()
        WorkManager.initialize(context, configuration)
        return WorkManager.getInstance(context)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
