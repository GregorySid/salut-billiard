package com.salut.domain.model

enum class TableType {
    RUSSIAN,
    AMERICAN,
    SNOOKER;

    val apiValue: String
        get() = when (this) {
            RUSSIAN -> "russian"
            AMERICAN -> "american"
            SNOOKER -> "snooker"
        }

    companion object {
        fun fromApi(value: String?): TableType = when (value?.lowercase()) {
            "american" -> AMERICAN
            "snooker" -> SNOOKER
            else -> RUSSIAN
        }
    }
}

enum class TableStatus {
    FREE,
    BUSY,
    MAINTENANCE;

    val apiValue: String
        get() = when (this) {
            FREE -> "free"
            BUSY -> "busy"
            MAINTENANCE -> "maintenance"
        }

    companion object {
        fun fromApi(value: String?): TableStatus = when (value?.lowercase()) {
            "busy" -> BUSY
            "maintenance" -> MAINTENANCE
            else -> FREE
        }
    }
}

data class BilliardTable(
    val id: Long,
    val name: String,
    val number: Int,
    val imageUrl: String,
    val type: TableType,
    val status: TableStatus,
    val capacity: Int,
    val description: String,
    val pricePerHour: Int,
    val equipment: List<String>,
)

data class TimeSlot(
    val id: String,
    val tableId: Long,
    val date: java.time.LocalDate,
    val start: java.time.LocalTime,
    val end: java.time.LocalTime,
    val available: Boolean,
    val price: Int,
)

enum class BookingStatus {
    ACTIVE,
    PAST,
    CANCELLED,
}

data class Booking(
    val id: String,
    val tableId: Long,
    val tableName: String,
    val date: java.time.LocalDate,
    val start: java.time.LocalTime,
    val end: java.time.LocalTime,
    val status: BookingStatus,
    val totalPrice: Int,
)
