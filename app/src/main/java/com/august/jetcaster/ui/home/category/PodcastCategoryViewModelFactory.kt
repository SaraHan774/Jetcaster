package com.august.jetcaster.ui.home.category

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface PodcastCategoryViewModelFactory {
    fun create(@Assisted categoryId: Long): PodcastCategoryViewModel
}