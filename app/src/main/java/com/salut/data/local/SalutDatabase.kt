package com.salut.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salut.data.local.dao.BookingDao
import com.salut.data.local.dao.SlotDao
import com.salut.data.local.dao.TableDao
import com.salut.data.local.entity.BookingEntity
import com.salut.data.local.entity.SlotEntity
import com.salut.data.local.entity.TableEntity

@Database(
    entities = [TableEntity::class, SlotEntity::class, BookingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SalutDatabase : RoomDatabase() {
    abstract fun tableDao(): TableDao
    abstract fun slotDao(): SlotDao
    abstract fun bookingDao(): BookingDao

    companion object {
        const val NAME = "salut.db"
    }
}
