package ir.erfansn.nsmavpn.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.work.WorkManager
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.core.DefaultNsmaVpnNotificationManager
import ir.erfansn.nsmavpn.core.NsmaVpnNotificationManager
import ir.erfansn.nsmavpn.data.repository.ConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.DefaultConfigurationsRepository
import ir.erfansn.nsmavpn.data.repository.DefaultLastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.DefaultUserProfileRepository
import ir.erfansn.nsmavpn.data.repository.DefaultVpnGateMailRepository
import ir.erfansn.nsmavpn.data.repository.DefaultVpnServersRepository
import ir.erfansn.nsmavpn.data.repository.LastVpnConnectionRepository
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.data.repository.VpnGateMailRepository
import ir.erfansn.nsmavpn.data.repository.VpnServersRepository
import ir.erfansn.nsmavpn.data.source.local.DefaultLastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.DefaultUserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.DefaultUserProfileLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.DefaultVpnServersLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.LastVpnConnectionLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.UserProfileLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnServersLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnection
import ir.erfansn.nsmavpn.data.source.local.datastore.LastVpnConnectionSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferencesSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfile
import ir.erfansn.nsmavpn.data.source.local.datastore.UserProfileSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServers
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnServersSerializer
import ir.erfansn.nsmavpn.data.source.remote.DefaultVpnGateMailMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMailMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GoogleApi
import ir.erfansn.nsmavpn.data.util.ConnectivityNetworkMonitor
import ir.erfansn.nsmavpn.data.util.DefaultInstalledAppsListProvider
import ir.erfansn.nsmavpn.data.util.DefaultNetworkUsageTracker
import ir.erfansn.nsmavpn.data.util.DefaultPingChecker
import ir.erfansn.nsmavpn.data.util.DefaultVpnGateContentExtractor
import ir.erfansn.nsmavpn.data.util.InstalledAppsListProvider
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.data.util.NetworkUsageTracker
import ir.erfansn.nsmavpn.data.util.PingChecker
import ir.erfansn.nsmavpn.data.util.VpnGateContentExtractor
import ir.erfansn.nsmavpn.feature.home.vpn.DefaultSstpVpnEventHandler
import ir.erfansn.nsmavpn.feature.home.vpn.DefaultSstpVpnServiceAction
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnEventHandler
import ir.erfansn.nsmavpn.feature.home.vpn.SstpVpnServiceAction
import ir.erfansn.nsmavpn.sync.DefaultVpnServersSyncManager
import ir.erfansn.nsmavpn.sync.VpnServersSyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    fun bindsGoogleGmailApi(gmailApi: GmailApi): GoogleApi<Gmail>

    @Binds
    fun bindsVpnGateMailMessagesRemoteDataSource(
        defaultVpnGateMessagesRemoteDataSource: DefaultVpnGateMailMessagesRemoteDataSource
    ): VpnGateMailMessagesRemoteDataSource

    @Binds
    fun bindsUserPreferencesLocalDataSource(
        defaultUserPreferencesLocalDataSource: DefaultUserPreferencesLocalDataSource
    ): UserPreferencesLocalDataSource

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

    @Binds
    fun bindsNetworkMonitor(
        connectivityNetworkMonitor: ConnectivityNetworkMonitor,
    ): NetworkMonitor

    @Binds
    fun bindsSstpVpnServiceActions(
        defaultSstpVpnServiceActions: DefaultSstpVpnServiceAction,
    ): SstpVpnServiceAction

    @Binds
    fun bindsUserProfileRepository(
        defaultProfileRepository: DefaultUserProfileRepository,
    ): UserProfileRepository

    @Binds
    fun bindsVpnServersLocalDataSource(
        defaultVpnServersLocalDataSource: DefaultVpnServersLocalDataSource,
    ): VpnServersLocalDataSource

    @Binds
    fun bindsServersRepository(
        defaultServersRepository: DefaultVpnServersRepository
    ): VpnServersRepository

    @Binds
    fun bindsSstpVpnEventHandler(
        defaultSstpVpnEventHandler: DefaultSstpVpnEventHandler
    ): SstpVpnEventHandler

    @Binds
    fun bindsNsmaVpnNotificationManager(
        defaultNsmaVpnNotificationManager: DefaultNsmaVpnNotificationManager
    ): NsmaVpnNotificationManager

    @Binds
    fun bindsConfigurationsRepository(
        defaultConfigurationRepository: DefaultConfigurationsRepository
    ): ConfigurationsRepository

    @Binds
    fun bindsUserProfileLocalDataSource(
        defaultUserProfileLocalDataSource: DefaultUserProfileLocalDataSource
    ): UserProfileLocalDataSource

    companion object {

        @[Provides IoDispatcher]
        fun providesIoDispatcher(): CoroutineDispatcher =
            Dispatchers.IO

        @[Provides DefaultDispatcher]
        fun providesDefaultDispatcher(): CoroutineDispatcher =
            Dispatchers.Default

        @Provides
        fun providesApplicationCoroutineScope(@IoDispatcher ioDispatcher: CoroutineDispatcher): CoroutineScope =
            CoroutineScope(SupervisorJob() + ioDispatcher)

        @[Provides Singleton]
        fun providesUserPreferencesDataStore(
            @ApplicationContext context: Context,
            applicationScope: CoroutineScope,
        ): DataStore<UserPreferences> =
            DataStoreFactory.create(
                serializer = UserPreferencesSerializer,
                produceFile = { context.dataStoreFile("user_preferences") },
                scope = applicationScope
            )

        @[Provides Singleton]
        fun providesLastVpnConnectionDataStore(
            @ApplicationContext context: Context,
            applicationScope: CoroutineScope
        ): DataStore<LastVpnConnection> =
            DataStoreFactory.create(
                serializer = LastVpnConnectionSerializer,
                produceFile = { context.dataStoreFile("last_vpn_connection") },
                scope = applicationScope,
            )

        @[Provides Singleton]
        fun providesVpnServersDataStore(
            @ApplicationContext context: Context,
            applicationScope: CoroutineScope
        ): DataStore<VpnServers> =
            DataStoreFactory.create(
                serializer = VpnServersSerializer,
                produceFile = { context.dataStoreFile("vpn_servers") },
                scope = applicationScope
            )

        @[Provides Singleton]
        fun providesUserProfileDataStore(
            @ApplicationContext context: Context,
            applicationScope: CoroutineScope
        ): DataStore<UserProfile> =
            DataStoreFactory.create(
                serializer = UserProfileSerializer,
                produceFile = { context.dataStoreFile("user_profile") },
                scope = applicationScope
            )

        @Provides
        fun providesWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)

        @[Provides Singleton]
        fun providesNetHttpTransport(): NetHttpTransport =
            GoogleNetHttpTransport.newTrustedTransport()

        @Provides
        fun providesGsonFactory(): GsonFactory =
            GsonFactory.getDefaultInstance()

        @[Provides Singleton]
        fun providesOkHttpClient(): OkHttpClient =
            OkHttpClient()
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
