package ir.erfansn.nsmavpn.core.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerInitializer : Initializer<WorkManager> {

    @[Provides Singleton]
    override fun create(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
