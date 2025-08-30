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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "connected_devices_history")


class ConnectedDevicesRepository(private val context: Context) {

    private val gson = Gson()
    private val deviceListType: Type = object : TypeToken<List<ConnectedDevice>>() {}.type

    
    val connectedDevices: Flow<List<ConnectedDevice>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[CONNECTED_DEVICES_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    gson.fromJson(jsonString, deviceListType)
                } catch (e: Exception) {

                    emptyList()
                }
            }
        }

    
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

            val existingIndex = currentDevices.indexOfFirst { it.address == device.address }
            if (existingIndex != -1) {
                currentDevices.removeAt(existingIndex)
            }

            currentDevices.add(0, device)

            preferences[CONNECTED_DEVICES_KEY] = gson.toJson(currentDevices)
        }
    }

    
    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(CONNECTED_DEVICES_KEY)
        }
    }

    private companion object {

        val CONNECTED_DEVICES_KEY = stringPreferencesKey("connected_devices_json_list")
    }
}