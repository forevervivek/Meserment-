package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.LengthUnit
import com.example.ui.theme.TabularNumericStyle

@Composable
fun PhotoshopUnitField(
    value: String,
    unit: LengthUnit,
    onValueChange: (String) -> Unit,
    onUnitChange: (LengthUnit) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    inputTestTag: String = "",
    unitTestTag: String = "",
) {
    var isFocused by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outlineVariant
        isFocused -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    val containerBg = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val shape = RoundedCornerShape(if (compact) 8.dp else 10.dp)
    val unitDesc = stringResource(R.string.cd_unit_selector)

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 44.dp else 48.dp)
                .clip(shape)
                .background(containerBg)
                .border(width = if (isFocused) 1.5.dp else 1.dp, color = borderColor, shape = shape)
                .alpha(if (enabled) 1f else 0.6f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = if (compact) 8.dp else 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = if (enabled) "0" else "—",
                        style = TabularNumericStyle.copy(
                            fontSize = if (compact) 13.sp else 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        ),
                    )
                }
                BasicTextField(
                    value = if (enabled) value else "",
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TabularNumericStyle.copy(
                        fontSize = if (compact) 13.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = imeAction,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onImeAction() },
                        onNext = { onImeAction() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .let { if (inputTestTag.isNotEmpty()) it.testTag(inputTestTag) else it },
                )
            }

            VerticalDivider(
                modifier = Modifier.height(if (compact) 22.dp else 26.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Box {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .clickable(enabled = enabled) { menuExpanded = true }
                        .padding(horizontal = if (compact) 6.dp else 8.dp)
                        .semantics { contentDescription = unitDesc }
                        .let { if (unitTestTag.isNotEmpty()) it.testTag(unitTestTag) else it },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = unit.label,
                        style = TabularNumericStyle.copy(
                            fontSize = if (compact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    LengthUnit.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    style = TabularNumericStyle.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (option == unit) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (option == unit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onUnitChange(option)
                            },
                        )
                    }
                }
            }
        }
    }
}
