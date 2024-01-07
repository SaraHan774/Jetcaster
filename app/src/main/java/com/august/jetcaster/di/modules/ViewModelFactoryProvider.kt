package com.august.jetcaster.di.modules

import com.august.jetcaster.ui.home.category.PodcastCategoryViewModel
import com.august.jetcaster.ui.home.category.PodcastCategoryViewModelFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {
    fun podcastsCategoryViewModelFactory() : PodcastCategoryViewModelFactory
}