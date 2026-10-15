package com.salut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.salut.data.local.entity.BookingEntity
import com.salut.data.local.entity.SlotEntity
import com.salut.data.local.entity.TableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @RawQuery(observedEntities = [TableEntity::class])
    fun observe(query: SupportSQLiteQuery): Flow<List<TableEntity>>

    @RawQuery(observedEntities = [TableEntity::class])
    fun observeCount(query: SupportSQLiteQuery): Flow<Int>

    @RawQuery
    suspend fun queryOnce(query: SupportSQLiteQuery): List<TableEntity>

    @RawQuery
    suspend fun countOnce(query: SupportSQLiteQuery): Int

    @Query("SELECT * FROM tables WHERE id = :id")
    fun observeById(id: Long): Flow<TableEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TableEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TableEntity)
}

@Dao
interface SlotDao {
    @Query("SELECT * FROM slots WHERE tableId = :tableId AND date = :date ORDER BY start ASC")
    fun observe(tableId: Long, date: String): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE id = :id")
    suspend fun getById(id: String): SlotEntity?

    @Query("DELETE FROM slots WHERE tableId = :tableId AND date = :date")
    suspend fun deleteForDate(tableId: Long, date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SlotEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY date DESC, start DESC")
    fun observeAll(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getById(id: String): BookingEntity?

    @Query("SELECT * FROM bookings")
    suspend fun getAll(): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BookingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM bookings")
    suspend fun clear()
}
