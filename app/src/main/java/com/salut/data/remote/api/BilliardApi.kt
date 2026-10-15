package com.salut.data.remote.api

import com.salut.data.remote.dto.BookingDto
import com.salut.data.remote.dto.CreateBookingRequest
import com.salut.data.remote.dto.SlotDto
import com.salut.data.remote.dto.TableDto
import com.salut.data.remote.dto.TablesPageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BilliardApi {

    @GET("tables")
    suspend fun getTables(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("minCapacity") minCapacity: Int? = null,
        @Query("maxCapacity") maxCapacity: Int? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("equipment") equipment: List<String>? = null,
    ): TablesPageDto

    @GET("tables/{id}")
    suspend fun getTable(@Path("id") id: Long): TableDto

    @GET("tables/{id}/slots")
    suspend fun getSlots(
        @Path("id") id: Long,
        @Query("date") date: String,
    ): List<SlotDto>

    @GET("bookings")
    suspend fun getBookings(): List<BookingDto>

    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): BookingDto

    @DELETE("bookings/{id}")
    suspend fun cancelBooking(@Path("id") id: String): Response<Unit>
}
