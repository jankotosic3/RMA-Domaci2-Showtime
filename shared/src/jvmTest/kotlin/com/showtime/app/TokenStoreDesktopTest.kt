package com.showtime.app

import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.datastore.createDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Proves the Desktop/JVM DataStore token persistence works: save → read → clear.
class TokenStoreDesktopTest {

    @Test
    fun savesAndClearsToken() = runBlocking {
        val store = TokenStore(createDataStore())
        store.clear()
        assertNull(store.tokenFlow.first())

        store.save("abc123")
        assertEquals("abc123", store.tokenFlow.first())

        store.clear()
        assertNull(store.tokenFlow.first())
    }
}
