package com.salut.ui.filters

import com.salut.domain.model.TableFilter
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType

data class FiltersUiState(
    val type: TableType? = null,
    val status: TableStatus? = null,
    val capacityRange: ClosedFloatingPointRange<Float> = CAPACITY_MIN..CAPACITY_MAX,
    val priceRange: ClosedFloatingPointRange<Float> = PRICE_MIN..PRICE_MAX,
    val equipment: Set<String> = emptySet(),
) {
    fun toFilter(): TableFilter = TableFilter(
        type = type,
        status = status,
        minCapacity = capacityRange.start.toInt().takeIf { capacityRange.start > CAPACITY_MIN },
        maxCapacity = capacityRange.endInclusive.toInt().takeIf { capacityRange.endInclusive < CAPACITY_MAX },
        minPrice = priceRange.start.toInt().takeIf { priceRange.start > PRICE_MIN },
        maxPrice = priceRange.endInclusive.toInt().takeIf { priceRange.endInclusive < PRICE_MAX },
        equipment = equipment,
    )

    companion object {
        const val CAPACITY_MIN = 1f
        const val CAPACITY_MAX = 10f
        const val PRICE_MIN = 0f
        const val PRICE_MAX = 2000f
        const val PRICE_STEP = 20
        val EQUIPMENT_OPTIONS = listOf("kievnitsa", "lighting", "cues", "balls", "scoreboard", "bar")

        fun fromFilter(filter: TableFilter): FiltersUiState = FiltersUiState(
            type = filter.type,
            status = filter.status,
            capacityRange = (filter.minCapacity?.toFloat() ?: CAPACITY_MIN)..(filter.maxCapacity?.toFloat() ?: CAPACITY_MAX),
            priceRange = (filter.minPrice?.toFloat() ?: PRICE_MIN)..(filter.maxPrice?.toFloat() ?: PRICE_MAX),
            equipment = filter.equipment,
        )
    }
}
