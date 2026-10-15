package com.salut.di

import android.content.Context
import androidx.room.Room
import com.salut.data.local.SalutDatabase
import com.salut.data.local.dao.BookingDao
import com.salut.data.local.dao.SlotDao
import com.salut.data.local.dao.TableDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SalutDatabase =
        Room.databaseBuilder(context, SalutDatabase::class.java, SalutDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideTableDao(database: SalutDatabase): TableDao = database.tableDao()

    @Provides
    fun provideSlotDao(database: SalutDatabase): SlotDao = database.slotDao()

    @Provides
    fun provideBookingDao(database: SalutDatabase): BookingDao = database.bookingDao()
}
