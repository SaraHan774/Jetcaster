package com.august.jetcaster.di.modules

import android.content.Context
import androidx.room.Room
import com.august.jetcaster.data.CategoryStore
import com.august.jetcaster.data.EpisodeStore
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.data.room.CategoriesDao
import com.august.jetcaster.data.room.EpisodesDao
import com.august.jetcaster.data.room.JetcasterDatabase
import com.august.jetcaster.data.room.PodcastCategoryEntryDao
import com.august.jetcaster.data.room.PodcastFollowedEntryDao
import com.august.jetcaster.data.room.PodcastsDao
import com.august.jetcaster.data.room.TransactionRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(context: Context): JetcasterDatabase {
        return Room.databaseBuilder(context, JetcasterDatabase::class.java, "data.db")
            // This is not recommended for normal apps, but the goal of this sample isn't to
            // showcase all of Room.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePodcastsDao(db: JetcasterDatabase): PodcastsDao = db.podcastsDao()

    @Provides
    fun providePodcastFollowedEntryDao(db: JetcasterDatabase): PodcastFollowedEntryDao =
        db.podcastFollowedEntryDao()

    @Provides
    fun provideEpisodesDao(db: JetcasterDatabase): EpisodesDao = db.episodesDao()

    @Provides
    fun provideCategoryDao(db: JetcasterDatabase): CategoriesDao = db.categoriesDao()

    @Provides
    fun provideCategoryEntryDao(db: JetcasterDatabase): CategoriesDao = db.categoriesDao()

    @Provides
    fun provideCategoryStore(
        categoriesDao: CategoriesDao,
        categoryEntryDao: PodcastCategoryEntryDao,
        episodesDao: EpisodesDao,
        podcastsDao: PodcastsDao,
    ) = CategoryStore(
        categoriesDao = categoriesDao,
        categoryEntryDao = categoryEntryDao,
        episodesDao = episodesDao,
        podcastsDao = podcastsDao
    )

    @Provides
    fun provideEpisodeStore(
        episodesDao: EpisodesDao,
    ) = EpisodeStore(episodesDao = episodesDao)

    @Provides
    fun provideTransactionRunner(db: JetcasterDatabase) = db.transactionRunnerDao()

    @Provides
    fun providePodcastStore(
        podcastDao: PodcastsDao,
        podcastFollowedEntryDao: PodcastFollowedEntryDao,
        transactionRunner: TransactionRunner
    ) = PodcastStore(
        podcastDao = podcastDao,
        podcastFollowedEntryDao = podcastFollowedEntryDao,
        transactionRunner = transactionRunner
    )
}