package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@Composable
fun SettingsDialog(
    currentName: String,
    companyReceivable: Double,
    paidHakedis: Double,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onConfirmHakedisPayout: () -> Unit,
    onResetData: () -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var showConfirmPayout by remember { mutableStateOf(false) }
    var showConfirmReset by remember { mutableStateOf(false) }

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
                        text = "⚙️ Ayarlar & Hak Ediş Yönetimi",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name settings
                Text("Kurye Hitap İsmi", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("courier_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CourierOrange,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { onSaveName(nameInput.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kaydet", color = PureBlack, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Dedicated Hak Ediş Yönetimi Section (Safe Payout Approval)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.2.dp, CourierOrange.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = CourierOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hak Ediş Hesabı Yönetimi",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "Yanlışlıkla basılmasını önlemek amacıyla hak ediş aktarım onayı bu güvenli menüde tutulmaktadır.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bekleyen Hak Ediş Alacağı:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                CourierViewModel.formatCurrency(companyReceivable),
                                color = CourierOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Geçmişte Hesaba Yatan (Ödenen):", color = TextTertiary, fontSize = 12.sp)
                            Text(
                                CourierViewModel.formatCurrency(paidHakedis),
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (!showConfirmPayout) {
                            Button(
                                onClick = { showConfirmPayout = true },
                                enabled = companyReceivable > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_payout_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PureBlack, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hak Ediş Hesaba Yattı (Ödemeyi Onayla)",
                                    color = PureBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            // Confirmation block
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PureBlack)
                                    .border(1.dp, CourierOrange, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Firma ödemesi hesabınıza geçti mi? ${CourierViewModel.formatCurrency(companyReceivable)} tutarındaki bekleyen alacak sıfırlanıp 'Ödenen Hak Ediş'e aktarılacaktır.",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showConfirmPayout = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Vazgeç", color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            onConfirmHakedisPayout()
                                            showConfirmPayout = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = CourierOrange)
                                    ) {
                                        Text("Onayla", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sole proprietorship tax guide
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CourierOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Esnaf Kurye Vergi Takvimi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Text(
                            text = "• KDV 1 Beyannamesi: Her ayın 28'i son gün.\n" +
                                    "• Muhtasar Beyanname: Her ayın 26'sı.\n" +
                                    "• Bağkur Primi: Her ayın son iş günü.\n" +
                                    "• Geçici Vergi: 3 ayda bir (Şubat, Mayıs, Ağustos, Kasım).",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reset data
                if (!showConfirmReset) {
                    OutlinedButton(
                        onClick = { showConfirmReset = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_data_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = SoftRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tüm Verileri Sıfırla (₺0,00 Başlangıç)", color = SoftRed, fontSize = 13.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftRed.copy(alpha = 0.15f))
                            .border(1.dp, SoftRed, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tüm işlemler ve kayıtlar silinecek. Emin misiniz?",
                            color = SoftRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showConfirmReset = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Vazgeç", color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    onResetData()
                                    showConfirmReset = false
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftRed)
                            ) {
                                Text("Evet, Sıfırla", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
