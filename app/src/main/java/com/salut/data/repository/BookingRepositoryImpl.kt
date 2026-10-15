package com.salut.data.repository

import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppError
import com.salut.core.util.AppResult
import com.salut.core.util.DateFormats.toApi
import com.salut.core.util.ErrorType
import com.salut.core.util.IoDispatcher
import com.salut.data.local.dao.BookingDao
import com.salut.data.local.dao.SlotDao
import com.salut.data.mapper.toDomain
import com.salut.data.mapper.toEntity
import com.salut.data.remote.api.BilliardApi
import com.salut.data.remote.dto.CreateBookingRequest
import com.salut.domain.model.Booking
import com.salut.domain.repository.BookingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val api: BilliardApi,
    private val bookingDao: BookingDao,
    private val slotDao: SlotDao,
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BookingRepository {

    override fun observeBookings(): Flow<List<Booking>> =
        bookingDao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun refreshBookings(): AppResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return withContext(ioDispatcher) {
            try {
                val now = System.currentTimeMillis()
                val bookings = api.getBookings()
                bookingDao.clear()
                bookingDao.upsertAll(bookings.map { it.toEntity(now) })
                AppResult.Success(Unit)
            } catch (throwable: Throwable) {
                AppResult.Failure(AppError.fromThrowable(throwable))
            }
        }
    }

    override suspend fun createBooking(
        tableId: Long,
        date: LocalDate,
        slotIds: List<String>,
    ): AppResult<Booking> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return withContext(ioDispatcher) {
            try {
                val apiDate = date.toApi()
                val dto = api.createBooking(
                    CreateBookingRequest(tableId = tableId, date = apiDate, slotIds = slotIds),
                )
                val now = System.currentTimeMillis()
                bookingDao.upsert(dto.toEntity(now))
                slotDao.deleteForDate(tableId, apiDate)
                slotDao.upsertAll(api.getSlots(tableId, apiDate).map { it.toEntity(tableId, apiDate, now) })
                AppResult.Success(dto.toEntity(now).toDomain())
            } catch (throwable: Throwable) {
                AppResult.Failure(AppError.fromThrowable(throwable))
            }
        }
    }

    override suspend fun cancelBooking(id: String): AppResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return withContext(ioDispatcher) {
            try {
                val response = api.cancelBooking(id)
                if (!response.isSuccessful) {
                    return@withContext AppResult.Failure(
                        AppError(
                            type = if (response.code() == 404) ErrorType.NOT_FOUND else ErrorType.SERVER,
                            httpCode = response.code(),
                        ),
                    )
                }
                bookingDao.deleteById(id)
                AppResult.Success(Unit)
            } catch (throwable: Throwable) {
                AppResult.Failure(AppError.fromThrowable(throwable))
            }
        }
    }
}
