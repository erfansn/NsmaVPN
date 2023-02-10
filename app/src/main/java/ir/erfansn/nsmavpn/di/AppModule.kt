package ir.erfansn.nsmavpn.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.work.WorkManager
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.data.source.local.DefaultUserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.DefaultVpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferencesSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import ir.erfansn.nsmavpn.data.source.remote.DefaultVpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GoogleApi
import ir.erfansn.nsmavpn.data.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * To follow DRY principle we can use interface instead of abstract class
 * and to prevent repeat `abstract fun` for binding is preferred to using `val` property
 * extension that puts [Binds] to its `get` target
 */
@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @get:Binds
    val GmailApi.bindsGoogleGmailApi: GoogleApi<Gmail>

    @get:Binds
    val DefaultVpnGateMessagesRemoteDataSource.bindsVpnGateMessagesRemoteDataSource: VpnGateMessagesRemoteDataSource

    @get:Binds
    val DefaultUserPreferencesLocalDataSource.bindsUserPreferencesLocalDataSource: UserPreferencesLocalDataSource

    @get:Binds
    val DefaultVpnProviderLocalDataSource.bindsVpnProviderLocalDataSource: VpnProviderLocalDataSource

    @get:Binds
    val DefaultVpnGateContentExtractor.bindsVpnGateContentExtractor: VpnGateContentExtractor

    @get:Binds
    val DefaultPingChecker.bindsPingChecker: PingChecker

    companion object {

        @[IoDispatcher Provides]
        fun providesIoDispatcher() = Dispatchers.IO

        @Provides
        fun providesExternalCoroutineScope(
            @IoDispatcher ioDispatcher: CoroutineDispatcher,
        ) = CoroutineScope(SupervisorJob() + ioDispatcher)

        @[Provides Singleton]
        fun providesUserPreferencesDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = UserPreferencesSerializer,
                produceFile = { context.dataStoreFile("user_preferences") }
            )

        @[Provides Singleton]
        fun providesVpnProviderDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = VpnProviderSerializer,
                produceFile = { context.dataStoreFile("vpn_provider") }
            )

        @Provides
        fun providesWorkManager(@ApplicationContext context: Context) =
            WorkManager.getInstance(context)

        @[Provides Singleton]
        fun providesGoogleAccountCredential(@ApplicationContext context: Context) =
            GoogleAccountCredential.usingOAuth2(
                context,
                listOf(GmailScopes.GMAIL_READONLY)
            )!!

        @[Provides Singleton]
        fun providesNetHttpTransport() = GoogleNetHttpTransport.newTrustedTransport()!!

        @Provides
        fun providesGsonFactory() = GsonFactory.getDefaultInstance()!!
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
