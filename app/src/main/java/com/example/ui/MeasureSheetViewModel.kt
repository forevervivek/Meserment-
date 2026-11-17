package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MeasurementRepository
import com.example.data.MeasurementSheetEntity
import com.example.domain.LengthUnit
import com.example.domain.MeasurementMode
import com.example.domain.MeasurementRow
import com.example.domain.SheetDocument
import com.example.domain.UnitConverter
import com.example.domain.todayDateString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasureSheetUiState(
    val document: SheetDocument = SheetDocument(),
    val editingSavedSheetId: Long? = null,
    val entryMode: MeasurementMode = MeasurementMode.SQFT,
    val entryItem: String = "",
    val entryLength: String = "",
    val entryLengthUnit: LengthUnit = LengthUnit.FT,
    val entryWidth: String = "",
    val entryWidthUnit: LengthUnit = LengthUnit.FT,
    val entryQty: String = "1",
    val entryRate: String = "",
    val savedSheets: List<MeasurementSheetEntity> = emptyList(),
    val showPreviewModal: Boolean = false,
    val showSavedModal: Boolean = false,
    val statusBanner: String? = null,
    val isLoaded: Boolean = false,
)

class MeasureSheetViewModel(
    private val repository: MeasurementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeasureSheetUiState())
    val uiState: StateFlow<MeasureSheetUiState> = _uiState.asStateFlow()

    private var nextRowId = 1L
    private var draftSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.savedSheets.collect { sheets ->
                _uiState.update { it.copy(savedSheets = sheets) }
            }
        }
        viewModelScope.launch {
            val draft = repository.loadDraft()
            if (draft != null) {
                val maxId = draft.rows.maxOfOrNull { it.id } ?: 0L
                nextRowId = maxId + 1L
                _uiState.update {
                    it.copy(
                        document = draft.copy(
                            dateText = draft.dateText.ifBlank { todayDateString() },
                            sheetNo = draft.sheetNo.ifBlank { "MS-001" },
                        ),
                        isLoaded = true,
                    )
                }
            } else {
                val nextSheetNo = repository.generateNextSheetNo()
                _uiState.update {
                    it.copy(
                        document = SheetDocument(
                            dateText = todayDateString(),
                            sheetNo = nextSheetNo,
                        ),
                        isLoaded = true,
                    )
                }
            }
        }
    }

    // Header mutations
    fun onPartyNameChange(value: String) = updateDocument { it.copy(partyName = value) }
    fun onBusinessNameChange(value: String) = updateDocument { it.copy(businessName = value) }
    fun onDateChange(value: String) = updateDocument { it.copy(dateText = value) }
    fun onSheetNoChange(value: String) = updateDocument { it.copy(sheetNo = value) }

    // Entry composer mutations
    fun onEntryModeChange(mode: MeasurementMode) {
        _uiState.update { it.copy(entryMode = mode) }
    }

    fun onEntryItemChange(value: String) {
        _uiState.update { it.copy(entryItem = value) }
    }

    fun onEntryLengthChange(value: String) {
        _uiState.update { it.copy(entryLength = sanitizeDecimal(value)) }
    }

    fun onEntryLengthUnitChange(newUnit: LengthUnit) {
        _uiState.update { state ->
            val converted = UnitConverter.convertValue(state.entryLength, state.entryLengthUnit, newUnit)
            state.copy(entryLength = converted, entryLengthUnit = newUnit)
        }
    }

    fun onEntryWidthChange(value: String) {
        _uiState.update { it.copy(entryWidth = sanitizeDecimal(value)) }
    }

    fun onEntryWidthUnitChange(newUnit: LengthUnit) {
        _uiState.update { state ->
            val converted = UnitConverter.convertValue(state.entryWidth, state.entryWidthUnit, newUnit)
            state.copy(entryWidth = converted, entryWidthUnit = newUnit)
        }
    }

    fun onEntryQtyChange(value: String) {
        _uiState.update { it.copy(entryQty = sanitizeDecimal(value)) }
    }

    fun onEntryRateChange(value: String) {
        _uiState.update { it.copy(entryRate = sanitizeDecimal(value)) }
    }

    fun addRowFromEntry() {
        val state = _uiState.value
        val hasInput = state.entryItem.isNotBlank() ||
            state.entryLength.isNotBlank() ||
            state.entryWidth.isNotBlank() ||
            state.entryRate.isNotBlank()
        if (!hasInput) return

        val newRow = MeasurementRow(
            id = nextRowId++,
            mode = state.entryMode,
            item = state.entryItem.trim(),
            lengthText = state.entryLength.trim(),
            lengthUnit = state.entryLengthUnit,
            widthText = if (state.entryMode == MeasurementMode.SQFT) state.entryWidth.trim() else "",
            widthUnit = state.entryWidthUnit,
            qtyText = state.entryQty.trim().ifEmpty { "1" },
            rateText = state.entryRate.trim(),
        )

        _uiState.update { current ->
            current.copy(
                document = current.document.copy(rows = current.document.rows + newRow),
                entryItem = "",
                entryLength = "",
                entryWidth = "",
                entryQty = "1",
                entryRate = "",
            )
        }
        scheduleDraftSave()
    }

    // Inline row mutations
    fun onRowModeChange(rowId: Long, mode: MeasurementMode) = updateRow(rowId) { row ->
        row.copy(mode = mode)
    }

    fun onRowItemChange(rowId: Long, item: String) = updateRow(rowId) { row ->
        row.copy(item = item)
    }

    fun onRowLengthChange(rowId: Long, length: String) = updateRow(rowId) { row ->
        row.copy(lengthText = sanitizeDecimal(length))
    }

    fun onRowLengthUnitChange(rowId: Long, newUnit: LengthUnit) = updateRow(rowId) { row ->
        val converted = UnitConverter.convertValue(row.lengthText, row.lengthUnit, newUnit)
        row.copy(lengthText = converted, lengthUnit = newUnit)
    }

    fun onRowWidthChange(rowId: Long, width: String) = updateRow(rowId) { row ->
        row.copy(widthText = sanitizeDecimal(width))
    }

    fun onRowWidthUnitChange(rowId: Long, newUnit: LengthUnit) = updateRow(rowId) { row ->
        val converted = UnitConverter.convertValue(row.widthText, row.widthUnit, newUnit)
        row.copy(widthText = converted, widthUnit = newUnit)
    }

    fun onRowQtyChange(rowId: Long, qty: String) = updateRow(rowId) { row ->
        row.copy(qtyText = sanitizeDecimal(qty))
    }

    fun onRowRateChange(rowId: Long, rate: String) = updateRow(rowId) { row ->
        row.copy(rateText = sanitizeDecimal(rate))
    }

    fun duplicateRow(rowId: Long) {
        updateDocument { doc ->
            val index = doc.rows.indexOfFirst { it.id == rowId }
            if (index < 0) return@updateDocument doc
            val copy = doc.rows[index].copy(id = nextRowId++)
            val mutable = doc.rows.toMutableList()
            mutable.add(index + 1, copy)
            doc.copy(rows = mutable)
        }
    }

    fun deleteRow(rowId: Long) {
        updateDocument { doc ->
            doc.copy(rows = doc.rows.filterNot { it.id == rowId })
        }
    }

    // Saved sheets & document lifecycle
    fun saveCurrentSheet() {
        val currentState = _uiState.value
        val doc = currentState.document
        viewModelScope.launch {
            val savedId = repository.savePartySheet(currentState.editingSavedSheetId, doc)
            _uiState.update {
                it.copy(
                    editingSavedSheetId = savedId,
                    statusBanner = "Saved",
                )
            }
        }
    }

    fun createNewSheet() {
        viewModelScope.launch {
            val nextNo = repository.generateNextSheetNo()
            val fresh = SheetDocument(
                partyName = "",
                businessName = _uiState.value.document.businessName,
                dateText = todayDateString(),
                sheetNo = nextNo,
                rows = emptyList(),
            )
            _uiState.update {
                it.copy(
                    document = fresh,
                    editingSavedSheetId = null,
                    entryItem = "",
                    entryLength = "",
                    entryWidth = "",
                    entryQty = "1",
                    entryRate = "",
                    statusBanner = "New",
                )
            }
            repository.saveDraft(fresh)
        }
    }

    fun loadSavedSheet(sheetId: Long) {
        viewModelScope.launch {
            val loaded = repository.loadSavedSheet(sheetId) ?: return@launch
            val indexed = loaded.rows.map { row ->
                row.copy(id = nextRowId++)
            }
            val updatedDoc = loaded.copy(rows = indexed)
            _uiState.update {
                it.copy(
                    document = updatedDoc,
                    editingSavedSheetId = sheetId,
                    showSavedModal = false,
                    statusBanner = updatedDoc.sheetNo,
                )
            }
            repository.saveDraft(updatedDoc)
        }
    }

    fun duplicateSavedSheet(sheetId: Long) {
        viewModelScope.launch {
            val loaded = repository.loadSavedSheet(sheetId) ?: return@launch
            val nextNo = repository.generateNextSheetNo()
            val duplicated = loaded.copy(
                sheetNo = nextNo,
                dateText = todayDateString(),
                rows = loaded.rows.map { it.copy(id = nextRowId++) },
            )
            val savedId = repository.savePartySheet(null, duplicated)
            _uiState.update {
                it.copy(
                    document = duplicated,
                    editingSavedSheetId = savedId,
                    showSavedModal = false,
                    statusBanner = nextNo,
                )
            }
            repository.saveDraft(duplicated)
        }
    }

    fun deleteSavedSheet(sheetId: Long) {
        viewModelScope.launch {
            repository.deleteSavedSheet(sheetId)
            _uiState.update { state ->
                if (state.editingSavedSheetId == sheetId) {
                    state.copy(editingSavedSheetId = null)
                } else {
                    state
                }
            }
        }
    }

    fun setShowPreviewModal(show: Boolean) {
        _uiState.update { it.copy(showPreviewModal = show) }
    }

    fun setShowSavedModal(show: Boolean) {
        _uiState.update { it.copy(showSavedModal = show) }
    }

    fun showStatus(message: String) {
        _uiState.update { it.copy(statusBanner = message) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    private fun updateDocument(transform: (SheetDocument) -> SheetDocument) {
        _uiState.update { state ->
            state.copy(document = transform(state.document))
        }
        scheduleDraftSave()
    }

    private fun updateRow(rowId: Long, transform: (MeasurementRow) -> MeasurementRow) {
        updateDocument { doc ->
            doc.copy(
                rows = doc.rows.map { row ->
                    if (row.id == rowId) transform(row) else row
                },
            )
        }
    }

    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(180L)
            repository.saveDraft(_uiState.value.document)
        }
    }

    private fun sanitizeDecimal(input: String): String {
        val sb = StringBuilder()
        var dotSeen = false
        for (ch in input) {
            if (ch.isDigit()) {
                sb.append(ch)
            } else if (ch == '.' && !dotSeen) {
                sb.append('.')
                dotSeen = true
            }
        }
        return sb.toString()
    }

    class Factory(private val repository: MeasurementRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MeasureSheetViewModel(repository) as T
        }
    }
}
