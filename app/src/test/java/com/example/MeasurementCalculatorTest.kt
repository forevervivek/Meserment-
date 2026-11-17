package com.example

import com.example.domain.LengthUnit
import com.example.domain.MeasurementMode
import com.example.domain.MeasurementRow
import com.example.domain.SheetTotals
import com.example.domain.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementCalculatorTest {

    @Test
    fun photoshopUnitConversion_isExact() {
        assertEquals("25.4", UnitConverter.convertValue("10", LengthUnit.IN, LengthUnit.CM))
        assertEquals("10", UnitConverter.convertValue("25.4", LengthUnit.CM, LengthUnit.IN))
        assertEquals("12", UnitConverter.convertValue("1", LengthUnit.FT, LengthUnit.IN))
        assertEquals("1", UnitConverter.convertValue("12", LengthUnit.IN, LengthUnit.FT))
        assertEquals("1000", UnitConverter.convertValue("1", LengthUnit.M, LengthUnit.MM))
        assertEquals("1", UnitConverter.convertValue("100", LengthUnit.CM, LengthUnit.M))
        assertEquals("", UnitConverter.convertValue("", LengthUnit.IN, LengthUnit.CM))
    }

    @Test
    fun sqFtAndRftCalculations_areAccurate() {
        val sqFtRow = MeasurementRow(
            id = 1L,
            mode = MeasurementMode.SQFT,
            item = "Slab",
            lengthText = "10",
            lengthUnit = LengthUnit.FT,
            widthText = "24",
            widthUnit = LengthUnit.IN, // 2 ft
            qtyText = "3",
            rateText = "50",
        )
        assertEquals(60.0, sqFtRow.sqFtValue, 0.0001)
        assertEquals(0.0, sqFtRow.rftValue, 0.0001)
        assertEquals(3000.0, sqFtRow.amountValue, 0.0001)

        val rftRow = MeasurementRow(
            id = 2L,
            mode = MeasurementMode.RFT,
            item = "Moulding",
            lengthText = "12",
            lengthUnit = LengthUnit.FT,
            widthText = "99",
            widthUnit = LengthUnit.FT, // ignored in RFT mode
            qtyText = "2",
            rateText = "25",
        )
        assertEquals(24.0, rftRow.rftValue, 0.0001)
        assertEquals(0.0, rftRow.sqFtValue, 0.0001)
        assertEquals(600.0, rftRow.amountValue, 0.0001)

        val totals = SheetTotals.fromRows(listOf(sqFtRow, rftRow))
        assertEquals(2, totals.itemCount)
        assertEquals(5.0, totals.totalQty, 0.0001)
        assertEquals(24.0, totals.totalRft, 0.0001)
        assertEquals(60.0, totals.totalSqFt, 0.0001)
        assertEquals(3600.0, totals.totalAmount, 0.0001)
    }
}
