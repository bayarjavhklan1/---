package com.bayrjavhlanmw.finance.ui

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {
    private val input = Regex("\\d{1,12}([.,]\\d{1,2})?")
    fun parseMinor(text: String): Long? {
        val normalized = text.trim()
        if (!input.matches(normalized)) return null
        return runCatching {
            BigDecimal(normalized.replace(',', '.')).movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY).longValueExact().takeIf { it > 0 }
        }.getOrNull()
    }
    fun editable(minor: Long): String = BigDecimal.valueOf(minor, 2).stripTrailingZeros().toPlainString()
    fun format(minor: Long): String = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(BigDecimal.valueOf(minor, 2)) + " ₮"
}
