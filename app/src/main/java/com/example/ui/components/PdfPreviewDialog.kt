package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.domain.MeasurementMode
import com.example.domain.NumberFormatters
import com.example.domain.SheetDocument
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintNavy
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TabularNumericStyle

@Composable
fun PdfPreviewDialog(
    document: SheetDocument,
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${stringResource(R.string.preview_label)} • A4",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_preview_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.close_label),
                        )
                    }
                }

                // A4 Page Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f / 1.4142f),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    ) {
                        A4SheetContent(document = document)
                    }
                }

                // Modal Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onSharePdf,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("preview_share_pdf_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.share_pdf))
                        }
                        Button(
                            onClick = onDownloadPdf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("preview_download_pdf_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.download_pdf))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun A4SheetContent(document: SheetDocument) {
    val totals = document.totals
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Document Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.businessName.trim().ifEmpty { "MeasureSheet" },
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BlueprintNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "MEASUREMENT SHEET",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = BlueprintCyan,
                )
            }

            Column(
                modifier = Modifier
                    .background(Color(0xFFF4F7FA), RoundedCornerShape(4.dp))
                    .border(0.75.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = stringResource(R.string.sheet_no),
                            fontSize = 8.sp,
                            color = Color(0xFF475569),
                        )
                        Text(
                            text = document.sheetNo.ifBlank { "MS-001" },
                            style = TabularNumericStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.date_label),
                            fontSize = 8.sp,
                            color = Color(0xFF475569),
                        )
                        Text(
                            text = document.dateText.ifBlank { "-" },
                            style = TabularNumericStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Party Name bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F7FA), RoundedCornerShape(4.dp))
                .border(0.75.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${stringResource(R.string.party_name)}:",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
            )
            Text(
                text = document.partyName.trim().ifEmpty { "—" },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // A4 Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintNavy)
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            A4Cell("#", 0.35f, TextAlign.Start, Color.White, bold = true)
            A4Cell("Item", 1.3f, TextAlign.Start, Color.White, bold = true)
            A4Cell("Mode", 0.65f, TextAlign.Start, Color.White, bold = true)
            A4Cell("Size", 1.3f, TextAlign.Start, Color.White, bold = true)
            A4Cell("Qty", 0.55f, TextAlign.End, Color.White, bold = true)
            A4Cell("Rate", 0.7f, TextAlign.End, Color.White, bold = true)
            A4Cell("RFT", 0.7f, TextAlign.End, Color.White, bold = true)
            A4Cell("Sq.Ft", 0.75f, TextAlign.End, Color.White, bold = true)
            A4Cell("Amount", 0.9f, TextAlign.End, Color.White, bold = true)
        }

        // Rows
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(0.6.dp, Color(0xFFCBD5E1)),
        ) {
            document.rows.forEachIndexed { idx, row ->
                val bg = if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White
                val sizeText = if (row.mode == MeasurementMode.SQFT) {
                    "${row.lengthText.ifBlank { "0" }} ${row.lengthUnit.label} × ${row.widthText.ifBlank { "0" }} ${row.widthUnit.label}"
                } else {
                    "${row.lengthText.ifBlank { "0" }} ${row.lengthUnit.label}"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    A4Cell("${idx + 1}", 0.35f, TextAlign.Start, Color(0xFF0F172A), numeric = true)
                    A4Cell(row.item.ifBlank { "-" }, 1.3f, TextAlign.Start, Color(0xFF0F172A))
                    A4Cell(row.mode.label, 0.65f, TextAlign.Start, Color(0xFF475569))
                    A4Cell(sizeText, 1.3f, TextAlign.Start, Color(0xFF0F172A), numeric = true)
                    A4Cell(NumberFormatters.formatQty(row.qtyNum), 0.55f, TextAlign.End, Color(0xFF0F172A), numeric = true)
                    A4Cell(NumberFormatters.formatRate(row.rateNum), 0.7f, TextAlign.End, Color(0xFF0F172A), numeric = true)
                    A4Cell(
                        if (row.mode == MeasurementMode.RFT) NumberFormatters.formatDecimal(row.rftValue) else "—",
                        0.7f,
                        TextAlign.End,
                        Color(0xFF0F172A),
                        numeric = true,
                    )
                    A4Cell(
                        if (row.mode == MeasurementMode.SQFT) NumberFormatters.formatDecimal(row.sqFtValue) else "—",
                        0.75f,
                        TextAlign.End,
                        Color(0xFF0F172A),
                        numeric = true,
                    )
                    A4Cell(NumberFormatters.formatDecimal(row.amountValue), 0.9f, TextAlign.End, Color(0xFF0F172A), bold = true, numeric = true)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grand Totals Footer Block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintNavy, RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            A4TotalItem(stringResource(R.string.items_total), totals.itemCount.toString())
            A4TotalItem(stringResource(R.string.qty_total), NumberFormatters.formatQty(totals.totalQty))
            A4TotalItem(stringResource(R.string.rft_total), NumberFormatters.formatDecimal(totals.totalRft))
            A4TotalItem(stringResource(R.string.sqft_total), NumberFormatters.formatDecimal(totals.totalSqFt))
            A4TotalItem(stringResource(R.string.amount_total), NumberFormatters.formatDecimal(totals.totalAmount))
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.A4Cell(
    text: String,
    weight: Float,
    align: TextAlign,
    color: Color,
    bold: Boolean = false,
    numeric: Boolean = false,
) {
    Text(
        text = text,
        style = if (numeric) {
            TabularNumericStyle.copy(
                fontSize = 8.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            )
        } else {
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 8.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            )
        },
        color = color,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun A4TotalItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 7.sp,
            color = Color(0xFF93C5FD),
            maxLines = 1,
        )
        Text(
            text = value,
            style = TabularNumericStyle.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
        )
    }
}
