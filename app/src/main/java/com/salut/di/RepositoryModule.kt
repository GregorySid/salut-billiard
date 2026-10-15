package com.salut.di

import com.salut.data.repository.BookingRepositoryImpl
import com.salut.data.repository.TableRepositoryImpl
import com.salut.domain.repository.BookingRepository
import com.salut.domain.repository.TableRepository
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
    abstract fun bindTableRepository(impl: TableRepositoryImpl): TableRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository
}
