package com.aliceqr.vicevirtue.di

import com.aliceqr.vicevirtue.data.repository.TrackableRepositoryImpl
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTrackableRepository(
        impl: TrackableRepositoryImpl
    ): TrackableRepository
}
