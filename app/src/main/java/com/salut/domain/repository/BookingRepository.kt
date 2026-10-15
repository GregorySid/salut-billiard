package com.salut.domain.repository

import com.salut.core.util.AppResult
import com.salut.domain.model.Booking
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BookingRepository {
    fun observeBookings(): Flow<List<Booking>>

    suspend fun refreshBookings(): AppResult<Unit>

    suspend fun createBooking(
        tableId: Long,
        date: LocalDate,
        slotIds: List<String>,
    ): AppResult<Booking>

    suspend fun cancelBooking(id: String): AppResult<Unit>
}
