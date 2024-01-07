package com.august.jetcaster.di.modules

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IODispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY) // compile time 과 binary 에도 포함되고 reflection 을 통해 접근 가능합니다.
@Qualifier
annotation class MainImmediateDispatcher