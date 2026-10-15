package com.salut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val number: Int,
    val imageUrl: String,
    val type: String,
    val status: String,
    val capacity: Int,
    val description: String,
    val pricePerHour: Int,
    val equipment: String,
    val cachedAt: Long,
)

@Entity(tableName = "slots", primaryKeys = ["id"])
data class SlotEntity(
    val id: String,
    val tableId: Long,
    val date: String,
    val start: String,
    val end: String,
    val available: Boolean,
    val price: Int,
    val cachedAt: Long,
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val tableId: Long,
    val tableName: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String,
    val totalPrice: Int,
    val cachedAt: Long,
)
