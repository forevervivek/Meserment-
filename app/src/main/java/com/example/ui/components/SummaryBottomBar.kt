package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.NumberFormatters
import com.example.domain.SheetTotals
import com.example.ui.theme.TabularNumericStyle

@Composable
fun SummaryBottomBar(
    totals: SheetTotals,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                val isWide = maxWidth >= 560.dp
                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { if (!isWide) it.horizontalScroll(scrollState) else it },
                    horizontalArrangement = if (isWide) Arrangement.SpaceBetween else Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SummaryMetric(
                        label = stringResource(R.string.items_total),
                        value = totals.itemCount.toString(),
                        testTag = "total_items",
                    )
                    SummaryMetric(
                        label = stringResource(R.string.qty_total),
                        value = NumberFormatters.formatQty(totals.totalQty),
                        testTag = "total_qty",
                    )
                    SummaryMetric(
                        label = stringResource(R.string.rft_total),
                        value = NumberFormatters.formatDecimal(totals.totalRft),
                        testTag = "total_rft",
                    )
                    SummaryMetric(
                        label = stringResource(R.string.sqft_total),
                        value = NumberFormatters.formatDecimal(totals.totalSqFt),
                        testTag = "total_sqft",
                    )
                    SummaryMetric(
                        label = stringResource(R.string.amount_total),
                        value = NumberFormatters.formatDecimal(totals.totalAmount),
                        highlight = true,
                        testTag = "total_amount",
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    testTag: String,
    highlight: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
            maxLines = 1,
        )
        Text(
            text = value,
            style = TabularNumericStyle.copy(
                fontSize = if (highlight) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = if (highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .testTag(testTag),
        )
    }
}
