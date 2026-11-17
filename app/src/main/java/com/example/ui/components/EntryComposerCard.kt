package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.LengthUnit
import com.example.domain.MeasurementMode
import com.example.ui.theme.TabularNumericStyle

@Composable
fun EntryComposerCard(
    mode: MeasurementMode,
    item: String,
    length: String,
    lengthUnit: LengthUnit,
    width: String,
    widthUnit: LengthUnit,
    qty: String,
    rate: String,
    onModeChange: (MeasurementMode) -> Unit,
    onItemChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onLengthUnitChange: (LengthUnit) -> Unit,
    onWidthChange: (String) -> Unit,
    onWidthUnitChange: (LengthUnit) -> Unit,
    onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onAddRow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enterModifier = Modifier.onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown &&
            (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
        ) {
            onAddRow()
            true
        } else {
            false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(enterModifier),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        BoxWithConstraints(modifier = Modifier.padding(12.dp)) {
            val isWide = maxWidth >= 760.dp
            val widthEnabled = mode == MeasurementMode.SQFT

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    ModeSelectorBlock(
                        mode = mode,
                        onModeChange = onModeChange,
                        modifier = Modifier.width(126.dp),
                    )
                    CompactLabeledInput(
                        label = stringResource(R.string.item_label),
                        value = item,
                        onValueChange = onItemChange,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        onImeAction = onAddRow,
                        testTag = "entry_item_input",
                        modifier = Modifier.weight(1.3f),
                    )
                    PhotoshopUnitField(
                        label = stringResource(R.string.length_label),
                        value = length,
                        unit = lengthUnit,
                        onValueChange = onLengthChange,
                        onUnitChange = onLengthUnitChange,
                        imeAction = ImeAction.Next,
                        onImeAction = onAddRow,
                        inputTestTag = "entry_length_input",
                        unitTestTag = "entry_length_unit",
                        modifier = Modifier.weight(1.1f),
                    )
                    PhotoshopUnitField(
                        label = stringResource(R.string.width_label),
                        value = width,
                        unit = widthUnit,
                        onValueChange = onWidthChange,
                        onUnitChange = onWidthUnitChange,
                        enabled = widthEnabled,
                        imeAction = ImeAction.Next,
                        onImeAction = onAddRow,
                        inputTestTag = "entry_width_input",
                        unitTestTag = "entry_width_unit",
                        modifier = Modifier.weight(1.1f),
                    )
                    CompactLabeledInput(
                        label = stringResource(R.string.qty_label),
                        value = qty,
                        onValueChange = onQtyChange,
                        keyboardType = KeyboardType.Decimal,
                        tabular = true,
                        imeAction = ImeAction.Next,
                        onImeAction = onAddRow,
                        testTag = "entry_qty_input",
                        modifier = Modifier.width(72.dp),
                    )
                    CompactLabeledInput(
                        label = stringResource(R.string.rate_label),
                        value = rate,
                        onValueChange = onRateChange,
                        keyboardType = KeyboardType.Decimal,
                        tabular = true,
                        imeAction = ImeAction.Done,
                        onImeAction = onAddRow,
                        testTag = "entry_rate_input",
                        modifier = Modifier.width(88.dp),
                    )
                    AddRowButton(onClick = onAddRow)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        ModeSelectorBlock(
                            mode = mode,
                            onModeChange = onModeChange,
                            modifier = Modifier.width(124.dp),
                        )
                        CompactLabeledInput(
                            label = stringResource(R.string.item_label),
                            value = item,
                            onValueChange = onItemChange,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onImeAction = onAddRow,
                            testTag = "entry_item_input",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PhotoshopUnitField(
                            label = stringResource(R.string.length_label),
                            value = length,
                            unit = lengthUnit,
                            onValueChange = onLengthChange,
                            onUnitChange = onLengthUnitChange,
                            imeAction = ImeAction.Next,
                            onImeAction = onAddRow,
                            inputTestTag = "entry_length_input",
                            unitTestTag = "entry_length_unit",
                            modifier = Modifier.weight(1f),
                        )
                        PhotoshopUnitField(
                            label = stringResource(R.string.width_label),
                            value = width,
                            unit = widthUnit,
                            onValueChange = onWidthChange,
                            onUnitChange = onWidthUnitChange,
                            enabled = widthEnabled,
                            imeAction = ImeAction.Next,
                            onImeAction = onAddRow,
                            inputTestTag = "entry_width_input",
                            unitTestTag = "entry_width_unit",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        CompactLabeledInput(
                            label = stringResource(R.string.qty_label),
                            value = qty,
                            onValueChange = onQtyChange,
                            keyboardType = KeyboardType.Decimal,
                            tabular = true,
                            imeAction = ImeAction.Next,
                            onImeAction = onAddRow,
                            testTag = "entry_qty_input",
                            modifier = Modifier.weight(0.75f),
                        )
                        CompactLabeledInput(
                            label = stringResource(R.string.rate_label),
                            value = rate,
                            onValueChange = onRateChange,
                            keyboardType = KeyboardType.Decimal,
                            tabular = true,
                            imeAction = ImeAction.Done,
                            onImeAction = onAddRow,
                            testTag = "entry_rate_input",
                            modifier = Modifier.weight(1f),
                        )
                        AddRowButton(onClick = onAddRow)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorBlock(
    mode: MeasurementMode,
    onModeChange: (MeasurementMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.mode_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MeasurementMode.entries.forEach { option ->
                val selected = option == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .clickable { onModeChange(option) }
                        .testTag("entry_mode_${option.name.lowercase()}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactLabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
    tabular: Boolean = false,
    testTag: String = "",
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    val borderColor = if (isFocused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(if (isFocused) 1.5.dp else 1.dp, borderColor, shape)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty() && tabular) {
                Text(
                    text = "0",
                    style = TabularNumericStyle.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    ),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = if (tabular) {
                    TabularNumericStyle.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onImeAction() },
                    onNext = { onImeAction() },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .let { if (testTag.isNotEmpty()) it.testTag(testTag) else it },
            )
        }
    }
}

@Composable
private fun AddRowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .defaultMinSize(minWidth = 88.dp, minHeight = 48.dp)
            .height(48.dp)
            .testTag("add_row_button"),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.add_row),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}
