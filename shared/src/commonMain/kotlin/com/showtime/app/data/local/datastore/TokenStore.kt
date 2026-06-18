package com.showtime.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenStore(private val dataStore: DataStore<Preferences>) {
    private val keyToken = stringPreferencesKey("access_token")

    val tokenFlow: Flow<String?> = dataStore.data.map { it[keyToken] }

    suspend fun save(token: String) { dataStore.edit { it[keyToken] = token } }
    suspend fun clear() { dataStore.edit { it.remove(keyToken) } }
}
