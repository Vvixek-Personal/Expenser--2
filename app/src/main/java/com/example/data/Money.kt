package com.example.data

import com.example.ui.CurrencyManager
import java.text.NumberFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToLong

object Money {

    fun minorDigits(currencyCode: String): Int {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.defaultFractionDigits
        } catch (e: Exception) {
            when (currencyCode) {
                "JPY", "KRW", "VND", "CLP" -> 0
                "KWD", "BHD", "OMR", "TND", "LYD", "IQD", "JOD" -> 3
                else -> 2
            }
        }
    }

    fun factor(currencyCode: String): Double {
        return 10.0.pow(minorDigits(currencyCode))
    }

    fun parse(text: String, currencyCode: String, locale: Locale = Locale.getDefault()): Long? {
        if (text.isBlank()) return null
        val cleaned = text.trim().replace(Regex("[^\\d.,-]"), "")
        if (cleaned.isEmpty() || cleaned == "-") return null
        return try {
            val lastDot = cleaned.lastIndexOf('.')
            val lastComma = cleaned.lastIndexOf(',')
            val normalized = if (lastComma > lastDot) {
                cleaned.replace(".", "").replace(",", ".")
            } else {
                cleaned.replace(",", "").replace(".", ".")
            }
            val doubleVal = normalized.toDouble()
            (doubleVal * factor(currencyCode)).roundToLong()
        } catch (e: Exception) {
            null
        }
    }

    fun formatPlain(minorUnits: Long, currencyCode: String): String {
        val f = factor(currencyCode)
        return String.format(Locale.US, "%.${minorDigits(currencyCode)}f", minorUnits / f)
    }

    fun format(minorUnits: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val currency = try { Currency.getInstance(currencyCode) } catch (e: Exception) { null }
        val nf = NumberFormat.getCurrencyInstance(locale)
        if (currency != null) nf.currency = currency
        return nf.format(toDouble(minorUnits, currencyCode))
    }

    fun toDouble(minorUnits: Long, currencyCode: String): Double {
        return minorUnits / factor(currencyCode)
    }

    fun fromDouble(amount: Double, currencyCode: String): Long {
        return (amount * factor(currencyCode)).roundToLong()
    }

    fun add(a: Long, b: Long): Long = Math.addExact(a, b)
    fun subtract(a: Long, b: Long): Long = Math.subtractExact(a, b)
    fun multiply(a: Long, b: Long): Long = Math.multiplyExact(a, b)

    fun convert(
        amountMinor: Long,
        fromCurrency: String,
        toCurrency: String,
        rates: Map<String, Double> = CurrencyManager.getRatesMap()
    ): Long {
        if (fromCurrency == toCurrency) return amountMinor
        val fromRate = rates[fromCurrency] ?: return amountMinor
        val toRate = rates[toCurrency] ?: return amountMinor
        val baseAmount = amountMinor.toDouble() / factor(fromCurrency) / fromRate
        val targetAmount = baseAmount * toRate * factor(toCurrency)
        return targetAmount.roundToLong()
    }
}
