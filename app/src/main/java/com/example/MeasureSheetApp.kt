package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.MeasurementRepository

class MeasureSheetApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: MeasurementRepository by lazy { MeasurementRepository(database.measurementDao()) }
}
