package com.salut.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.salut.R
import com.salut.core.util.AppError
import com.salut.core.util.ErrorType
import com.salut.domain.model.BookingStatus
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType

@StringRes
fun AppError.messageRes(): Int = when (type) {
    ErrorType.NETWORK -> R.string.error_network
    ErrorType.TIMEOUT -> R.string.error_timeout
    ErrorType.SERVER -> R.string.error_server
    ErrorType.NOT_FOUND -> R.string.error_not_found
    ErrorType.UNKNOWN -> R.string.error_unknown
}

@Composable
fun AppError.asMessage(): String = stringResource(messageRes())

@StringRes
fun TableType.labelRes(): Int = when (this) {
    TableType.RUSSIAN -> R.string.type_russian
    TableType.AMERICAN -> R.string.type_american
    TableType.SNOOKER -> R.string.type_snooker
}

@Composable
fun TableType.label(): String = stringResource(labelRes())

@StringRes
fun TableStatus.labelRes(): Int = when (this) {
    TableStatus.FREE -> R.string.status_free
    TableStatus.BUSY -> R.string.status_busy
    TableStatus.MAINTENANCE -> R.string.status_maintenance
}

@Composable
fun TableStatus.label(): String = stringResource(labelRes())

@StringRes
fun BookingStatus.labelRes(): Int = when (this) {
    BookingStatus.ACTIVE -> R.string.booking_status_active
    BookingStatus.PAST -> R.string.booking_status_past
    BookingStatus.CANCELLED -> R.string.booking_status_cancelled
}

@Composable
fun BookingStatus.label(): String = stringResource(labelRes())

@StringRes
private fun equipmentLabelRes(key: String): Int? = when (key) {
    "kievnitsa" -> R.string.equipment_kievnitsa
    "lighting" -> R.string.equipment_lighting
    "cues" -> R.string.equipment_cues
    "balls" -> R.string.equipment_balls
    "scoreboard" -> R.string.equipment_scoreboard
    "bar" -> R.string.equipment_bar
    else -> null
}

@Composable
fun equipmentLabel(key: String): String = equipmentLabelRes(key)?.let { stringResource(it) } ?: key
