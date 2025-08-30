package com.example.dvpdemo3.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type

// Defines the DataStore instance for the application.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "connected_devices_history")

/**
 * Repository for managing the history of connected devices using DataStore.
 *
 * This implementation stores the entire list of devices as a single JSON string,
 * which is more efficient for reading and writing compared to storing individual entries in a Set.
 *
 * @param context The application context.
 */
class ConnectedDevicesRepository(private val context: Context) {

    private val gson = Gson()
    private val deviceListType: Type = object : TypeToken<List<ConnectedDevice>>() {}.type

    /**
     * A flow that emits the list of historically connected devices.
     * The list is retrieved from DataStore and deserialized from JSON.
     */
    val connectedDevices: Flow<List<ConnectedDevice>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[CONNECTED_DEVICES_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    gson.fromJson(jsonString, deviceListType)
                } catch (e: Exception) {
                    // Log error or handle corruption
                    emptyList()
                }
            }
        }

    /**
     * Saves a newly connected device to the history.
     * If a device with the same address already exists, it updates the existing entry.
     *
     * @param device The device to save.
     */
    suspend fun saveConnectedDevice(device: ConnectedDevice) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[CONNECTED_DEVICES_KEY]
            val currentDevices = if (jsonString.isNullOrEmpty()) {
                mutableListOf()
            } else {
                try {
                    gson.fromJson<MutableList<ConnectedDevice>>(jsonString, deviceListType)
                } catch (e: Exception) {
                    mutableListOf()
                }
            }

            // Find and remove the old entry if it exists
            val existingIndex = currentDevices.indexOfFirst { it.address == device.address }
            if (existingIndex != -1) {
                currentDevices.removeAt(existingIndex)
            }

            // Add the new or updated device to the beginning of the list
            currentDevices.add(0, device)

            // Save the updated list back to DataStore
            preferences[CONNECTED_DEVICES_KEY] = gson.toJson(currentDevices)
        }
    }

    /**
     * Clears the entire connected device history.
     */
    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(CONNECTED_DEVICES_KEY)
        }
    }

    private companion object {
        // Use stringPreferencesKey for storing the list as a single JSON string
        val CONNECTED_DEVICES_KEY = stringPreferencesKey("connected_devices_json_list")
    }
}