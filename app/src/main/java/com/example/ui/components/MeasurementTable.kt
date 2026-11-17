package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.LengthUnit
import com.example.domain.MeasurementMode
import com.example.domain.MeasurementRow
import com.example.domain.NumberFormatters
import com.example.ui.theme.TabularNumericStyle

private val ColIndexWidth = 34.dp
private val ColModeWidth = 90.dp
private val ColItemWidth = 132.dp
private val ColLengthWidth = 132.dp
private val ColWidthWidth = 132.dp
private val ColQtyWidth = 68.dp
private val ColRateWidth = 84.dp
private val ColRftWidth = 76.dp
private val ColSqFtWidth = 80.dp
private val ColAmountWidth = 96.dp
private val ColActionsWidth = 96.dp

@Composable
fun MeasurementTable(
    rows: List<MeasurementRow>,
    onRowModeChange: (Long, MeasurementMode) -> Unit,
    onRowItemChange: (Long, String) -> Unit,
    onRowLengthChange: (Long, String) -> Unit,
    onRowLengthUnitChange: (Long, LengthUnit) -> Unit,
    onRowWidthChange: (Long, String) -> Unit,
    onRowWidthUnitChange: (Long, LengthUnit) -> Unit,
    onRowQtyChange: (Long, String) -> Unit,
    onRowRateChange: (Long, String) -> Unit,
    onDuplicateRow: (Long) -> Unit,
    onDeleteRow: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        if (rows.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Straighten,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.no_rows),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hScroll),
            ) {
                TableHeaderRow()
                rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    MeasurementEditableRow(
                        rowIndex = index + 1,
                        row = row,
                        shaded = index % 2 == 1,
                        onModeChange = { onRowModeChange(row.id, it) },
                        onItemChange = { onRowItemChange(row.id, it) },
                        onLengthChange = { onRowLengthChange(row.id, it) },
                        onLengthUnitChange = { onRowLengthUnitChange(row.id, it) },
                        onWidthChange = { onRowWidthChange(row.id, it) },
                        onWidthUnitChange = { onRowWidthUnitChange(row.id, it) },
                        onQtyChange = { onRowQtyChange(row.id, it) },
                        onRateChange = { onRowRateChange(row.id, it) },
                        onDuplicate = { onDuplicateRow(row.id) },
                        onDelete = { onDeleteRow(row.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HeaderCell("#", ColIndexWidth, TextAlign.Center)
        HeaderCell(stringResource(R.string.mode_label), ColModeWidth, TextAlign.Center)
        HeaderCell(stringResource(R.string.item_label), ColItemWidth, TextAlign.Start)
        HeaderCell(stringResource(R.string.length_label), ColLengthWidth, TextAlign.Start)
        HeaderCell(stringResource(R.string.width_label), ColWidthWidth, TextAlign.Start)
        HeaderCell(stringResource(R.string.qty_label), ColQtyWidth, TextAlign.End)
        HeaderCell(stringResource(R.string.rate_label), ColRateWidth, TextAlign.End)
        HeaderCell(stringResource(R.string.rft_label), ColRftWidth, TextAlign.End)
        HeaderCell(stringResource(R.string.sqft_label), ColSqFtWidth, TextAlign.End)
        HeaderCell(stringResource(R.string.amount_label), ColAmountWidth, TextAlign.End)
        Spacer(modifier = Modifier.width(ColActionsWidth))
    }
}

@Composable
private fun HeaderCell(
    text: String,
    width: Dp,
    align: TextAlign,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = align,
        maxLines = 1,
        modifier = Modifier.width(width),
    )
}

@Composable
private fun MeasurementEditableRow(
    rowIndex: Int,
    row: MeasurementRow,
    shaded: Boolean,
    onModeChange: (MeasurementMode) -> Unit,
    onItemChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onLengthUnitChange: (LengthUnit) -> Unit,
    onWidthChange: (String) -> Unit,
    onWidthUnitChange: (LengthUnit) -> Unit,
    onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val bgColor = if (shaded) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("table_row_${row.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = rowIndex.toString(),
            style = TabularNumericStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(ColIndexWidth),
        )

        // Inline Mode Toggle Pill
        Row(
            modifier = Modifier
                .width(ColModeWidth)
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(2.dp),
        ) {
            MeasurementMode.entries.forEach { option ->
                val selected = option == row.mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .clickable { onModeChange(option) }
                        .testTag("row_mode_${row.id}_${option.name.lowercase()}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        // Inline Item Cell
        InlineCellTextField(
            value = row.item,
            onValueChange = onItemChange,
            width = ColItemWidth,
            keyboardType = KeyboardType.Text,
            tabular = false,
            textAlign = TextAlign.Start,
            testTag = "row_item_${row.id}",
        )

        // Inline Length + Photoshop Unit
        PhotoshopUnitField(
            value = row.lengthText,
            unit = row.lengthUnit,
            onValueChange = onLengthChange,
            onUnitChange = onLengthUnitChange,
            compact = true,
            inputTestTag = "row_length_${row.id}",
            unitTestTag = "row_length_unit_${row.id}",
            modifier = Modifier.width(ColLengthWidth),
        )

        // Inline Width + Photoshop Unit (disabled in RFT)
        PhotoshopUnitField(
            value = row.widthText,
            unit = row.widthUnit,
            onValueChange = onWidthChange,
            onUnitChange = onWidthUnitChange,
            enabled = row.mode == MeasurementMode.SQFT,
            compact = true,
            inputTestTag = "row_width_${row.id}",
            unitTestTag = "row_width_unit_${row.id}",
            modifier = Modifier.width(ColWidthWidth),
        )

        // Inline Qty
        InlineCellTextField(
            value = row.qtyText,
            onValueChange = onQtyChange,
            width = ColQtyWidth,
            keyboardType = KeyboardType.Decimal,
            tabular = true,
            textAlign = TextAlign.End,
            testTag = "row_qty_${row.id}",
        )

        // Inline Rate
        InlineCellTextField(
            value = row.rateText,
            onValueChange = onRateChange,
            width = ColRateWidth,
            keyboardType = KeyboardType.Decimal,
            tabular = true,
            textAlign = TextAlign.End,
            testTag = "row_rate_${row.id}",
        )

        // Real-time RFT
        Text(
            text = if (row.mode == MeasurementMode.RFT) NumberFormatters.formatDecimal(row.rftValue) else "—",
            style = TabularNumericStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = if (row.mode == MeasurementMode.RFT) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(ColRftWidth),
        )

        // Real-time Sq.Ft
        Text(
            text = if (row.mode == MeasurementMode.SQFT) NumberFormatters.formatDecimal(row.sqFtValue) else "—",
            style = TabularNumericStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = if (row.mode == MeasurementMode.SQFT) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(ColSqFtWidth),
        )

        // Real-time Amount
        Text(
            text = NumberFormatters.formatDecimal(row.amountValue),
            style = TabularNumericStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .width(ColAmountWidth)
                .testTag("row_amount_${row.id}"),
        )

        // Duplicate & Delete Row Actions (48dp touch targets)
        Row(
            modifier = Modifier.width(ColActionsWidth),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("duplicate_row_${row.id}"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = stringResource(R.string.duplicate_row),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_row_${row.id}"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.delete_row),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun InlineCellTextField(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    keyboardType: KeyboardType,
    tabular: Boolean,
    textAlign: TextAlign,
    testTag: String,
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isFocused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (isFocused) 1.5.dp else 1.dp, borderColor, shape)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = if (tabular) {
                TabularNumericStyle.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = textAlign,
                )
            } else {
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = textAlign,
                )
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .testTag(testTag),
        )
    }
}
