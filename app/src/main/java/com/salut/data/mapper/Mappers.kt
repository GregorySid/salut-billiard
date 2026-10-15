package com.salut.data.mapper

import com.salut.core.util.DateFormats.parseDate
import com.salut.core.util.DateFormats.parseTime
import com.salut.data.local.entity.BookingEntity
import com.salut.data.local.entity.SlotEntity
import com.salut.data.local.entity.TableEntity
import com.salut.data.remote.dto.BookingDto
import com.salut.data.remote.dto.SlotDto
import com.salut.data.remote.dto.TableDto
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.Booking
import com.salut.domain.model.BookingStatus
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType
import com.salut.domain.model.TimeSlot
import java.time.LocalDateTime

private const val EQUIPMENT_SEPARATOR = "|"

fun encodeEquipment(equipment: List<String>): String =
    if (equipment.isEmpty()) "" else equipment.joinToString(
        separator = EQUIPMENT_SEPARATOR,
        prefix = EQUIPMENT_SEPARATOR,
        postfix = EQUIPMENT_SEPARATOR,
    )

fun decodeEquipment(value: String): List<String> =
    value.split(EQUIPMENT_SEPARATOR).filter { it.isNotBlank() }

fun TableDto.toEntity(now: Long): TableEntity = TableEntity(
    id = id,
    name = name,
    number = number,
    imageUrl = image,
    type = type,
    status = status,
    capacity = capacity,
    description = description,
    pricePerHour = pricePerHour,
    equipment = encodeEquipment(equipment),
    cachedAt = now,
)

fun TableEntity.toDomain(): BilliardTable = BilliardTable(
    id = id,
    name = name,
    number = number,
    imageUrl = imageUrl,
    type = TableType.fromApi(type),
    status = TableStatus.fromApi(status),
    capacity = capacity,
    description = description,
    pricePerHour = pricePerHour,
    equipment = decodeEquipment(equipment),
)

fun SlotDto.toEntity(tableId: Long, date: String, now: Long): SlotEntity = SlotEntity(
    id = id,
    tableId = tableId,
    date = date,
    start = start,
    end = end,
    available = available,
    price = price,
    cachedAt = now,
)

fun SlotEntity.toDomain(): TimeSlot = TimeSlot(
    id = id,
    tableId = tableId,
    date = parseDate(date),
    start = parseTime(start),
    end = parseTime(end),
    available = available,
    price = price,
)

fun BookingDto.toEntity(now: Long): BookingEntity = BookingEntity(
    id = id,
    tableId = tableId,
    tableName = tableName,
    date = date,
    start = start,
    end = end,
    status = status,
    totalPrice = totalPrice,
    cachedAt = now,
)

fun BookingEntity.toDomain(now: LocalDateTime = LocalDateTime.now()): Booking {
    val parsedDate = parseDate(date)
    val parsedEnd = parseTime(end)
    val status = when {
        this.status.equals("cancelled", ignoreCase = true) -> BookingStatus.CANCELLED
        LocalDateTime.of(parsedDate, parsedEnd).isBefore(now) -> BookingStatus.PAST
        else -> BookingStatus.ACTIVE
    }
    return Booking(
        id = id,
        tableId = tableId,
        tableName = tableName,
        date = parsedDate,
        start = parseTime(start),
        end = parsedEnd,
        status = status,
        totalPrice = totalPrice,
    )
}
