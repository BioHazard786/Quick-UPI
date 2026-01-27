package com.biohazard786.quickupi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This is like initializing the database/localStorage
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserStore(private val context: Context) {

    // Define the keys (like keys in localStorage)
    companion object {
        val UPI_ID_KEY = stringPreferencesKey("upi_id") // Legacy
        val UPI_IDS_KEY = stringPreferencesKey("upi_ids") // New: Comma separated list
        val PAYEE_NAME_KEY = stringPreferencesKey("payee_name")
        val RECENT_AMOUNTS_KEY = stringPreferencesKey("recent_amounts")
        val SHOW_UPI_ID_KEY = booleanPreferencesKey("show_upi_id")
    }

    // Get the UPI ID (First one is default/primary)
    // Legacy support: If ID list is empty, fall back to single ID key
    val upiIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[UPI_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            // Fallback to legacy key if new key hasn't been set yet
            val legacyId = preferences[UPI_ID_KEY]
            if (!legacyId.isNullOrBlank()) listOf(legacyId) else emptyList()
        }
    }

    // Maintained for potential backward compatibility access, though upiIds is preferred
    val upiId: Flow<String?> = upiIds.map { it.firstOrNull() }


    val payeeName: Flow<String?> =
        context.dataStore.data.map { preferences -> preferences[PAYEE_NAME_KEY] }

    val recentAmounts: Flow<List<String>> =
        context.dataStore.data.map { preferences ->
            val serialized = preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500"
            serialized.split(",").filter { it.isNotBlank() }
        }

    val showUpiId: Flow<Boolean> =
        context.dataStore.data.map { preferences -> preferences[SHOW_UPI_ID_KEY] ?: true }

    // Save the UPI IDs list
    suspend fun saveUpiIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[UPI_IDS_KEY] = ids.joinToString(",")
            // Also update legacy key for safety/fallback with the primary ID
            if (ids.isNotEmpty()) {
                preferences[UPI_ID_KEY] = ids.first()
            } else {
                preferences.remove(UPI_ID_KEY)
            }
        }
    }

    // Deprecated: Use saveUpiIds instead. Kept for minimal disruption if needed.
    // This will overwrite the entire list with just this one ID.
    suspend fun saveUpiId(id: String) {
        saveUpiIds(listOf(id))
    }

    suspend fun savePayeeName(name: String) {
        context.dataStore.edit { preferences -> preferences[PAYEE_NAME_KEY] = name }
    }

    suspend fun saveShowUpiId(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_UPI_ID_KEY] = show }
    }

    suspend fun saveRecentAmount(amount: String) {
        if (amount.isBlank()) return

        context.dataStore.edit { preferences ->
            val currentList =
                (preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toMutableList()

            // Remove if exists to move to top
            currentList.remove(amount)
            // Add to front
            currentList.add(0, amount)
            // Keep only top 3
            val newList = currentList.take(3)

            preferences[RECENT_AMOUNTS_KEY] = newList.joinToString(",")
        }
    }
}
