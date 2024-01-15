package com.august.jetcaster.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ServiceScope

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @ServiceScoped
    @ServiceScope
    @Provides
    fun provideServiceCoroutineScope(@MainDispatcher mainDispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + mainDispatcher)
}