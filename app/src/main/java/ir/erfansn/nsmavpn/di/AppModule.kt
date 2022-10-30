package ir.erfansn.nsmavpn.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.data.source.remote.DefaultGmailMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.GmailMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApiImpl
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindsGmailApi(gmailApiImpl: GmailApiImpl): GmailApi

    @Binds
    abstract fun bindsGmailMessagesRemoteDataSource(
        defaultGmailMessagesRemoteDataSource: DefaultGmailMessagesRemoteDataSource
    ): GmailMessagesRemoteDataSource

    companion object {

        @Provides
        fun providesIoDispatcher() = Dispatchers.IO
    }
}