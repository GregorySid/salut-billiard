package com.salut.data.remote.mock

import com.salut.data.remote.dto.BookingDto
import com.salut.data.remote.dto.CreateBookingRequest
import com.salut.data.remote.dto.ErrorDto
import com.salut.data.remote.dto.TableDto
import com.salut.data.remote.dto.TablesPageDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInterceptor @Inject constructor(
    private val json: Json,
    private val state: MockServerState,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Thread.sleep(SIMULATED_LATENCY_MS)
        return try {
            handle(request)
        } catch (t: Throwable) {
            buildResponse(request, 500, ErrorDto(message = t.message ?: "Mock error"))
        }
    }

    private fun handle(request: Request): Response {
        val segments = request.url.pathSegments
        val method = request.method
        return when {
            segments == listOf("tables") && method == "GET" -> handleTables(request)
            segments.size == 2 && segments[0] == "tables" && method == "GET" ->
                handleTable(request, segments[1].toLongOrNull())

            segments.size == 3 && segments[0] == "tables" && segments[2] == "slots" && method == "GET" ->
                handleSlots(request, segments[1].toLongOrNull())

            segments.size == 1 && segments[0] == "bookings" && method == "GET" ->
                buildResponse(request, 200, state.allBookings())

            segments.size == 1 && segments[0] == "bookings" && method == "POST" ->
                handleCreateBooking(request)

            segments.size == 2 && segments[0] == "bookings" && method == "DELETE" ->
                handleCancelBooking(request, segments[1])

            else -> buildResponse(request, 404, ErrorDto(message = "Not found"))
        }
    }

    private fun handleTables(request: Request): Response {
        val url = request.url
        val page = url.queryParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val pageSize = url.queryParameter("pageSize")?.toIntOrNull()?.coerceIn(1, 100) ?: 10
        val search = url.queryParameter("search")?.trim().orEmpty()
        val type = url.queryParameter("type")
        val status = url.queryParameter("status")
        val minCapacity = url.queryParameter("minCapacity")?.toIntOrNull()
        val maxCapacity = url.queryParameter("maxCapacity")?.toIntOrNull()
        val minPrice = url.queryParameter("minPrice")?.toIntOrNull()
        val maxPrice = url.queryParameter("maxPrice")?.toIntOrNull()
        val equipment = url.queryParameterValues("equipment")
            .filterNotNull()
            .filter { it.isNotBlank() }
            .toSet()

        val filtered = state.tablesFiltered(
            MockData.tables,
            search, type, status, minCapacity, maxCapacity, minPrice, maxPrice, equipment,
        )
        val total = filtered.size
        val offset = (page - 1) * pageSize
        val items = filtered.drop(offset).take(pageSize)
        val dto = TablesPageDto(
            items = items,
            page = page,
            pageSize = pageSize,
            total = total,
            hasMore = offset + items.size < total,
        )
        return buildResponse(request, 200, dto)
    }

    private fun handleTable(request: Request, id: Long?): Response {
        val table = id?.let { MockData.findTable(it) }
            ?: return buildResponse(request, 404, ErrorDto(message = "Table not found"))
        return buildResponse(request, 200, table)
    }

    private fun handleSlots(request: Request, id: Long?): Response {
        val date = request.url.queryParameter("date") ?: return buildResponse(
            request, 400, ErrorDto(message = "date is required"),
        )
        if (id == null || MockData.findTable(id) == null) {
            return buildResponse(request, 404, ErrorDto(message = "Table not found"))
        }
        return buildResponse(request, 200, state.slotsFor(id, date))
    }

    private fun handleCreateBooking(request: Request): Response {
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        val payload = buffer.readUtf8()
        val body = json.decodeFromString<CreateBookingRequest>(payload)
        val booking: BookingDto = state.addBooking(body)
        return buildResponse(request, 201, booking)
    }

    private fun handleCancelBooking(request: Request, id: String): Response {
        val removed = state.cancelBooking(id)
        return if (removed) {
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(204)
                .message("No Content")
                .build()
        } else {
            buildResponse(request, 404, ErrorDto(message = "Booking not found"))
        }
    }

    private inline fun <reified T> buildResponse(
        request: Request,
        code: Int,
        payload: T,
    ): Response {
        val body = json.encodeToString(payload)
            .toResponseBody(JSON_MEDIA_TYPE)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code < 300) "OK" else "Error")
            .body(body)
            .build()
    }

    companion object {
        private const val SIMULATED_LATENCY_MS = 450L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

fun MockServerState.tablesFiltered(
    source: List<TableDto>,
    search: String,
    type: String?,
    status: String?,
    minCapacity: Int?,
    maxCapacity: Int?,
    minPrice: Int?,
    maxPrice: Int?,
    equipment: Set<String>,
): List<TableDto> = source.filter { table ->
    val matchesSearch = search.isBlank() ||
        table.name.contains(search, ignoreCase = true) ||
        table.number.toString().contains(search)
    val matchesType = type.isNullOrBlank() || table.type.equals(type, ignoreCase = true)
    val matchesStatus = status.isNullOrBlank() || table.status.equals(status, ignoreCase = true)
    val matchesMinCapacity = minCapacity == null || table.capacity >= minCapacity
    val matchesMaxCapacity = maxCapacity == null || table.capacity <= maxCapacity
    val matchesMinPrice = minPrice == null || table.pricePerHour >= minPrice
    val matchesMaxPrice = maxPrice == null || table.pricePerHour <= maxPrice
    val matchesEquipment = equipment.isEmpty() || table.equipment.any { it in equipment }
    matchesSearch && matchesType && matchesStatus && matchesMinCapacity &&
        matchesMaxCapacity && matchesMinPrice && matchesMaxPrice && matchesEquipment
}
