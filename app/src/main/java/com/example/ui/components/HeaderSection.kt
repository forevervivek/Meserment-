package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TabularNumericStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    partyName: String,
    businessName: String,
    dateText: String,
    sheetNo: String,
    onPartyNameChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onSheetNoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onDateChange(sdf.format(Date(millis)))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.close_label))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        BoxWithConstraints(modifier = Modifier.padding(12.dp)) {
            val isWide = maxWidth >= 600.dp

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HeaderField(
                        label = stringResource(R.string.party_name),
                        value = partyName,
                        onValueChange = onPartyNameChange,
                        testTag = "party_name_input",
                        modifier = Modifier.weight(1.3f),
                    )
                    HeaderField(
                        label = stringResource(R.string.business_name),
                        value = businessName,
                        onValueChange = onBusinessNameChange,
                        placeholder = stringResource(R.string.optional_hint),
                        testTag = "business_name_input",
                        modifier = Modifier.weight(1.2f),
                    )
                    HeaderField(
                        label = stringResource(R.string.date_label),
                        value = dateText,
                        onValueChange = onDateChange,
                        tabular = true,
                        testTag = "date_input",
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = stringResource(R.string.pick_date),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        modifier = Modifier.weight(0.95f),
                    )
                    HeaderField(
                        label = stringResource(R.string.sheet_no),
                        value = sheetNo,
                        onValueChange = onSheetNoChange,
                        tabular = true,
                        testTag = "sheet_no_input",
                        modifier = Modifier.weight(0.75f),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HeaderField(
                            label = stringResource(R.string.party_name),
                            value = partyName,
                            onValueChange = onPartyNameChange,
                            testTag = "party_name_input",
                            modifier = Modifier.weight(1f),
                        )
                        HeaderField(
                            label = stringResource(R.string.business_name),
                            value = businessName,
                            onValueChange = onBusinessNameChange,
                            placeholder = stringResource(R.string.optional_hint),
                            testTag = "business_name_input",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HeaderField(
                            label = stringResource(R.string.date_label),
                            value = dateText,
                            onValueChange = onDateChange,
                            tabular = true,
                            testTag = "date_input",
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = stringResource(R.string.pick_date),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.15f),
                        )
                        HeaderField(
                            label = stringResource(R.string.sheet_no),
                            value = sheetNo,
                            onValueChange = onSheetNoChange,
                            tabular = true,
                            testTag = "sheet_no_input",
                            modifier = Modifier.weight(0.85f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    tabular: Boolean = false,
    testTag: String = "",
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val textStyle = if (tabular) {
        TabularNumericStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    } else {
        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
        placeholder = if (placeholder != null) {
            { Text(text = placeholder, style = MaterialTheme.typography.bodyMedium) }
        } else {
            null
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        textStyle = textStyle,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        modifier = modifier.let { if (testTag.isNotEmpty()) it.testTag(testTag) else it },
    )
}
