package com.salut.domain.repository

import com.salut.core.util.AppResult
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.Page
import com.salut.domain.model.TableFilter
import com.salut.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TableRepository {
    fun observeTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): Flow<Page<BilliardTable>>

    suspend fun getTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): Page<BilliardTable>

    suspend fun refreshTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): AppResult<Unit>

    fun observeTable(id: Long): Flow<BilliardTable?>

    suspend fun refreshTable(id: Long): AppResult<Unit>

    fun observeSlots(tableId: Long, date: LocalDate): Flow<List<TimeSlot>>

    suspend fun refreshSlots(tableId: Long, date: LocalDate): AppResult<Unit>
}
