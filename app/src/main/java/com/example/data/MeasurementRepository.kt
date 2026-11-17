package com.example.data

import com.example.domain.LengthUnit
import com.example.domain.MeasurementMode
import com.example.domain.MeasurementRow
import com.example.domain.SheetDocument
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val dao: MeasurementDao) {
    val savedSheets: Flow<List<MeasurementSheetEntity>> = dao.getSavedSheets()

    suspend fun loadDraft(): SheetDocument? {
        val draft = dao.getDraftWithRows() ?: return null
        return draft.toDocument()
    }

    suspend fun saveDraft(document: SheetDocument) {
        val (entity, rows) = document.toEntities(isDraft = true)
        dao.saveDraft(entity, rows)
    }

    suspend fun savePartySheet(existingSavedId: Long?, document: SheetDocument): Long {
        val (entity, rows) = document.toEntities(isDraft = false)
        return dao.savePartySheet(existingSavedId, entity, rows)
    }

    suspend fun loadSavedSheet(sheetId: Long): SheetDocument? =
        dao.getSheetWithRows(sheetId)?.toDocument()

    suspend fun deleteSavedSheet(sheetId: Long) =
        dao.deleteSheetById(sheetId)

    suspend fun generateNextSheetNo(): String {
        val existing = dao.getAllSavedSheetNumbers()
        val maxNum = existing.mapNotNull { sheetNo ->
            Regex("""MS-(\d+)""", RegexOption.IGNORE_CASE)
                .find(sheetNo.trim())
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
        }.maxOrNull() ?: 0
        val next = maxNum + 1
        return "MS-${next.toString().padStart(3, '0')}"
    }

    private fun SheetWithRows.toDocument(): SheetDocument =
        SheetDocument(
            partyName = sheet.partyName,
            businessName = sheet.businessName,
            dateText = sheet.dateText,
            sheetNo = sheet.sheetNo,
            rows = rows.sortedBy { it.sortOrder }.mapIndexed { index, row ->
                MeasurementRow(
                    id = if (row.id > 0) row.id else (index + 1).toLong(),
                    mode = MeasurementMode.fromName(row.mode),
                    item = row.item,
                    lengthText = row.lengthText,
                    lengthUnit = LengthUnit.fromLabel(row.lengthUnit),
                    widthText = row.widthText,
                    widthUnit = LengthUnit.fromLabel(row.widthUnit),
                    qtyText = row.qtyText,
                    rateText = row.rateText,
                )
            },
        )

    private fun SheetDocument.toEntities(isDraft: Boolean): Pair<MeasurementSheetEntity, List<MeasurementRowEntity>> {
        val totals = this.totals
        val sheetEntity = MeasurementSheetEntity(
            isDraft = isDraft,
            sheetNo = sheetNo,
            partyName = partyName,
            businessName = businessName,
            dateText = dateText,
            itemCount = totals.itemCount,
            totalQty = totals.totalQty,
            totalRft = totals.totalRft,
            totalSqFt = totals.totalSqFt,
            totalAmount = totals.totalAmount,
            updatedAt = System.currentTimeMillis(),
        )
        val rowEntities = rows.mapIndexed { index, row ->
            MeasurementRowEntity(
                sheetId = 0,
                sortOrder = index,
                mode = row.mode.name,
                item = row.item,
                lengthText = row.lengthText,
                lengthUnit = row.lengthUnit.name,
                widthText = row.widthText,
                widthUnit = row.widthUnit.name,
                qtyText = row.qtyText,
                rateText = row.rateText,
            )
        }
        return sheetEntity to rowEntities
    }
}
