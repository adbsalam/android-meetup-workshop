package uk.co.androidmcr.memorymatch

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertNotEquals

class BrowserTimeZoneTest {
    @Test
    fun browserTimeZoneHasAnIanaIdentifier() {
        assertNotEquals("SYSTEM", TimeZone.currentSystemDefault().id)
    }
}
