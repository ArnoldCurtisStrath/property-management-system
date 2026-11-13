package com.example.propertymanagementsystem.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the checks that are used on the forms.
 *
 * These tests run on the computer, not on the phone. They check that the
 * rules used on the forms give the right answer.
 */
class ValidationTest {

    @Test
    fun emptyTextIsNotAccepted() {
        assertTrue(Validation.isEmpty(""))
        assertTrue(Validation.isEmpty("   "))
        assertFalse(Validation.isEmpty("a"))
    }

    @Test
    fun emailIsChecked() {
        assertTrue(Validation.isValidEmail("landlord@gmail.com"))
        assertTrue(Validation.isValidEmail("arnold.mbici@strathmore.edu"))
        assertFalse(Validation.isValidEmail("landlord"))
        assertFalse(Validation.isValidEmail("landlord@"))
        assertFalse(Validation.isValidEmail("@gmail.com"))
        assertFalse(Validation.isValidEmail(""))
    }

    @Test
    fun phoneIsChecked() {
        assertTrue(Validation.isValidPhone("0712345678"))
        assertTrue(Validation.isValidPhone("0712 345 678"))
        assertFalse(Validation.isValidPhone("712345678"))
        assertFalse(Validation.isValidPhone("071234567"))
        assertFalse(Validation.isValidPhone("abcdefghij"))
    }

    @Test
    fun amountIsChecked() {
        assertTrue(Validation.isValidAmount(25000.0))
        assertFalse(Validation.isValidAmount(0.0))
        assertFalse(Validation.isValidAmount(-100.0))
    }

    @Test
    fun dateIsChecked() {
        assertTrue(Validation.isValidDate("2026-11-12"))
        assertFalse(Validation.isValidDate("12-11-2026"))
        assertFalse(Validation.isValidDate("2026-13-01"))
        assertFalse(Validation.isValidDate(""))
    }
}
