package com.salut.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.salut.domain.model.TableFilter
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType
import com.salut.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[THEME_MODE])
    }

    val tableFilter: Flow<TableFilter> = dataStore.data.map { prefs ->
        TableFilter(
            type = prefs[FILTER_TYPE]?.let { TableType.fromApi(it) },
            status = prefs[FILTER_STATUS]?.let { TableStatus.fromApi(it) },
            minCapacity = prefs[FILTER_MIN_CAPACITY],
            maxCapacity = prefs[FILTER_MAX_CAPACITY],
            minPrice = prefs[FILTER_MIN_PRICE],
            maxPrice = prefs[FILTER_MAX_PRICE],
            equipment = prefs[FILTER_EQUIPMENT] ?: emptySet(),
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    suspend fun setTableFilter(filter: TableFilter) {
        dataStore.edit { prefs ->
            if (filter.type != null) prefs[FILTER_TYPE] = filter.type.apiValue else prefs.remove(FILTER_TYPE)
            if (filter.status != null) prefs[FILTER_STATUS] = filter.status.apiValue else prefs.remove(FILTER_STATUS)
            filter.minCapacity?.let { prefs[FILTER_MIN_CAPACITY] = it } ?: prefs.remove(FILTER_MIN_CAPACITY)
            filter.maxCapacity?.let { prefs[FILTER_MAX_CAPACITY] = it } ?: prefs.remove(FILTER_MAX_CAPACITY)
            filter.minPrice?.let { prefs[FILTER_MIN_PRICE] = it } ?: prefs.remove(FILTER_MIN_PRICE)
            filter.maxPrice?.let { prefs[FILTER_MAX_PRICE] = it } ?: prefs.remove(FILTER_MAX_PRICE)
            if (filter.equipment.isNotEmpty()) {
                prefs[FILTER_EQUIPMENT] = filter.equipment
            } else {
                prefs.remove(FILTER_EQUIPMENT)
            }
        }
    }

    suspend fun clearTableFilter() {
        dataStore.edit { prefs ->
            prefs.remove(FILTER_TYPE)
            prefs.remove(FILTER_STATUS)
            prefs.remove(FILTER_MIN_CAPACITY)
            prefs.remove(FILTER_MAX_CAPACITY)
            prefs.remove(FILTER_MIN_PRICE)
            prefs.remove(FILTER_MAX_PRICE)
            prefs.remove(FILTER_EQUIPMENT)
        }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FILTER_TYPE = stringPreferencesKey("filter_type")
        val FILTER_STATUS = stringPreferencesKey("filter_status")
        val FILTER_MIN_CAPACITY = intPreferencesKey("filter_min_capacity")
        val FILTER_MAX_CAPACITY = intPreferencesKey("filter_max_capacity")
        val FILTER_MIN_PRICE = intPreferencesKey("filter_min_price")
        val FILTER_MAX_PRICE = intPreferencesKey("filter_max_price")
        val FILTER_EQUIPMENT = stringSetPreferencesKey("filter_equipment")
    }
}
