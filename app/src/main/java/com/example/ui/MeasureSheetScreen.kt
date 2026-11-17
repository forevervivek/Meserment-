package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.pdf.PdfGenerator
import com.example.ui.components.EntryComposerCard
import com.example.ui.components.HeaderSection
import com.example.ui.components.MeasurementTable
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.components.SavedSheetsModal
import com.example.ui.components.SummaryBottomBar
import com.example.ui.theme.BlueprintCyanLight
import kotlinx.coroutines.launch

@Composable
fun MeasureSheetScreen(
    viewModel: MeasureSheetViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = PdfGenerator.writePdfToUri(context, uri, uiState.document)
                if (result.isSuccess) {
                    viewModel.showStatus("PDF Saved")
                } else {
                    viewModel.showStatus("Export Failed")
                }
            }
        }
    }

    val launchDownloadPdf: () -> Unit = {
        createPdfLauncher.launch(PdfGenerator.suggestedFileName(uiState.document))
    }

    val launchSharePdf: () -> Unit = {
        scope.launch {
            val result = PdfGenerator.createShareablePdfFile(context, uiState.document)
            result.onSuccess { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, uiState.document.sheetNo)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
            }.onFailure {
                viewModel.showStatus("Share Failed")
            }
        }
    }

    LaunchedEffect(uiState.statusBanner) {
        val message = uiState.statusBanner
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatus()
        }
    }

    if (uiState.showPreviewModal) {
        PdfPreviewDialog(
            document = uiState.document,
            onDismiss = { viewModel.setShowPreviewModal(false) },
            onDownloadPdf = launchDownloadPdf,
            onSharePdf = launchSharePdf,
        )
    }

    if (uiState.showSavedModal) {
        SavedSheetsModal(
            savedSheets = uiState.savedSheets,
            onDismiss = { viewModel.setShowSavedModal(false) },
            onLoadSheet = viewModel::loadSavedSheet,
            onDuplicateSheet = viewModel::duplicateSavedSheet,
            onDeleteSheet = viewModel::deleteSavedSheet,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            WorkspaceTopBar(
                savedCount = uiState.savedSheets.size,
                onNewSheet = viewModel::createNewSheet,
                onSaveSheet = viewModel::saveCurrentSheet,
                onOpenSaved = { viewModel.setShowSavedModal(true) },
                onPreview = { viewModel.setShowPreviewModal(true) },
                onDownloadPdf = launchDownloadPdf,
            )
        },
        bottomBar = {
            SummaryBottomBar(totals = uiState.document.totals)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1120.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HeaderSection(
                    partyName = uiState.document.partyName,
                    businessName = uiState.document.businessName,
                    dateText = uiState.document.dateText,
                    sheetNo = uiState.document.sheetNo,
                    onPartyNameChange = viewModel::onPartyNameChange,
                    onBusinessNameChange = viewModel::onBusinessNameChange,
                    onDateChange = viewModel::onDateChange,
                    onSheetNoChange = viewModel::onSheetNoChange,
                )

                EntryComposerCard(
                    mode = uiState.entryMode,
                    item = uiState.entryItem,
                    length = uiState.entryLength,
                    lengthUnit = uiState.entryLengthUnit,
                    width = uiState.entryWidth,
                    widthUnit = uiState.entryWidthUnit,
                    qty = uiState.entryQty,
                    rate = uiState.entryRate,
                    onModeChange = viewModel::onEntryModeChange,
                    onItemChange = viewModel::onEntryItemChange,
                    onLengthChange = viewModel::onEntryLengthChange,
                    onLengthUnitChange = viewModel::onEntryLengthUnitChange,
                    onWidthChange = viewModel::onEntryWidthChange,
                    onWidthUnitChange = viewModel::onEntryWidthUnitChange,
                    onQtyChange = viewModel::onEntryQtyChange,
                    onRateChange = viewModel::onEntryRateChange,
                    onAddRow = viewModel::addRowFromEntry,
                )

                MeasurementTable(
                    rows = uiState.document.rows,
                    onRowModeChange = viewModel::onRowModeChange,
                    onRowItemChange = viewModel::onRowItemChange,
                    onRowLengthChange = viewModel::onRowLengthChange,
                    onRowLengthUnitChange = viewModel::onRowLengthUnitChange,
                    onRowWidthChange = viewModel::onRowWidthChange,
                    onRowWidthUnitChange = viewModel::onRowWidthUnitChange,
                    onRowQtyChange = viewModel::onRowQtyChange,
                    onRowRateChange = viewModel::onRowRateChange,
                    onDuplicateRow = viewModel::duplicateRow,
                    onDeleteRow = viewModel::deleteRow,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WorkspaceTopBar(
    savedCount: Int,
    onNewSheet: () -> Unit,
    onSaveSheet: () -> Unit,
    onOpenSaved: () -> Unit,
    onPreview: () -> Unit,
    onDownloadPdf: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                val isWide = maxWidth >= 680.dp

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BrandMarkTitle()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ToolbarActionsRow(
                                savedCount = savedCount,
                                onNewSheet = onNewSheet,
                                onSaveSheet = onSaveSheet,
                                onOpenSaved = onOpenSaved,
                                onPreview = onPreview,
                                onDownloadPdf = onDownloadPdf,
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BrandMarkTitle()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(
                                    onClick = onNewSheet,
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("new_sheet_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.NoteAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.new_sheet), fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = onOpenSaved,
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("saved_sheets_button"),
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (savedCount > 0) {
                                                Badge { Text(savedCount.toString()) }
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.BookmarkBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.saved_label), fontSize = 12.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = onSaveSheet,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("save_sheet_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(stringResource(R.string.save_label), fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onPreview,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("preview_pdf_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(stringResource(R.string.preview_label), fontSize = 12.sp)
                            }
                            Button(
                                onClick = onDownloadPdf,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("download_pdf_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(stringResource(R.string.download_pdf), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun ToolbarActionsRow(
    savedCount: Int,
    onNewSheet: () -> Unit,
    onSaveSheet: () -> Unit,
    onOpenSaved: () -> Unit,
    onPreview: () -> Unit,
    onDownloadPdf: () -> Unit,
) {
    TextButton(
        onClick = onNewSheet,
        modifier = Modifier
            .height(48.dp)
            .testTag("new_sheet_button"),
    ) {
        Icon(Icons.Outlined.NoteAdd, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(stringResource(R.string.new_sheet))
    }
    TextButton(
        onClick = onOpenSaved,
        modifier = Modifier
            .height(48.dp)
            .testTag("saved_sheets_button"),
    ) {
        BadgedBox(
            badge = {
                if (savedCount > 0) {
                    Badge { Text(savedCount.toString()) }
                }
            },
        ) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.saved_label))
    }
    OutlinedButton(
        onClick = onSaveSheet,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(48.dp)
            .testTag("save_sheet_button"),
    ) {
        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(stringResource(R.string.save_label))
    }
    OutlinedButton(
        onClick = onPreview,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(48.dp)
            .testTag("preview_pdf_button"),
    ) {
        Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(stringResource(R.string.preview_label))
    }
    Button(
        onClick = onDownloadPdf,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .height(48.dp)
            .testTag("download_pdf_button"),
    ) {
        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(stringResource(R.string.download_pdf))
    }
}

@Composable
private fun BrandMarkTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val badgeColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(badgeColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val ruler = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.78f)
                    lineTo(size.width * 0.78f, size.height * 0.78f)
                    lineTo(size.width * 0.78f, size.height * 0.15f)
                }
                drawPath(
                    path = ruler,
                    color = BlueprintCyanLight,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawLine(
                    color = androidx.compose.ui.graphics.Color.White,
                    start = Offset(size.width * 0.25f, size.height * 0.28f),
                    end = Offset(size.width * 0.55f, size.height * 0.28f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = androidx.compose.ui.graphics.Color.White,
                    start = Offset(size.width * 0.25f, size.height * 0.28f),
                    end = Offset(size.width * 0.25f, size.height * 0.55f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
