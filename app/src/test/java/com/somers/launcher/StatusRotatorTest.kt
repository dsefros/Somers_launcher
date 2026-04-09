package com.somers.launcher

import com.somers.launcher.domain.StatusRotator
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusRotatorTest {
    @Test
    fun rotatesEveryTickLoopingOverItems() {
        val rotator = StatusRotator(listOf("a", "b", "c"))
        assertEquals("a", rotator.statusForTick(0))
        assertEquals("b", rotator.statusForTick(1))
        assertEquals("c", rotator.statusForTick(2))
        assertEquals("a", rotator.statusForTick(3))
    }
}
