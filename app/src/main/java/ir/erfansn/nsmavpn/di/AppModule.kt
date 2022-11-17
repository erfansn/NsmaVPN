package ir.erfansn.nsmavpn.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import ir.erfansn.nsmavpn.data.source.remote.DefaultVpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApiImpl
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindsGmailApi(gmailApiImpl: GmailApiImpl): GmailApi

    @Binds
    abstract fun bindsGmailMessagesRemoteDataSource(
        defaultVpnGateMessagesRemoteDataSource: DefaultVpnGateMessagesRemoteDataSource,
    ): VpnGateMessagesRemoteDataSource

    companion object {

        @[Provides Singleton]
        fun providesVpnProviderDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = VpnProviderSerializer,
                produceFile = { context.dataStoreFile("vpn_provider") }
            )

        @Provides
        fun providesIoDispatcher() = Dispatchers.IO
    }
}