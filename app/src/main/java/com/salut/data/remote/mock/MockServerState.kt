package com.salut.data.remote.mock

import com.salut.data.remote.dto.BookingDto
import com.salut.data.remote.dto.CreateBookingRequest
import com.salut.data.remote.dto.SlotDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockServerState @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    private val bookings = CopyOnWriteArrayList<BookingDto>()
    private var counter = 0

    init {
        val today = LocalDate.now()
        bookings += BookingDto(
            id = "seed-1",
            tableId = 1L,
            tableName = "Бордо №1",
            date = today.plusDays(1).format(dateFormatter),
            start = "18:00",
            end = "20:00",
            status = "active",
            totalPrice = 1200,
        )
        bookings += BookingDto(
            id = "seed-2",
            tableId = 3L,
            tableName = "Бордо №3",
            date = today.minusDays(2).format(dateFormatter),
            start = "15:00",
            end = "16:00",
            status = "past",
            totalPrice = 600,
        )
    }

    fun allBookings(): List<BookingDto> = bookings.sortedWith(
        compareByDescending<BookingDto> { it.date }.thenByDescending { it.start },
    )

    fun slotsFor(tableId: Long, date: String): List<SlotDto> {
        val table = MockData.findTable(tableId) ?: return emptyList()
        val maintenance = table.status == "maintenance"
        val result = mutableListOf<SlotDto>()
        var hour = OPEN_HOUR
        while (hour < CLOSE_HOUR) {
            val start = LocalTime.of(hour, 0)
            val end = start.plusHours(1)
            val id = slotId(tableId, date, hour)
            val booked = bookings.any { it.tableId == tableId && it.date == date && it.start == start.format(timeFormatter) }
            val pseudoBusy = (id.hashCode() % 6) == 0
            result += SlotDto(
                id = id,
                start = start.format(timeFormatter),
                end = end.format(timeFormatter),
                available = !maintenance && !booked && !pseudoBusy,
                price = table.pricePerHour,
            )
            hour++
        }
        return result
    }

    fun addBooking(request: CreateBookingRequest): BookingDto {
        val table = MockData.findTable(request.tableId)
            ?: error("Table ${request.tableId} not found")
        val slots = slotsFor(request.tableId, request.date).filter { it.id in request.slotIds }
        val start = slots.minOfOrNull { it.start } ?: "10:00"
        val end = slots.maxOfOrNull { it.end } ?: "11:00"
        val price = slots.sumOf { it.price }
        counter += 1
        val booking = BookingDto(
            id = "booking-${System.currentTimeMillis()}-$counter",
            tableId = table.id,
            tableName = table.name,
            date = request.date,
            start = start,
            end = end,
            status = "active",
            totalPrice = price,
        )
        bookings += booking
        return booking
    }

    fun cancelBooking(id: String): Boolean = bookings.removeAll { it.id == id }

    private fun slotId(tableId: Long, date: String, hour: Int): String = "$tableId|$date|$hour"

    companion object {
        const val OPEN_HOUR = 10
        const val CLOSE_HOUR = 23
    }
}
