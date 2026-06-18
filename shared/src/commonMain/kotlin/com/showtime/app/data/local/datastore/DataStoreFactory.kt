package com.showtime.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal const val TOKEN_DATASTORE_FILE = "showtime.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
