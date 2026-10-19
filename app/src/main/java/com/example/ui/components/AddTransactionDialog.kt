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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

enum class TransactionEntryMode {
    EXPENSE,          // Gider / Fiş Harcaması
    HAKEDIS,          // Firma Hak Edişi / Kazancı
    CASH_COLLECTION   // Kapıda Nakit Sipariş Tahsilatı (Mahsuplaşma)
}

@Composable
fun AddTransactionDialog(
    envelopes: List<EnvelopeEntity>,
    initialMode: TransactionEntryMode = TransactionEntryMode.EXPENSE,
    initialEnvelopeId: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        isIncome: Boolean,
        envelopeId: Int?,
        envelopeName: String?,
        vatRate: Int,
        note: String?,
        transactionType: String
    ) -> Unit
) {
    var entryMode by remember { mutableStateOf(initialMode) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedEnvelopeId by remember { mutableStateOf(initialEnvelopeId ?: envelopes.firstOrNull()?.id) }

    val selectedEnvelope = envelopes.find { it.id == selectedEnvelopeId }
    var vatRate by remember(selectedEnvelope, entryMode) {
        mutableIntStateOf(
            if (entryMode == TransactionEntryMode.EXPENSE) (selectedEnvelope?.vatRate ?: 20) else 0
        )
    }

    val amount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val vatAmount = if (vatRate > 0) amount * vatRate / (100.0 + vatRate) else 0.0
    val baseAmount = amount - vatAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
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
                        text = when (entryMode) {
                            TransactionEntryMode.EXPENSE -> "Gider / Fiş Ekle"
                            TransactionEntryMode.HAKEDIS -> "Hak Ediş Kazancı Ekle"
                            TransactionEntryMode.CASH_COLLECTION -> "Kapıda Nakit Tahsilat"
                        },
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_transaction_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Way Mode Switcher: GİDER, HAK EDİŞ, KAPIDA NAKİT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Gider
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (entryMode == TransactionEntryMode.EXPENSE) SoftRed else Color.Transparent)
                            .clickable { entryMode = TransactionEntryMode.EXPENSE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GİDER (-)",
                            color = if (entryMode == TransactionEntryMode.EXPENSE) PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Hak Ediş
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (entryMode == TransactionEntryMode.HAKEDIS) CourierOrange else Color.Transparent)
                            .clickable { entryMode = TransactionEntryMode.HAKEDIS }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HAK EDİŞ (+)",
                            color = if (entryMode == TransactionEntryMode.HAKEDIS) PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Kapıda Nakit
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (entryMode == TransactionEntryMode.CASH_COLLECTION) NeonGreen else Color.Transparent)
                            .clickable { entryMode = TransactionEntryMode.CASH_COLLECTION }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KAPIDA NAKİT",
                            color = if (entryMode == TransactionEntryMode.CASH_COLLECTION) PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Context Explanation Banner for Cash Collection
                if (entryMode == TransactionEntryMode.CASH_COLLECTION) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeonGreen.copy(alpha = 0.12f))
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💵 Müşteriden nakit alındı: Para cebinize girer (+Cepteki Nakit), ancak şirketin parası olduğundan firma hak edişinizden otomatik düşülür (-Hak Ediş).",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else if (entryMode == TransactionEntryMode.HAKEDIS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CourierOrange.copy(alpha = 0.12f))
                            .border(1.dp, CourierOrange.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "🏢 Firma paket kazancı veya prim doğrudan 'Şirket Hak Ediş Alacağı' hanenizi artırır.",
                            color = CourierOrange,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount input (Large Monospace)
                Text(text = "Tutar (₺)", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("0,00", color = TextTertiary, fontSize = 32.sp, fontFamily = FontFamily.Monospace) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = when (entryMode) {
                            TransactionEntryMode.EXPENSE -> SoftRed
                            TransactionEntryMode.HAKEDIS -> CourierOrange
                            TransactionEntryMode.CASH_COLLECTION -> NeonGreen
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CourierOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title input
                Text(
                    text = when (entryMode) {
                        TransactionEntryMode.EXPENSE -> "Açıklama / İş Yeri"
                        TransactionEntryMode.HAKEDIS -> "Firma / Dönem Açıklaması"
                        TransactionEntryMode.CASH_COLLECTION -> "Sipariş / Müşteri Notu"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            text = when (entryMode) {
                                TransactionEntryMode.EXPENSE -> "Örn: Opet Benzin"
                                TransactionEntryMode.HAKEDIS -> "Örn: Trendyol Go Haftalık Hak Ediş"
                                TransactionEntryMode.CASH_COLLECTION -> "Örn: Kapıda Nakit Sipariş #108"
                            },
                            color = TextTertiary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CourierOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Envelope selection (ONLY for EXPENSE)
                if (entryMode == TransactionEntryMode.EXPENSE) {
                    Text(text = "Harcama Zarfı Seçin", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(envelopes) { env ->
                            val isSelected = env.id == selectedEnvelopeId
                            val envColor = try {
                                Color(android.graphics.Color.parseColor(env.colorHex))
                            } catch (_: Exception) {
                                CourierOrange
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) envColor.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) envColor else DarkBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedEnvelopeId = env.id
                                        vatRate = env.vatRate
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(envColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = env.name,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // VAT Rate Selection for EXPENSE
                    Text(text = "KDV Oranı (%)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 1, 10, 20).forEach { rate ->
                            val isSelected = vatRate == rate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CourierOrange else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) CourierOrange else DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { vatRate = rate }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%$rate",
                                    color = if (isSelected) PureBlack else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Matrah & VAT breakdown preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("KDV Hariç Matrah:", color = TextSecondary, fontSize = 13.sp)
                                Text(CourierViewModel.formatCurrency(baseAmount), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (selectedEnvelope?.type == "COMPANY") "İndirilecek KDV (Tasarruf):" else "KDV Tutarı:",
                                    color = if (selectedEnvelope?.type == "COMPANY") NeonGreen else TextSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    CourierViewModel.formatCurrency(vatAmount),
                                    color = if (selectedEnvelope?.type == "COMPANY") NeonGreen else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        if (amount > 0) {
                            when (entryMode) {
                                TransactionEntryMode.EXPENSE -> {
                                    onConfirm(
                                        title.ifBlank { "Gider Harcaması" },
                                        amount,
                                        false,
                                        selectedEnvelopeId,
                                        selectedEnvelope?.name,
                                        vatRate,
                                        note.ifBlank { null },
                                        "EXPENSE"
                                    )
                                }
                                TransactionEntryMode.HAKEDIS -> {
                                    onConfirm(
                                        title.ifBlank { "Firma Hak Ediş Kazancı" },
                                        amount,
                                        true,
                                        null,
                                        null,
                                        0,
                                        note.ifBlank { null },
                                        "HAKEDIS"
                                    )
                                }
                                TransactionEntryMode.CASH_COLLECTION -> {
                                    onConfirm(
                                        title.ifBlank { "Kapıda Nakit Tahsilat" },
                                        amount,
                                        false,
                                        null,
                                        null,
                                        0,
                                        note.ifBlank { "Kapıda nakit tahsil edildi (Hak edişten mahsup edildi)" },
                                        "CASH_COLLECTION"
                                    )
                                }
                            }
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (entryMode) {
                            TransactionEntryMode.EXPENSE -> SoftRed
                            TransactionEntryMode.HAKEDIS -> CourierOrange
                            TransactionEntryMode.CASH_COLLECTION -> NeonGreen
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = amount > 0
                ) {
                    Text(
                        text = when (entryMode) {
                            TransactionEntryMode.EXPENSE -> "Harcamayı Kaydet"
                            TransactionEntryMode.HAKEDIS -> "Hak Edişi Kaydet"
                            TransactionEntryMode.CASH_COLLECTION -> "Tahsilatı Kaydet"
                        },
                        color = PureBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
