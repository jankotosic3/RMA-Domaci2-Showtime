package com.showtime.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.showtime.app.appContext
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            appContext.filesDir.resolve(TOKEN_DATASTORE_FILE).absolutePath.toPath()
        }
    )
