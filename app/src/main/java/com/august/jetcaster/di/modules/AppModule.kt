package com.august.jetcaster.di.modules

import android.content.Context
import androidx.room.Room
import com.august.jetcaster.BuildConfig
import com.august.jetcaster.data.room.CategoriesDao
import com.august.jetcaster.data.room.EpisodesDao
import com.august.jetcaster.data.room.JetcasterDatabase
import com.august.jetcaster.data.room.PodcastCategoryEntryDao
import com.august.jetcaster.data.room.PodcastFollowedEntryDao
import com.august.jetcaster.data.room.PodcastsDao
import com.august.jetcaster.data.room.TransactionRunner
import com.rometools.rome.io.SyndFeedInput
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): JetcasterDatabase {
        return Room.databaseBuilder(context, JetcasterDatabase::class.java, "data.db")
            // This is not recommended for normal apps, but the goal of this sample isn't to
            // showcase all of Room.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providePodcastsDao(db: JetcasterDatabase): PodcastsDao = db.podcastsDao()

    @Singleton
    @Provides
    fun providePodcastFollowedEntryDao(db: JetcasterDatabase): PodcastFollowedEntryDao =
        db.podcastFollowedEntryDao()

    @Singleton
    @Provides
    fun provideEpisodesDao(db: JetcasterDatabase): EpisodesDao = db.episodesDao()

    @Singleton
    @Provides
    fun provideCategoryDao(db: JetcasterDatabase): CategoriesDao = db.categoriesDao()

    @Singleton
    @Provides
    fun provideCategoryEntryDao(db: JetcasterDatabase): PodcastCategoryEntryDao =
        db.podcastCategoryEntryDao()

    @Singleton
    @Provides
    fun provideTransactionRunner(db: JetcasterDatabase): TransactionRunner =
        db.transactionRunnerDao()

    @Singleton
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_cache"), (20 * 1024 * 1024).toLong()))
            .apply {
                if (BuildConfig.DEBUG) eventListenerFactory(LoggingEventListener.Factory())
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideSyndFeedInput() = SyndFeedInput()
}