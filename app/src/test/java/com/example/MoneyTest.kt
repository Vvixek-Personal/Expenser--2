package com.example

import com.example.data.Money
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class MoneyTest {

    @Test
    fun testMinorDigitsPerCurrency() {
        // 0-decimal currencies
        assertEquals(0, Money.minorDigits("JPY"))
        assertEquals(0, Money.minorDigits("KRW"))
        assertEquals(0, Money.minorDigits("VND"))
        assertEquals(0, Money.minorDigits("CLP"))

        // 3-decimal currencies
        assertEquals(3, Money.minorDigits("KWD"))
        assertEquals(3, Money.minorDigits("BHD"))
        assertEquals(3, Money.minorDigits("OMR"))

        // 2-decimal standard currencies
        assertEquals(2, Money.minorDigits("USD"))
        assertEquals(2, Money.minorDigits("EUR"))
        assertEquals(2, Money.minorDigits("INR"))
        assertEquals(2, Money.minorDigits("GBP"))
    }

    @Test
    fun testParseFormattingAcrossLocales() {
        // Standard English format
        assertEquals(123456L, Money.parse("1,234.56", "USD", Locale.US))
        assertEquals(123456L, Money.parse("1234.56", "USD", Locale.US))
        assertEquals(1250L, Money.parse("12.50", "USD", Locale.US))

        // European format with comma decimal
        assertEquals(123456L, Money.parse("1.234,56", "EUR", Locale.GERMANY))
        assertEquals(1250L, Money.parse("12,50", "EUR", Locale.GERMANY))

        // Flexible user input: comma entered on US keyboard
        assertEquals(1250L, Money.parse("12,50", "USD", Locale.US))
        // Flexible user input: dot entered on German keyboard
        assertEquals(1250L, Money.parse("12.50", "EUR", Locale.GERMANY))

        // Negative values
        assertEquals(-5000L, Money.parse("-50.00", "USD", Locale.US))

        // Zero and blanks
        assertEquals(0L, Money.parse("0", "USD", Locale.US))
        assertEquals(0L, Money.parse("0.00", "USD", Locale.US))
        assertNull(Money.parse("", "USD", Locale.US))
        assertNull(Money.parse("   ", "USD", Locale.US))
        assertNull(Money.parse("abc", "USD", Locale.US))
        assertNull(Money.parse("--", "USD", Locale.US))

        // 0-decimal currencies
        assertEquals(500L, Money.parse("500", "JPY", Locale.JAPAN))
        assertEquals(500L, Money.parse("500.00", "JPY", Locale.JAPAN))

        // 3-decimal currencies
        assertEquals(1234L, Money.parse("1.234", "KWD", Locale.US))

        // Huge values
        val hugeText = "9876543210.50"
        val hugeParsed = Money.parse(hugeText, "USD", Locale.US)
        assertEquals(987654321050L, hugeParsed)
    }

    @Test
    fun testExactnessNoBinaryFloatDrift() {
        // In IEEE-754 Double: 0.10 + 0.10 + ... 1,000 times drift away from 100.00
        // In Money Long minor units: 10 cents = 10L
        var minorSum = 0L
        for (i in 1..1000) {
            minorSum = Money.add(minorSum, 10L)
        }
        assertEquals(10000L, minorSum) // Exactly $100.00 (10,000 cents)
        assertEquals("100.00", Money.formatPlain(minorSum, "USD"))
        assertEquals(100.0, Money.toDouble(minorSum, "USD"), 0.0)
    }

    @Test
    fun testCurrencyConversionRoundedOnceHalfEven() {
        // Exchange rates map: USD = 1.0, EUR = 0.92, JPY = 155.0, KWD = 0.31
        val rates = mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "JPY" to 155.0,
            "KWD" to 0.31
        )

        // Convert 100.00 USD (10000 cents) -> EUR (rate 0.92) = 92.00 EUR (9200 cents)
        val eurMinor = Money.convert(10000L, "USD", "EUR", rates)
        assertEquals(9200L, eurMinor)

        // Convert 100.00 USD -> JPY (rate 155.0) = 15,500 JPY (0 decimals -> 15500)
        val jpyMinor = Money.convert(10000L, "USD", "JPY", rates)
        assertEquals(15500L, jpyMinor)

        // Convert 100.00 USD -> KWD (rate 0.31) = 31.000 KWD (3 decimals -> 31000)
        val kwdMinor = Money.convert(10000L, "USD", "KWD", rates)
        assertEquals(31000L, kwdMinor)
    }

    @Test
    fun testArithmeticOverflowProtection() {
        try {
            Money.add(Long.MAX_VALUE, 1L)
            fail("Expected ArithmeticException on overflow")
        } catch (_: ArithmeticException) {
            // Success
        }

        try {
            Money.multiply(Long.MAX_VALUE / 2 + 1, 2L)
            fail("Expected ArithmeticException on multiplication overflow")
        } catch (_: ArithmeticException) {
            // Success
        }
    }
}
