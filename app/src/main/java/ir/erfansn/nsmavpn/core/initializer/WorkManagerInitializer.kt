package ir.erfansn.nsmavpn.core.initializer

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object WorkManagerInitializer : Initializer<WorkManager> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkManagerInitializerEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override fun create(context: Context): WorkManager {
        val workManagerInitializerEntryPoint = EntryPointAccessors.fromApplication(context,
            WorkManagerInitializerEntryPoint::class.java)
        val configuration = Configuration.Builder()
            .setWorkerFactory(workManagerInitializerEntryPoint.workerFactory())
            .build()
        WorkManager.initialize(context, configuration)
        return WorkManager.getInstance(context)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
