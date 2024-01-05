package com.august.jetcaster.di.modules

import com.august.jetcaster.data.CategoryStore
import com.august.jetcaster.data.EpisodeStore
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.data.PodcastsFetcher
import com.august.jetcaster.data.PodcastsRepository
import com.august.jetcaster.data.room.TransactionRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


@Module
@InstallIn(SingletonComponent::class)
object PodcastRepositoryModule {

    @Provides
    fun providePodcastRepository(
        podcastsFetcher: PodcastsFetcher,
        podcastStore: PodcastStore,
        episodeStore: EpisodeStore,
        categoryStore: CategoryStore,
        transactionRunner: TransactionRunner,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ) = PodcastsRepository(
        podcastsFetcher,
        podcastStore,
        episodeStore,
        categoryStore,
        transactionRunner,
        mainDispatcher
    )
}