package com.salut

import com.salut.data.local.entity.BookingEntity
import com.salut.data.mapper.decodeEquipment
import com.salut.data.mapper.encodeEquipment
import com.salut.data.mapper.toDomain
import com.salut.data.mapper.toEntity
import com.salut.data.remote.dto.TableDto
import com.salut.data.remote.mock.MockServerState
import com.salut.data.remote.mock.tablesFiltered
import com.salut.domain.model.BookingStatus
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MappersTest {

    @Test
    fun equipmentEncoding_roundTrips() {
        val encoded = encodeEquipment(listOf("lighting", "cues"))
        assertEquals("|lighting|cues|", encoded)
        assertEquals(listOf("lighting", "cues"), decodeEquipment(encoded))
        assertTrue(decodeEquipment("").isEmpty())
    }

    @Test
    fun tableDto_mapsToDomain() {
        val dto = TableDto(
            id = 3,
            name = "Стол",
            number = 7,
            image = "img",
            type = "snooker",
            status = "busy",
            capacity = 4,
            description = "desc",
            pricePerHour = 900,
            equipment = listOf("lighting"),
        )

        val domain = dto.toEntity(now = 0L).toDomain()

        assertEquals(TableType.SNOOKER, domain.type)
        assertEquals(TableStatus.BUSY, domain.status)
        assertEquals(7, domain.number)
        assertEquals(listOf("lighting"), domain.equipment)
    }

    @Test
    fun bookingEntity_pastStatusWhenInThePast() {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val pastDate = java.time.LocalDate.now().minusDays(3)
        val entity = BookingEntity(
            id = "b1",
            tableId = 1,
            tableName = "Стол",
            date = pastDate.format(dateFormatter),
            start = "10:00",
            end = "11:00",
            status = "active",
            totalPrice = 500,
            cachedAt = 0L,
        )

        val domain = entity.toDomain(now = LocalDateTime.now())

        assertEquals(BookingStatus.PAST, domain.status)
    }

    @Test
    fun tablesFiltered_appliesTypeAndEquipment() {
        val source = listOf(
            TableDto(1, "A", 1, "img", "russian", "free", 2, "", 500, listOf("cues")),
            TableDto(2, "B", 2, "img", "snooker", "free", 4, "", 800, listOf("lighting")),
        )

        val byType = MockServerState().tablesFiltered(
            source = source,
            search = "",
            type = "snooker",
            status = null,
            minCapacity = null,
            maxCapacity = null,
            minPrice = null,
            maxPrice = null,
            equipment = emptySet(),
        )
        assertEquals(1, byType.size)
        assertEquals(2L, byType.first().id)

        val byEquipment = MockServerState().tablesFiltered(
            source = source,
            search = "",
            type = null,
            status = null,
            minCapacity = null,
            maxCapacity = null,
            minPrice = null,
            maxPrice = null,
            equipment = setOf("cues"),
        )
        assertEquals(1, byEquipment.size)
        assertEquals(1L, byEquipment.first().id)
    }
}
