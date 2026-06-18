package com.showtime.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    val dir = File(System.getProperty("user.home"), ".showtime").also { it.mkdirs() }
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { File(dir, TOKEN_DATASTORE_FILE).absolutePath.toPath() }
    )
}
