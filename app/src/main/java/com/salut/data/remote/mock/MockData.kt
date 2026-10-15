package com.salut.data.remote.mock

import com.salut.data.remote.dto.TableDto

object MockData {

    private const val BORDO_IMAGE = "android.resource://com.salut/drawable/bardo"
    private const val LEO_IMAGE = "android.resource://com.salut/drawable/leo"

    private data class Seed(
        val id: Long,
        val name: String,
        val status: String,
        val capacity: Int,
        val pricePerHour: Int,
        val equipment: List<String>,
    )

    private val seeds = listOf(
        Seed(
            id = 1L,
            name = "Бордо №1",
            status = "free",
            capacity = 4,
            pricePerHour = 900,
            equipment = listOf("lighting", "cues", "balls", "scoreboard"),
        ),
        Seed(
            id = 2L,
            name = "Бордо №2",
            status = "free",
            capacity = 4,
            pricePerHour = 900,
            equipment = listOf("kievnitsa", "lighting", "cues", "balls"),
        ),
        Seed(
            id = 3L,
            name = "Бордо №3",
            status = "busy",
            capacity = 4,
            pricePerHour = 1000,
            equipment = listOf("lighting", "cues", "balls", "bar"),
        ),
        Seed(
            id = 4L,
            name = "Бордо №4",
            status = "maintenance",
            capacity = 4,
            pricePerHour = 1000,
            equipment = listOf("kievnitsa", "cues", "balls", "scoreboard"),
        ),
        Seed(
            id = 5L,
            name = "Лео №1",
            status = "free",
            capacity = 2,
            pricePerHour = 700,
            equipment = listOf("lighting", "cues", "balls"),
        ),
        Seed(
            id = 6L,
            name = "Лео №2",
            status = "busy",
            capacity = 2,
            pricePerHour = 700,
            equipment = listOf("lighting", "cues", "balls", "scoreboard"),
        ),
    )

    val tables: List<TableDto> = seeds.map { seed ->
        TableDto(
            id = seed.id,
            name = seed.name,
            number = seed.id.toInt(),
            image = if (seed.name.startsWith("Лео")) LEO_IMAGE else BORDO_IMAGE,
            type = "american",
            status = seed.status,
            capacity = seed.capacity,
            description = "${seed.name} — профессиональный американский стол. " +
                "Ровное основание, качественное сукно и хорошая подсветка. " +
                "Подходит как для любителей, так и для турниров.",
            pricePerHour = seed.pricePerHour,
            equipment = seed.equipment,
        )
    }

    fun findTable(id: Long): TableDto? = tables.firstOrNull { it.id == id }
}
