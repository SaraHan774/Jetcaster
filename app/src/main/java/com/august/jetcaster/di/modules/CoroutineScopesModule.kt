package com.august.jetcaster.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MediaControllerScope

@InstallIn(SingletonComponent::class)
@Module
object CoroutineScopesModule {

    @Singleton
    @ApplicationScope
    @Provides
    fun provideDefaultCoroutineScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher) =
        CoroutineScope(SupervisorJob() + defaultDispatcher)

    @MediaControllerScope
    @Provides
    fun provideMcCoroutineScope() = CoroutineScope(SupervisorJob())
}