package com.salut.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableDto(
    val id: Long,
    val name: String,
    val number: Int,
    val image: String,
    val type: String,
    val status: String,
    val capacity: Int,
    val description: String,
    val pricePerHour: Int,
    val equipment: List<String> = emptyList(),
)

@Serializable
data class TablesPageDto(
    val items: List<TableDto>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val hasMore: Boolean,
)

@Serializable
data class SlotDto(
    val id: String,
    val start: String,
    val end: String,
    val available: Boolean,
    val price: Int,
)

@Serializable
data class BookingDto(
    val id: String,
    val tableId: Long,
    val tableName: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String,
    val totalPrice: Int,
)

@Serializable
data class CreateBookingRequest(
    val tableId: Long,
    val date: String,
    val slotIds: List<String>,
)

@Serializable
data class ErrorDto(
    val message: String? = null,
)
