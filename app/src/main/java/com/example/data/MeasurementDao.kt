package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurement_sheets WHERE isDraft = 0 ORDER BY updatedAt DESC")
    fun getSavedSheets(): Flow<List<MeasurementSheetEntity>>

    @Transaction
    @Query("SELECT * FROM measurement_sheets WHERE isDraft = 1 LIMIT 1")
    suspend fun getDraftWithRows(): SheetWithRows?

    @Transaction
    @Query("SELECT * FROM measurement_sheets WHERE id = :sheetId LIMIT 1")
    suspend fun getSheetWithRows(sheetId: Long): SheetWithRows?

    @Query("SELECT sheetNo FROM measurement_sheets WHERE isDraft = 0")
    suspend fun getAllSavedSheetNumbers(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: MeasurementSheetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<MeasurementRowEntity>)

    @Query("DELETE FROM measurement_sheets WHERE isDraft = 1")
    suspend fun clearDrafts()

    @Query("DELETE FROM measurement_rows WHERE sheetId = :sheetId")
    suspend fun deleteRowsForSheet(sheetId: Long)

    @Query("DELETE FROM measurement_sheets WHERE id = :sheetId")
    suspend fun deleteSheetById(sheetId: Long)

    @Transaction
    suspend fun saveDraft(sheet: MeasurementSheetEntity, rows: List<MeasurementRowEntity>) {
        clearDrafts()
        val sheetId = insertSheet(sheet.copy(id = 0, isDraft = true))
        if (rows.isNotEmpty()) {
            insertRows(rows.mapIndexed { index, row -> row.copy(id = 0, sheetId = sheetId, sortOrder = index) })
        }
    }

    @Transaction
    suspend fun savePartySheet(
        existingSavedId: Long?,
        sheet: MeasurementSheetEntity,
        rows: List<MeasurementRowEntity>,
    ): Long {
        val targetId = if (existingSavedId != null) {
            deleteRowsForSheet(existingSavedId)
            insertSheet(sheet.copy(id = existingSavedId, isDraft = false))
            existingSavedId
        } else {
            insertSheet(sheet.copy(id = 0, isDraft = false))
        }
        if (rows.isNotEmpty()) {
            insertRows(rows.mapIndexed { index, row -> row.copy(id = 0, sheetId = targetId, sortOrder = index) })
        }
        return targetId
    }
}
