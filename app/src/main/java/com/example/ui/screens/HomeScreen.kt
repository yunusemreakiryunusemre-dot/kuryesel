package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EnvelopeEntity
import com.example.data.model.TransactionEntity
import com.example.ui.CourierSummary
import com.example.ui.CourierViewModel
import com.example.ui.theme.CourierOrange
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SoftRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    summary: CourierSummary,
    courierName: String,
    recentTransactions: List<TransactionEntity>,
    envelopes: List<EnvelopeEntity>,
    onOpenSettings: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenScanReceipt: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenVatCalculator: () -> Unit,
    onAddQuickTip: (Double) -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val envelopeMap = envelopes.associateBy { it.id }
    var showCustomTipDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Bar: Greeting & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selam $courierName,",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Motosikletli Esnaf Cüzdanın",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onOpenArchive,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, CircleShape)
                            .testTag("archive_icon_button")
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Arşiv / Geçmiş",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, CircleShape)
                            .testTag("settings_icon_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 1. Giant Net Balance Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "NET CÜZDANDA KALAN TOPLAM",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CourierViewModel.formatCurrency(summary.netBalance),
                        color = if (summary.netBalance >= 0) NeonGreen else SoftRed,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 46.sp,
                        modifier = Modifier.testTag("net_balance_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // VAT Excluded & Declaration countdown pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KDV Hariç Matrah Kalanı",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = CourierViewModel.formatCurrency(summary.vatExcludedBalance),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CourierOrange.copy(alpha = 0.15f))
                                    .border(1.dp, CourierOrange, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Beyannameye ${summary.daysUntilDeclaration} Gün",
                                    color = CourierOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "İndirilecek KDV (Vergi Tasarrufu):",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = CourierViewModel.formatCurrency(summary.deductibleVat),
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 2. Dual Wallet Architecture Cards (Cepteki Nakit Kasa & Şirket Hak Ediş Alacağı)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card A: Cepteki Nakit Kasa (Fiziksel Nakit)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.2.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .testTag("pocket_cash_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cepteki Nakit",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = CourierViewModel.formatCurrency(summary.pocketCash),
                            color = NeonGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Eldeki Fiziksel Para",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Card B: Şirket Hak Ediş Alacağı (Firma İçerideki Kazanç)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.2.dp, CourierOrange.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .testTag("company_receivable_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = CourierOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hak Ediş Alacağı",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = CourierViewModel.formatCurrency(summary.companyReceivable),
                            color = CourierOrange,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Firmada Bekleyen",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. Center 3 Circular Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Fiş Tara (📷)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("scan_receipt_button_action")
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, DarkBorder, CircleShape)
                            .clickable { onOpenScanReceipt() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Fiş Tara",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fiş Tara",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Button 2: Big Orange İşlem Ekle (+)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("add_transaction_fab_action")
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(12.dp, CircleShape, spotColor = CourierOrange)
                            .clip(CircleShape)
                            .background(CourierOrange)
                            .clickable { onOpenAddTransaction() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "İşlem Ekle",
                            tint = PureBlack,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "İşlem Ekle",
                        color = CourierOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Button 3: KDV Hesapla (🧮)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("vat_calculator_button_action")
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, DarkBorder, CircleShape)
                            .clickable { onOpenVatCalculator() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "KDV Hesapla",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "KDV Hesapla",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. Quick Tip (Bahşiş) Buttons Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hızlı Bahşiş Ekle (Cepteki Nakit)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20.0, 50.0, 100.0).forEach { tipAmount ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { onAddQuickTip(tipAmount) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+₺${tipAmount.toInt()}",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Custom tip chip
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CourierOrange.copy(alpha = 0.15f))
                                .border(1.dp, CourierOrange, RoundedCornerShape(12.dp))
                                .clickable { showCustomTipDialog = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Özel",
                                color = CourierOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Section Title: Son İşlemler (Son 5)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son Eklenen İşlemler",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Son 5 Kayıt",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        }

        // Transaction list items
        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz işlem eklenmedi",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ortadaki turuncu (+) ile Hak Ediş, Kapıda Nakit Tahsilat veya Benzin Fişi ekleyin!",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                val env = tx.envelopeId?.let { envelopeMap[it] }
                val envColor = try {
                    if (env != null) Color(android.graphics.Color.parseColor(env.colorHex)) else CourierOrange
                } catch (_: Exception) {
                    CourierOrange
                }
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("tr-TR"))
                val dateStr = sdf.format(Date(tx.timestamp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (tx.transactionType) {
                                            "HAKEDIS" -> CourierOrange.copy(alpha = 0.2f)
                                            "CASH_COLLECTION" -> NeonGreen.copy(alpha = 0.2f)
                                            "TIP" -> PastelPurple.copy(alpha = 0.2f)
                                            "HAKEDIS_PAYOUT" -> CourierOrange.copy(alpha = 0.3f)
                                            else -> envColor.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (tx.transactionType) {
                                        "HAKEDIS" -> Icons.Default.Business
                                        "CASH_COLLECTION" -> Icons.Default.Payments
                                        "TIP" -> Icons.Default.AutoAwesome
                                        "HAKEDIS_PAYOUT" -> Icons.Default.Payments
                                        else -> Icons.AutoMirrored.Filled.TrendingDown
                                    },
                                    contentDescription = null,
                                    tint = when (tx.transactionType) {
                                        "HAKEDIS" -> CourierOrange
                                        "CASH_COLLECTION" -> NeonGreen
                                        "TIP" -> PastelPurple
                                        "HAKEDIS_PAYOUT" -> CourierOrange
                                        else -> envColor
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Transaction type badge
                                    val typeTag = when (tx.transactionType) {
                                        "HAKEDIS" -> "Hak Ediş (+Alacak)"
                                        "CASH_COLLECTION" -> "Kapıda Tahsilat (Mahsup)"
                                        "TIP" -> "Bahşiş (+Nakit)"
                                        "HAKEDIS_PAYOUT" -> "Ödenen Hak Ediş"
                                        else -> env?.name ?: "Gider"
                                    }
                                    val tagColor = when (tx.transactionType) {
                                        "HAKEDIS" -> CourierOrange
                                        "CASH_COLLECTION" -> NeonGreen
                                        "TIP" -> PastelPurple
                                        "HAKEDIS_PAYOUT" -> CourierOrange
                                        else -> envColor
                                    }

                                    Text(
                                        text = typeTag,
                                        color = tagColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = " • $dateStr",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                val amountPrefix = when (tx.transactionType) {
                                    "HAKEDIS" -> "+"
                                    "CASH_COLLECTION" -> "+💵 "
                                    "TIP" -> "+"
                                    "HAKEDIS_PAYOUT" -> "✓ "
                                    else -> "-"
                                }
                                val amountColor = when (tx.transactionType) {
                                    "HAKEDIS" -> CourierOrange
                                    "CASH_COLLECTION" -> NeonGreen
                                    "TIP" -> NeonGreen
                                    "HAKEDIS_PAYOUT" -> CourierOrange
                                    else -> SoftRed
                                }

                                Text(
                                    text = amountPrefix + CourierViewModel.formatCurrency(tx.amount),
                                    color = amountColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (tx.transactionType == "CASH_COLLECTION") {
                                    Text(
                                        text = "-Hak Ediş",
                                        color = TextTertiary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteTransaction(tx.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog for Custom Tip Amount
    if (showCustomTipDialog) {
        CustomTipDialog(
            onDismiss = { showCustomTipDialog = false },
            onConfirm = { amount ->
                onAddQuickTip(amount)
                showCustomTipDialog = false
            }
        )
    }
}

@Composable
fun CustomTipDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var tipText by remember { mutableStateOf("") }
    val tipAmount = tipText.replace(',', '.').toDoubleOrNull() ?: 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Özel Bahşiş Tutarı", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tipText,
                    onValueChange = { tipText = it },
                    placeholder = { Text("0,00", color = TextTertiary, fontSize = 28.sp, fontFamily = FontFamily.Monospace) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = NeonGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (tipAmount > 0) onConfirm(tipAmount) },
                    enabled = tipAmount > 0,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cebe Ekle (+${CourierViewModel.formatCurrency(tipAmount)})", color = PureBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
