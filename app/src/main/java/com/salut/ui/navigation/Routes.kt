package com.salut.ui.navigation

object Routes {
    const val ARG_TABLE_ID = "tableId"

    const val TABLES = "tables"
    const val FILTERS = "filters"
    const val BOOKINGS = "bookings"
    const val SETTINGS = "settings"
    const val TABLE_DETAIL = "tables/{$ARG_TABLE_ID}"
    const val BOOKING = "tables/{$ARG_TABLE_ID}/booking"

    fun tableDetail(tableId: Long): String = "tables/$tableId"
    fun booking(tableId: Long): String = "tables/$tableId/booking"
}
