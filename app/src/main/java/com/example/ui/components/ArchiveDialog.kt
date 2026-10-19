package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EnvelopeEntity
import com.example.data.model.TransactionEntity
import com.example.ui.CourierViewModel
import com.example.ui.theme.CourierOrange
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SoftRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Calendar

val MONTH_NAMES = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)

val YEARS = listOf(2026, 2025, 2024)

@Composable
fun ArchiveDialog(
    transactions: List<TransactionEntity>,
    envelopes: List<EnvelopeEntity>,
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    val currentCal = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedMonthIndex by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) }

    val envelopeMap = remember(envelopes) { envelopes.associateBy { it.id } }

    // Filter transactions for selected year and month
    val filteredTransactions = remember(transactions, selectedYear, selectedMonthIndex) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonthIndex
        }
    }

    val periodIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
    val periodExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    val periodNet = periodIncome - periodExpense

    val periodDeductibleVat = filteredTransactions.filter { !it.isIncome }.sumOf { tx ->
        val env = tx.envelopeId?.let { envelopeMap[it] }
        if (env?.type == "COMPANY") tx.vatAmount else 0.0
    }

    val periodCompanyExpense = filteredTransactions.filter { !it.isIncome }.sumOf { tx ->
        val env = tx.envelopeId?.let { envelopeMap[it] }
        if (env?.type == "COMPANY") tx.amount else 0.0
    }

    val periodPersonalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { tx ->
        val env = tx.envelopeId?.let { envelopeMap[it] }
        if (env?.type != "COMPANY") tx.amount else 0.0
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(28.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Aylık & Yıllık Arşiv",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_archive_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Year selector
                Text("Yıl Seçimi", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YEARS.forEach { year ->
                        val isSelected = selectedYear == year
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CourierOrange else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) CourierOrange else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedYear = year }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = year.toString(),
                                color = if (isSelected) PureBlack else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month selector (Horizontal scroll)
                Text("Ay Seçimi", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MONTH_NAMES.indices.toList()) { index ->
                        val isSelected = selectedMonthIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CourierOrange else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) CourierOrange else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedMonthIndex = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = MONTH_NAMES[index],
                                color = if (isSelected) PureBlack else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Period Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "${MONTH_NAMES[selectedMonthIndex]} $selectedYear Dönemi",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = CourierViewModel.formatCurrency(periodNet),
                            color = if (periodNet >= 0) NeonGreen else SoftRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Dönem Net Kalan Cüzdan Bakiyesi",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Toplam Hakediş (Gelir):", color = TextSecondary, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(periodIncome), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Toplam Harcama (Gider):", color = TextSecondary, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(periodExpense), color = SoftRed, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Şirket Giderleri:", color = TextSecondary, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(periodCompanyExpense), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kişisel Harcamalar:", color = TextSecondary, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(periodPersonalExpense), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("İndirilecek KDV (Tasarruf):", color = NeonGreen, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(periodDeductibleVat), color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Export buttons
                Text("Dışa Aktarma & Yedekleme", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExportCsv,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_csv_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = PureBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV / Excel Olarak Dışa Aktar", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExportJson,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_json_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = CourierOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verileri Yedekle (JSON)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}
