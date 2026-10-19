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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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

val PASTEL_PALETTE = listOf(
    "#FF8A65", // Şeftali
    "#64B5F6", // Mavi
    "#BA68C8", // Mor
    "#FFD54F", // Amber
    "#4DB6AC", // Turkuaz
    "#90A4AE", // Gri
    "#F06292", // Pembe
    "#81C784", // Yeşil
    "#FFB74D", // Turuncu
    "#9575CD"  // Leylak
)

@Composable
fun AddEditEnvelopeDialog(
    initialEnvelope: EnvelopeEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, vatRate: Int, colorHex: String, monthlyBudget: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialEnvelope?.name ?: "") }
    var type by remember { mutableStateOf(initialEnvelope?.type ?: "COMPANY") }
    var vatRate by remember { mutableIntStateOf(initialEnvelope?.vatRate ?: 20) }
    var selectedColor by remember { mutableStateOf(initialEnvelope?.colorHex ?: PASTEL_PALETTE[0]) }
    var budgetText by remember {
        mutableStateOf(if (initialEnvelope != null && initialEnvelope.monthlyBudget > 0) initialEnvelope.monthlyBudget.toInt().toString() else "")
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
                        text = if (initialEnvelope == null) "Yeni Zarf Oluştur" else "Zarfı Düzenle",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_envelope_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type selection: Şirket vs Kişisel
                Text("Zarf Türü:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "COMPANY") CourierOrange else Color.Transparent)
                            .clickable { type = "COMPANY" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ŞİRKET GİDERİ",
                            color = if (type == "COMPANY") PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "PERSONAL") CourierOrange else Color.Transparent)
                            .clickable { type = "PERSONAL" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KİŞİSEL HARCAMA",
                            color = if (type == "PERSONAL") PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = if (type == "COMPANY") "✓ KDV ve Gelir Vergisinden düşülebilir" else "✕ Şirket vergisinden düşülmez",
                    color = if (type == "COMPANY") NeonGreen else TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Name input
                Text("Zarf Adı", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Örn: Yağ Değişimi, Kask, Kira", color = TextTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("envelope_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CourierOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // VAT Rate selection
                Text("KDV Oranı (%)", color = TextSecondary, fontSize = 12.sp)
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

                // Pastel Color selection
                Text("Zarf Rengi", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PASTEL_PALETTE) { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) TextPrimary else DarkBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Monthly Budget (optional)
                Text("Aylık Bütçe Sınırı (₺ - İsteğe Bağlı)", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    placeholder = { Text("0 (Sınırsız)", color = TextTertiary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("envelope_budget_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CourierOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val budget = budgetText.toDoubleOrNull() ?: 0.0
                            onSave(name.trim(), type, vatRate, selectedColor, budget)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_envelope_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                    shape = RoundedCornerShape(16.dp),
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        text = if (initialEnvelope == null) "Zarfı Oluştur" else "Değişiklikleri Kaydet",
                        color = PureBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EnvelopeDetailDialog(
    envelope: EnvelopeEntity,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDeleteEnvelope: () -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val envTransactions = transactions.filter { it.envelopeId == envelope.id }
    val totalSpent = envTransactions.sumOf { it.amount }
    val totalVat = envTransactions.sumOf { it.vatAmount }
    val totalBase = envTransactions.sumOf { it.baseAmount }

    val envColor = try {
        Color(android.graphics.Color.parseColor(envelope.colorHex))
    } catch (_: Exception) {
        CourierOrange
    }

    val progress = if (envelope.monthlyBudget > 0) {
        (totalSpent / envelope.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
    } else 0f

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
                // Header with color badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(envColor))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(envelope.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(
                                text = if (envelope.type == "COMPANY") "Şirket Gideri (%${envelope.vatRate} KDV)" else "Kişisel Harcama (%${envelope.vatRate} KDV)",
                                color = if (envelope.type == "COMPANY") NeonGreen else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Toplam Harcanan", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = CourierViewModel.formatCurrency(totalSpent),
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (envelope.monthlyBudget > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Aylık Bütçe: ${CourierViewModel.formatCurrency(envelope.monthlyBudget)}", color = TextSecondary, fontSize = 12.sp)
                                Text("%${(progress * 100).toInt()}", color = if (progress >= 0.9f) SoftRed else envColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = if (progress >= 0.9f) SoftRed else envColor,
                                trackColor = DarkBorder
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

                        // Tax breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("KDV Hariç Matrah:", color = TextSecondary, fontSize = 13.sp)
                            Text(CourierViewModel.formatCurrency(totalBase), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (envelope.type == "COMPANY") "İndirilecek KDV (Vergi Tasarrufu):" else "Ödenen KDV:",
                                color = if (envelope.type == "COMPANY") NeonGreen else TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                CourierViewModel.formatCurrency(totalVat),
                                color = if (envelope.type == "COMPANY") NeonGreen else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Edit / Delete envelope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = CourierOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Düzenle", color = TextPrimary, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onDeleteEnvelope()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = SoftRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zarfı Sil", color = SoftRed, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transactions list in this envelope
                Text("Zarftaki İşlemler (${envTransactions.size})", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (envTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bu zarfta henüz işlem yok.", color = TextTertiary, fontSize = 13.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        envTransactions.forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("KDV: ${CourierViewModel.formatCurrency(tx.vatAmount)} (%${tx.vatRate})", color = TextSecondary, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        CourierViewModel.formatCurrency(tx.amount),
                                        color = CourierOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = { onDeleteTransaction(tx.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
