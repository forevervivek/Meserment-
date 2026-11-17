package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LengthUnit(
    val label: String,
    val metersFactor: BigDecimal,
) {
    FT("ft", BigDecimal("0.3048")),
    IN("in", BigDecimal("0.0254")),
    CM("cm", BigDecimal("0.01")),
    MM("mm", BigDecimal("0.001")),
    M("m", BigDecimal("1")),
    ;

    companion object {
        fun fromLabel(value: String): LengthUnit =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: FT
    }
}

enum class MeasurementMode(val label: String) {
    SQFT("Sq.Ft"),
    RFT("RFT"),
    ;

    companion object {
        fun fromName(value: String): MeasurementMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: SQFT
    }
}

object UnitConverter {
    private val feetInMeters = BigDecimal("0.3048")

    fun convertValue(rawInput: String, from: LengthUnit, to: LengthUnit): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty() || from == to) return rawInput
        val parsed = trimmed.toBigDecimalOrNull() ?: return rawInput
        val inMeters = parsed.multiply(from.metersFactor)
        val converted = inMeters.divide(to.metersFactor, 4, RoundingMode.HALF_UP)
        if (converted.compareTo(BigDecimal.ZERO) == 0) return "0"
        return converted.stripTrailingZeros().toPlainString()
    }

    fun toFeet(value: Double, unit: LengthUnit): Double {
        if (value == 0.0) return 0.0
        val bd = BigDecimal.valueOf(value)
        return bd.multiply(unit.metersFactor)
            .divide(feetInMeters, 10, RoundingMode.HALF_UP)
            .toDouble()
    }
}

data class MeasurementRow(
    val id: Long,
    val mode: MeasurementMode = MeasurementMode.SQFT,
    val item: String = "",
    val lengthText: String = "",
    val lengthUnit: LengthUnit = LengthUnit.FT,
    val widthText: String = "",
    val widthUnit: LengthUnit = LengthUnit.FT,
    val qtyText: String = "1",
    val rateText: String = "",
) {
    val lengthNum: Double get() = lengthText.trim().toDoubleOrNull() ?: 0.0
    val widthNum: Double get() = widthText.trim().toDoubleOrNull() ?: 0.0
    val qtyNum: Double get() = qtyText.trim().toDoubleOrNull() ?: 0.0
    val rateNum: Double get() = rateText.trim().toDoubleOrNull() ?: 0.0

    val rftValue: Double
        get() = if (mode == MeasurementMode.RFT) {
            UnitConverter.toFeet(lengthNum, lengthUnit) * qtyNum
        } else {
            0.0
        }

    val sqFtValue: Double
        get() = if (mode == MeasurementMode.SQFT) {
            UnitConverter.toFeet(lengthNum, lengthUnit) *
                UnitConverter.toFeet(widthNum, widthUnit) *
                qtyNum
        } else {
            0.0
        }

    val amountValue: Double
        get() {
            val measure = if (mode == MeasurementMode.SQFT) sqFtValue else rftValue
            return measure * rateNum
        }
}

data class SheetTotals(
    val itemCount: Int = 0,
    val totalQty: Double = 0.0,
    val totalRft: Double = 0.0,
    val totalSqFt: Double = 0.0,
    val totalAmount: Double = 0.0,
) {
    companion object {
        fun fromRows(rows: List<MeasurementRow>): SheetTotals = SheetTotals(
            itemCount = rows.size,
            totalQty = rows.sumOf { it.qtyNum },
            totalRft = rows.sumOf { it.rftValue },
            totalSqFt = rows.sumOf { it.sqFtValue },
            totalAmount = rows.sumOf { it.amountValue },
        )
    }
}

data class SheetDocument(
    val partyName: String = "",
    val businessName: String = "",
    val dateText: String = todayDateString(),
    val sheetNo: String = "MS-001",
    val rows: List<MeasurementRow> = emptyList(),
) {
    val totals: SheetTotals get() = SheetTotals.fromRows(rows)
}

fun todayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

object NumberFormatters {
    private val decimal2 = DecimalFormat("#,##0.00")
    private val qtyFmt = DecimalFormat("#,##0.##")

    fun formatDecimal(value: Double): String = decimal2.format(value)

    fun formatQty(value: Double): String = qtyFmt.format(value)

    fun formatRate(value: Double): String =
        if (value == 0.0) "0.00" else decimal2.format(value)
}
