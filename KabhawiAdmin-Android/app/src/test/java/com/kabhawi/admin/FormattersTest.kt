package com.kabhawi.admin

import com.kabhawi.admin.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormattersTest {

    @Test
    fun money_usesWesternDigitsAndCurrency() {
        assertEquals("1,250.50 ₪", Formatters.money(1250.5, "₪"))
        assertEquals("0.00", Formatters.money(0.0, ""))
    }

    @Test
    fun decimal_dropsTrailingZeros() {
        assertEquals("12", Formatters.decimal(12.0))
        assertEquals("12.5", Formatters.decimal(12.5))
        assertEquals("12.35", Formatters.decimal(12.3456))
    }

    @Test
    fun parse_acceptsArabicIndicDigits() {
        assertEquals(125.5, Formatters.parseDecimal("١٢٥٫٥")!!, 0.0001)
        assertEquals(42, Formatters.parseInt("٤٢"))
        assertEquals(3.75, Formatters.parseDecimal("3,75")!!, 0.0001)
        assertEquals(7, Formatters.parseInt("۷"))
    }

    @Test
    fun parse_rejectsInvalidInput() {
        assertNull(Formatters.parseDecimal(""))
        assertNull(Formatters.parseDecimal("abc"))
        assertNull(Formatters.parseInt("1.5"))
    }

    @Test
    fun percent_handlesZeroTotal() {
        assertEquals("0%", Formatters.percent(3, 0))
        assertEquals("50%", Formatters.percent(1, 2))
    }
}
