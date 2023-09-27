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
import ir.erfansn.nsmavpn.data.repository.DefaultLastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.DefaultVpnGateMailRepository
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.VpnGateMailRepository
import ir.erfansn.nsmavpn.data.source.local.DefaultLastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.util.DefaultInstalledAppsListProvider
import ir.erfansn.nsmavpn.data.util.InstalledAppsListProvider
import ir.erfansn.nsmavpn.data.source.local.DefaultUserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.DefaultVpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnectionSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferencesSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import ir.erfansn.nsmavpn.data.source.remote.DefaultVpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GoogleApi
import ir.erfansn.nsmavpn.data.sync.DefaultVpnServersSyncManager
import ir.erfansn.nsmavpn.data.sync.VpnServersSyncManager
import ir.erfansn.nsmavpn.data.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    fun bindsGoogleGmailApi(gmailApi: GmailApi): GoogleApi<Gmail>

    @Binds
    fun bindsVpnGateMessagesRemoteDataSource(
        defaultVpnGateMessagesRemoteDataSource: DefaultVpnGateMessagesRemoteDataSource
    ): VpnGateMessagesRemoteDataSource

    @Binds
    fun bindsUserPreferencesLocalDataSource(
        defaultUserPreferencesLocalDataSource: DefaultUserPreferencesLocalDataSource
    ): UserPreferencesLocalDataSource

    @Binds
    fun bindsVpnProviderLocalDataSource(
        defaultVpnProviderLocalDataSource: DefaultVpnProviderLocalDataSource
    ): VpnProviderLocalDataSource

    @Binds
    fun bindsVpnGateContentExtractor(
        defaultVpnGateContentExtractor: DefaultVpnGateContentExtractor
    ): VpnGateContentExtractor

    @Binds
    fun bindsPingChecker(
        defaultPingChecker: DefaultPingChecker
    ): PingChecker

    @Binds
    fun bindsInstalledAppsListProvider(
        defaultInstalledAppsListProvider: DefaultInstalledAppsListProvider,
    ): InstalledAppsListProvider

    @Binds
    fun bindsVpnGateMailRepository(
        defaultVpnGateMailRepository: DefaultVpnGateMailRepository
    ): VpnGateMailRepository

    @Binds
    fun bindsLastVpnConnectionLocalDataSource(
        defaultLastVpnConnectionLocalDataSource: DefaultLastVpnConnectionLocalDataSource
    ): LastVpnConnectionLocalDataSource

    @Binds
    fun bindsNetworkUsageTracker(
        defaultNetworkUsageTracker: DefaultNetworkUsageTracker,
    ): NetworkUsageTracker

    @Binds
    fun bindsLastVpnConnectionRepository(
        defaultLastVpnConnectionRepository: DefaultLastVpnConnectionRepository
    ): LastVpnConnectionRepository

    @Binds
    fun bindsVpnServersSyncManager(
        defaultVpnServersSyncManager: DefaultVpnServersSyncManager
    ): VpnServersSyncManager

    companion object {

        @[IoDispatcher Provides]
        fun providesIoDispatcher() = Dispatchers.IO

        @[DefaultDispatcher Provides]
        fun providesDefaultDispatcher() = Dispatchers.Default

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

        @[Provides Singleton]
        fun providesLastVpnConnectionDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = LastVpnConnectionSerializer,
                produceFile = { context.dataStoreFile("last_vpn_connection") }
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
