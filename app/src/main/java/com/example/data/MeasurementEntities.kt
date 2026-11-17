package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "measurement_sheets")
data class MeasurementSheetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isDraft: Boolean = false,
    val sheetNo: String,
    val partyName: String,
    val businessName: String,
    val dateText: String,
    val itemCount: Int,
    val totalQty: Double,
    val totalRft: Double,
    val totalSqFt: Double,
    val totalAmount: Double,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "measurement_rows",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementSheetEntity::class,
            parentColumns = ["id"],
            childColumns = ["sheetId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sheetId")],
)
data class MeasurementRowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sheetId: Long,
    val sortOrder: Int,
    val mode: String,
    val item: String,
    val lengthText: String,
    val lengthUnit: String,
    val widthText: String,
    val widthUnit: String,
    val qtyText: String,
    val rateText: String,
)

data class SheetWithRows(
    @Embedded val sheet: MeasurementSheetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sheetId",
    )
    val rows: List<MeasurementRowEntity>,
)
