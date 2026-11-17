package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.domain.MeasurementMode
import com.example.domain.MeasurementRow
import com.example.domain.NumberFormatters
import com.example.domain.SheetDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfGenerator {
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val MARGIN_X = 32f
    private const val MARGIN_TOP = 32f
    private const val MARGIN_BOTTOM = 36f
    private const val CONTENT_WIDTH = A4_WIDTH - (MARGIN_X * 2f) // 531f

    // Column proportions summing to 531f
    private val colWidths = floatArrayOf(
        24f,  // #
        118f, // Item
        42f,  // Mode
        102f, // Size
        36f,  // Qty
        50f,  // Rate
        46f,  // RFT
        50f,  // Sq.Ft
        63f,  // Amount
    )

    suspend fun writePdfToUri(context: Context, uri: Uri, document: SheetDocument): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    renderPdf(document, out)
                } ?: error("Unable to open output stream")
            }
        }

    suspend fun createShareablePdfFile(context: Context, document: SheetDocument): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
                val safeSheet = document.sheetNo.ifBlank { "MeasureSheet" }
                    .replace(Regex("[^A-Za-z0-9_-]"), "_")
                val safeParty = document.partyName.ifBlank { "Party" }
                    .replace(Regex("[^A-Za-z0-9_-]"), "_")
                    .take(24)
                val outFile = File(dir, "${safeSheet}_${safeParty}.pdf")
                FileOutputStream(outFile).use { out ->
                    renderPdf(document, out)
                }
                outFile
            }
        }

    fun suggestedFileName(document: SheetDocument): String {
        val sheet = document.sheetNo.ifBlank { "MS-001" }.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val party = document.partyName.trim().takeIf { it.isNotEmpty() }
            ?.replace(Regex("[^A-Za-z0-9_-]"), "_")
            ?.take(24)
        return if (party != null) "${sheet}_${party}.pdf" else "${sheet}.pdf"
    }

    private fun renderPdf(document: SheetDocument, outputStream: OutputStream) {
        val pdf = PdfDocument()
        try {
            val rows = document.rows
            val rowHeight = 22f
            val firstPageTableTop = MARGIN_TOP + 96f
            val nextPageTableTop = MARGIN_TOP + 34f
            val footerReserve = 78f
            val tableHeaderHeight = 22f

            val maxFirstPageRows = ((A4_HEIGHT - MARGIN_BOTTOM - footerReserve - firstPageTableTop - tableHeaderHeight) / rowHeight).toInt().coerceAtLeast(1)
            val maxNextPageRows = ((A4_HEIGHT - MARGIN_BOTTOM - footerReserve - nextPageTableTop - tableHeaderHeight) / rowHeight).toInt().coerceAtLeast(1)

            val pages = mutableListOf<List<IndexedValue<MeasurementRow>>>()
            val indexedRows = rows.withIndex().toList()
            if (indexedRows.isEmpty()) {
                pages.add(emptyList())
            } else {
                var cursor = 0
                val firstChunk = indexedRows.subList(0, minOf(maxFirstPageRows, indexedRows.size))
                pages.add(firstChunk)
                cursor = firstChunk.size
                while (cursor < indexedRows.size) {
                    val end = minOf(cursor + maxNextPageRows, indexedRows.size)
                    pages.add(indexedRows.subList(cursor, end))
                    cursor = end
                }
            }

            val totalPages = pages.size
            pages.forEachIndexed { pageIndex, pageRows ->
                val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageIndex + 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas = page.canvas

                var y = MARGIN_TOP
                if (pageIndex == 0) {
                    y = drawFullHeader(canvas, document, y)
                } else {
                    y = drawContinuationHeader(canvas, document, y)
                }

                y = drawTableHeader(canvas, y, tableHeaderHeight)
                pageRows.forEachIndexed { localIdx, indexedRow ->
                    y = drawTableRow(
                        canvas = canvas,
                        rowNumber = indexedRow.index + 1,
                        row = indexedRow.value,
                        y = y,
                        rowHeight = rowHeight,
                        shaded = localIdx % 2 == 1,
                    )
                }

                if (pageIndex == totalPages - 1) {
                    drawGrandTotals(canvas, document, y + 10f)
                }

                drawPageFooter(canvas, pageIndex + 1, totalPages)
                pdf.finishPage(page)
            }

            pdf.writeTo(outputStream)
        } finally {
            pdf.close()
        }
    }

    private fun drawFullHeader(canvas: Canvas, document: SheetDocument, startY: Float): Float {
        val navy = Color.parseColor("#0F2942")
        val cyan = Color.parseColor("#0284C7")
        val slate = Color.parseColor("#475569")
        val border = Color.parseColor("#CBD5E1")
        val boxBg = Color.parseColor("#F4F7FA")

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = navy
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cyan
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = slate
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val hasBusiness = document.businessName.isNotBlank()
        if (hasBusiness) {
            canvas.drawText(document.businessName.trim(), MARGIN_X, startY + 16f, titlePaint)
            canvas.drawText("MEASUREMENT SHEET", MARGIN_X, startY + 30f, subtitlePaint)
        } else {
            canvas.drawText("MeasureSheet", MARGIN_X, startY + 16f, titlePaint)
            canvas.drawText("MEASUREMENT SHEET", MARGIN_X, startY + 30f, subtitlePaint)
        }

        // Right metadata box (Sheet No. & Date)
        val metaRect = RectF(A4_WIDTH - MARGIN_X - 175f, startY, A4_WIDTH - MARGIN_X, startY + 40f)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = boxBg }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = border
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRoundRect(metaRect, 4f, 4f, boxPaint)
        canvas.drawRoundRect(metaRect, 4f, 4f, strokePaint)

        canvas.drawText("Sheet No.", metaRect.left + 8f, metaRect.top + 14f, labelPaint)
        canvas.drawText("Date", metaRect.left + 94f, metaRect.top + 14f, labelPaint)
        canvas.drawText(document.sheetNo.ifBlank { "MS-001" }, metaRect.left + 8f, metaRect.top + 30f, valuePaint)
        canvas.drawText(document.dateText.ifBlank { "-" }, metaRect.left + 94f, metaRect.top + 30f, valuePaint)

        // Party Name bar
        val partyRect = RectF(MARGIN_X, startY + 48f, A4_WIDTH - MARGIN_X, startY + 82f)
        canvas.drawRoundRect(partyRect, 4f, 4f, boxPaint)
        canvas.drawRoundRect(partyRect, 4f, 4f, strokePaint)
        canvas.drawText("Party Name", partyRect.left + 10f, partyRect.top + 13f, labelPaint)
        canvas.drawText(
            document.partyName.trim().ifEmpty { "-" },
            partyRect.left + 10f,
            partyRect.top + 27f,
            valuePaint,
        )

        return startY + 96f
    }

    private fun drawContinuationHeader(canvas: Canvas, document: SheetDocument, startY: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F2942")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val title = buildString {
            if (document.businessName.isNotBlank()) {
                append(document.businessName.trim())
                append("  •  ")
            }
            append(document.sheetNo.ifBlank { "MS-001" })
            if (document.partyName.isNotBlank()) {
                append("  •  ")
                append(document.partyName.trim())
            }
        }
        canvas.drawText(title, MARGIN_X, startY + 14f, paint)
        return startY + 28f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float, height: Float): Float {
        val headerBg = Paint().apply { color = Color.parseColor("#0F2942") }
        canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + height, headerBg)

        val headers = arrayOf("#", "Item", "Mode", "Size", "Qty", "Rate", "RFT", "Sq.Ft", "Amount")
        val rightAligned = booleanArrayOf(false, false, false, false, true, true, true, true, true)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        var x = MARGIN_X
        headers.forEachIndexed { index, title ->
            val w = colWidths[index]
            val textY = y + height / 2f + 3f
            if (rightAligned[index]) {
                val tw = textPaint.measureText(title)
                canvas.drawText(title, x + w - tw - 6f, textY, textPaint)
            } else {
                canvas.drawText(title, x + 6f, textY, textPaint)
            }
            x += w
        }
        return y + height
    }

    private fun drawTableRow(
        canvas: Canvas,
        rowNumber: Int,
        row: MeasurementRow,
        y: Float,
        rowHeight: Float,
        shaded: Boolean,
    ): Float {
        if (shaded) {
            val bg = Paint().apply { color = Color.parseColor("#F8FAFC") }
            canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + rowHeight, bg)
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + rowHeight, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val sizeText = if (row.mode == MeasurementMode.SQFT) {
            val l = row.lengthText.ifBlank { "0" }
            val w = row.widthText.ifBlank { "0" }
            "$l ${row.lengthUnit.label} × $w ${row.widthUnit.label}"
        } else {
            val l = row.lengthText.ifBlank { "0" }
            "$l ${row.lengthUnit.label}"
        }

        val values = arrayOf(
            rowNumber.toString(),
            row.item.ifBlank { "-" },
            row.mode.label,
            sizeText,
            NumberFormatters.formatQty(row.qtyNum),
            NumberFormatters.formatRate(row.rateNum),
            if (row.mode == MeasurementMode.RFT) NumberFormatters.formatDecimal(row.rftValue) else "—",
            if (row.mode == MeasurementMode.SQFT) NumberFormatters.formatDecimal(row.sqFtValue) else "—",
            NumberFormatters.formatDecimal(row.amountValue),
        )
        val rightAligned = booleanArrayOf(false, false, false, false, true, true, true, true, true)

        var x = MARGIN_X
        values.forEachIndexed { index, raw ->
            val w = colWidths[index]
            val paint = if (index >= 4 || index == 0) numPaint else textPaint
            val clipped = clipText(raw, paint, w - 10f)
            val textY = y + rowHeight / 2f + 3f
            if (rightAligned[index]) {
                val tw = paint.measureText(clipped)
                canvas.drawText(clipped, x + w - tw - 6f, textY, paint)
            } else {
                canvas.drawText(clipped, x + 6f, textY, paint)
            }
            if (index < values.lastIndex) {
                canvas.drawLine(x + w, y, x + w, y + rowHeight, borderPaint)
            }
            x += w
        }
        return y + rowHeight
    }

    private fun drawGrandTotals(canvas: Canvas, document: SheetDocument, y: Float) {
        val totals = document.totals
        val rect = RectF(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + 46f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F2942") }
        canvas.drawRoundRect(rect, 4f, 4f, bg)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#93C5FD")
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val items = listOf(
            "Items" to totals.itemCount.toString(),
            "Qty" to NumberFormatters.formatQty(totals.totalQty),
            "Running Feet" to NumberFormatters.formatDecimal(totals.totalRft),
            "Square Feet" to NumberFormatters.formatDecimal(totals.totalSqFt),
            "Amount" to NumberFormatters.formatDecimal(totals.totalAmount),
        )
        val slotWidth = CONTENT_WIDTH / items.size
        items.forEachIndexed { idx, (label, value) ->
            val x = MARGIN_X + idx * slotWidth + 10f
            canvas.drawText(label, x, y + 16f, labelPaint)
            canvas.drawText(value, x, y + 34f, valPaint)
        }
    }

    private fun drawPageFooter(canvas: Canvas, pageNum: Int, totalPages: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val label = "Page $pageNum / $totalPages"
        val tw = paint.measureText(label)
        canvas.drawText(label, A4_WIDTH - MARGIN_X - tw, A4_HEIGHT - 16f, paint)
    }

    private fun clipText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 1 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return text.substring(0, end) + "…"
    }
}
