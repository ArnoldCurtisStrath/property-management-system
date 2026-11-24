package com.example.propertymanagementsystem.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MpesaParserTest {

    @Test
    fun readsAReceivedMessage() {
        val parsed = MpesaParser.parse(
            "QGH7X8Y9Z Confirmed. Ksh2,500.00 received from GRACE WANJIKU " +
                "0712345678 on 24/11/26 at 10:42 AM."
        )

        assertNotNull(parsed)
        assertEquals(2500.0, parsed!!.amount, 0.01)
        assertEquals("712345678", parsed.phone)
        assertEquals("2026-11-24", parsed.date)
        assertEquals("QGH7X8Y9Z", parsed.reference)
        assertEquals("GRACE WANJIKU", parsed.senderName)
    }

    @Test
    fun readsAMessageWithoutAReference() {
        val parsed = MpesaParser.parse(
            "Confirmed. Ksh 1,500 received from 0723456789 - PETER OTIENO " +
                "on 05/11/2026 at 9:00 AM"
        )

        assertNotNull(parsed)
        assertEquals(1500.0, parsed!!.amount, 0.01)
        assertEquals("723456789", parsed.phone)
        assertEquals("2026-11-05", parsed.date)
    }

    @Test
    fun rejectsASentMessage() {
        assertNull(
            MpesaParser.parse(
                "QGH7X8Y9Z Confirmed. Ksh2,500.00 sent to GRACE WANJIKU 0712345678"
            )
        )
    }

    @Test
    fun rejectsGarbage() {
        assertNull(MpesaParser.parse(""))
        assertNull(MpesaParser.parse("Hello, this is not an M-Pesa message"))
        assertNull(MpesaParser.parse("Confirmed. No amount here"))
    }

    @Test
    fun normalisesPhoneNumbers() {
        assertEquals("712345678", MpesaParser.normalizePhone("0712345678"))
        assertEquals("712345678", MpesaParser.normalizePhone("+254712345678"))
        assertEquals("712345678", MpesaParser.normalizePhone("254712345678"))
    }
}
