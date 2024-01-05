package com.august.jetcaster.di.modules

import android.content.Context
import com.august.jetcaster.data.PodcastsFetcher
import com.rometools.rome.io.SyndFeedInput
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_cache"), (20 * 1024 * 1024).toLong()))
            .apply {
                // if (BuildConfig.DEBUG) eventListenerFactory(LoggingEventListener.Factory())
            }
            .build()
    }

    @Provides
    fun provideSyndFeedInput() = SyndFeedInput()

    @Provides
    fun providePodcastFetcher(
        okHttpClient: OkHttpClient,
        syndFeedInput: SyndFeedInput,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = PodcastsFetcher(
        okHttpClient,
        syndFeedInput,
        ioDispatcher
    )
}