package com.salut.domain.model

data class TableFilter(
    val type: TableType? = null,
    val status: TableStatus? = null,
    val minCapacity: Int? = null,
    val maxCapacity: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val equipment: Set<String> = emptySet(),
) {
    val isActive: Boolean
        get() = type != null || status != null || minCapacity != null || maxCapacity != null ||
            minPrice != null || maxPrice != null || equipment.isNotEmpty()

    val activeCount: Int
        get() = listOf(
            type != null,
            status != null,
            minCapacity != null || maxCapacity != null,
            minPrice != null || maxPrice != null,
            equipment.isNotEmpty(),
        ).count { it }

    companion object {
        val EMPTY = TableFilter()
    }
}

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val hasMore: Boolean,
)
