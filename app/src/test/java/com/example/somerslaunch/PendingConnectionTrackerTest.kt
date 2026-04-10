package com.example.somerslaunch

import com.example.somerslaunch.utils.PendingConnectionTracker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingConnectionTrackerTest {

    @Test
    fun fastSuccessSignalIsNotMissedAfterTrackingStarts() = runBlocking {
        val tracker = PendingConnectionTracker()
        val signal = tracker.start("OfficeWifi")

        tracker.completeIfMatches("OfficeWifi")

        assertTrue(signal.await())
    }

    @Test
    fun mismatchedSuccessSignalDoesNotCompletePendingConnection() {
        val tracker = PendingConnectionTracker()
        val signal = tracker.start("OfficeWifi")

        tracker.completeIfMatches("GuestWifi")

        assertFalse(signal.isCompleted)
    }
}
