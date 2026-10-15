package com.salut.core.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormats {
    private val apiDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val apiTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val display: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))

    fun LocalDate.toApi(): String = format(apiDate)
    fun LocalTime.toApi(): String = format(apiTime)

    fun parseDate(value: String): LocalDate = LocalDate.parse(value, apiDate)
    fun parseTime(value: String): LocalTime = LocalTime.parse(value, apiTime)

    fun LocalDate.toDisplay(): String = format(display)
    fun LocalTime.toDisplay(): String = format(apiTime)
}
