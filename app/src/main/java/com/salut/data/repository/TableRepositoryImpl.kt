package com.salut.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppError
import com.salut.core.util.AppResult
import com.salut.core.util.DateFormats.toApi
import com.salut.core.util.ErrorType
import com.salut.core.util.IoDispatcher
import com.salut.data.local.dao.SlotDao
import com.salut.data.local.dao.TableDao
import com.salut.data.mapper.toDomain
import com.salut.data.mapper.toEntity
import com.salut.data.remote.api.BilliardApi
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.Page
import com.salut.domain.model.TableFilter
import com.salut.domain.model.TimeSlot
import com.salut.domain.repository.TableRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepositoryImpl @Inject constructor(
    private val api: BilliardApi,
    private val tableDao: TableDao,
    private val slotDao: SlotDao,
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TableRepository {

    override fun observeTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): Flow<Page<BilliardTable>> {
        val offset = (page - 1) * pageSize
        val (where, args) = buildWhere(filter, search)
        val selectQuery = SimpleSQLiteQuery(
            "SELECT * FROM tables $where ORDER BY number ASC LIMIT ? OFFSET ?",
            (args + pageSize + offset).toTypedArray(),
        )
        val countQuery = SimpleSQLiteQuery("SELECT COUNT(*) FROM tables $where", args.toTypedArray())
        return combine(
            tableDao.observe(selectQuery),
            tableDao.observeCount(countQuery),
        ) { items, total ->
            Page(
                items = items.map { it.toDomain() },
                page = page,
                pageSize = pageSize,
                total = total,
                hasMore = offset + items.size < total,
            )
        }.flowOn(ioDispatcher)
    }

    override suspend fun getTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): Page<BilliardTable> = withContext(ioDispatcher) {
        val offset = (page - 1) * pageSize
        val (where, args) = buildWhere(filter, search)
        val selectQuery = SimpleSQLiteQuery(
            "SELECT * FROM tables $where ORDER BY number ASC LIMIT ? OFFSET ?",
            (args + pageSize + offset).toTypedArray(),
        )
        val countQuery = SimpleSQLiteQuery("SELECT COUNT(*) FROM tables $where", args.toTypedArray())
        val items = tableDao.queryOnce(selectQuery)
        val total = tableDao.countOnce(countQuery)
        Page(
            items = items.map { it.toDomain() },
            page = page,
            pageSize = pageSize,
            total = total,
            hasMore = offset + items.size < total,
        )
    }

    override suspend fun refreshTables(
        filter: TableFilter,
        search: String,
        page: Int,
        pageSize: Int,
    ): AppResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return runCatchingNetwork {
            val response = api.getTables(
                page = page,
                pageSize = pageSize,
                search = search.ifBlank { null },
                type = filter.type?.apiValue,
                status = filter.status?.apiValue,
                minCapacity = filter.minCapacity,
                maxCapacity = filter.maxCapacity,
                minPrice = filter.minPrice,
                maxPrice = filter.maxPrice,
                equipment = filter.equipment.takeIf { it.isNotEmpty() }?.toList(),
            )
            val now = System.currentTimeMillis()
            tableDao.upsertAll(response.items.map { it.toEntity(now) })
        }
    }

    override fun observeTable(id: Long): Flow<BilliardTable?> =
        tableDao.observeById(id).map { it?.toDomain() }.flowOn(ioDispatcher)

    override suspend fun refreshTable(id: Long): AppResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return runCatchingNetwork {
            val dto = api.getTable(id)
            tableDao.upsert(dto.toEntity(System.currentTimeMillis()))
        }
    }

    override fun observeSlots(tableId: Long, date: LocalDate): Flow<List<TimeSlot>> =
        slotDao.observe(tableId, date.toApi())
            .map { items -> items.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun refreshSlots(tableId: Long, date: LocalDate): AppResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return AppResult.Failure(AppError(ErrorType.NETWORK))
        }
        return runCatchingNetwork {
            val apiDate = date.toApi()
            val slots = api.getSlots(tableId, apiDate)
            val now = System.currentTimeMillis()
            slotDao.deleteForDate(tableId, apiDate)
            slotDao.upsertAll(slots.map { it.toEntity(tableId, apiDate, now) })
        }
    }

    private fun buildWhere(filter: TableFilter, search: String): Pair<String, List<Any>> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()

        if (search.isNotBlank()) {
            conditions += "(name LIKE ? OR CAST(number AS TEXT) LIKE ?)"
            val like = "%$search%"
            args += like
            args += like
        }
        filter.type?.let {
            conditions += "type = ?"
            args += it.apiValue
        }
        filter.status?.let {
            conditions += "status = ?"
            args += it.apiValue
        }
        filter.minCapacity?.let {
            conditions += "capacity >= ?"
            args += it
        }
        filter.maxCapacity?.let {
            conditions += "capacity <= ?"
            args += it
        }
        filter.minPrice?.let {
            conditions += "pricePerHour >= ?"
            args += it
        }
        filter.maxPrice?.let {
            conditions += "pricePerHour <= ?"
            args += it
        }
        if (filter.equipment.isNotEmpty()) {
            conditions += "(" + filter.equipment.joinToString(" OR ") { "equipment LIKE ?" } + ")"
            filter.equipment.forEach { args += "%|$it|%" }
        }
        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        return where to args
    }

    private suspend inline fun runCatchingNetwork(
        crossinline block: suspend () -> Unit,
    ): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            block()
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.fromThrowable(throwable))
        }
    }
}
