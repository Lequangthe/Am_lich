package com.quangthe.amlich.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fasting_prefs")

object FastingPreferences {
    private val FASTING_LEVEL = intPreferencesKey("fasting_level")

    fun getLevelFlow(context: Context): Flow<FastingLevel> =
        context.dataStore.data.map { prefs ->
            FastingLevel.entries[prefs[FASTING_LEVEL] ?: 0]
        }

    suspend fun getLevel(context: Context): FastingLevel =
        context.dataStore.data.first().let { prefs ->
            FastingLevel.entries[prefs[FASTING_LEVEL] ?: 0]
        }

    private val NHAT_NGOAT_MONTH = intPreferencesKey("nhat_ngoat_month")
    private val MONTH_NOTIF_TYPE = intPreferencesKey("month_notif_type")

    suspend fun setLevel(context: Context, level: FastingLevel) {
        context.dataStore.edit { prefs ->
            prefs[FASTING_LEVEL] = level.ordinal
        }
    }

    fun getNhatNgoatMonthFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[NHAT_NGOAT_MONTH] ?: 1
        }

    suspend fun getNhatNgoatMonth(context: Context): Int =
        context.dataStore.data.first().let { prefs ->
            prefs[NHAT_NGOAT_MONTH] ?: 1
        }

    suspend fun setNhatNgoatMonth(context: Context, month: Int) {
        context.dataStore.edit { prefs ->
            prefs[NHAT_NGOAT_MONTH] = month
        }
    }

    fun getMonthNotifTypeFlow(context: Context): Flow<MonthNotificationType> =
        context.dataStore.data.map { prefs ->
            MonthNotificationType.entries[prefs[MONTH_NOTIF_TYPE] ?: 0]
        }

    suspend fun getMonthNotifType(context: Context): MonthNotificationType =
        context.dataStore.data.first().let { prefs ->
            MonthNotificationType.entries[prefs[MONTH_NOTIF_TYPE] ?: 0]
        }

    suspend fun setMonthNotifType(context: Context, type: MonthNotificationType) {
        context.dataStore.edit { prefs ->
            prefs[MONTH_NOTIF_TYPE] = type.ordinal
        }
    }
}
