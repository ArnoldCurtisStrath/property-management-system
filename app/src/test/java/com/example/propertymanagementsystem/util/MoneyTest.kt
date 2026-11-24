package com.example.propertymanagementsystem.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun formatsWholeShillingsWithCommas() {
        assertEquals("Ksh 28,500", Money.format(28500.0))
        assertEquals("Ksh 0", Money.format(0.0))
        assertEquals("Ksh 7,000", Money.format(6999.6))
    }

    @Test
    fun formatsNegativeMoney() {
        assertEquals("-Ksh 1,000", Money.format(-1000.0))
    }

    @Test
    fun plainHasNoPrefix() {
        assertEquals("28,500", Money.plain(28500.0))
    }

    @Test
    fun parsesWhatTheAgentTypes() {
        assertEquals(2500.0, Money.parse("Ksh 2,500.00")!!, 0.01)
        assertEquals(2500.0, Money.parse("2,500")!!, 0.01)
        assertNull(Money.parse(""))
        assertNull(Money.parse("abc"))
    }
}
