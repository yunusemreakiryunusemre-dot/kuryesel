package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SoftRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsDialog(
    currentName: String,
    currentApiKey: String,
    companyReceivable: Double,
    paidHakedis: Double,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    onConfirmHakedisPayout: () -> Unit,
    onResetData: () -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var saveApiKeySuccessMessage by remember { mutableStateOf<String?>(null) }
    var nameSuccessMessage by remember { mutableStateOf<String?>(null) }

    var showConfirmPayout by remember { mutableStateOf(false) }
    var showConfirmReset by remember { mutableStateOf(false) }

    LaunchedEffect(currentApiKey) {
        if (apiKeyInput.isEmpty() && currentApiKey.isNotEmpty()) {
            apiKeyInput = currentApiKey
        }
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CourierOrange.copy(alpha = 0.15f))
                                .border(1.dp, CourierOrange.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚙️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ayarlar & Yapılandırma",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "KuryeCüzdan & Yapay Zekâ Tercihleri",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("close_settings_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                // 1. GEMINI API KEY SECTION (High priority, sleek AI-accented card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.2.dp, PastelPurple.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PastelPurple.copy(alpha = 0.2f))
                                        .border(1.dp, PastelPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = PastelPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Gemini Yapay Zekâ API",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Fiş Tarama (Vision) & Akıllı Asistan",
                                        color = PastelPurple,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Active / Inactive Badge
                            if (currentApiKey.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonGreen.copy(alpha = 0.15f))
                                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "● Kayıtlı (Aktif)",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CourierOrange.copy(alpha = 0.15f))
                                        .border(1.dp, CourierOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Key Girilmedi",
                                        color = CourierOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Google AI Studio'dan aldığınız Gemini API Anahtarınızı aşağıya girip kaydedebilirsiniz. Anahtar cihazın yerel belleğinde (localStorage) güvenle saklanır.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        // Masked key display if present
                        if (currentApiKey.isNotBlank()) {
                            val maskedKey = if (currentApiKey.length > 8) {
                                "${currentApiKey.take(4)}••••••••${currentApiKey.takeLast(4)}"
                            } else {
                                "••••••••••••"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PureBlack.copy(alpha = 0.5f))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = maskedKey,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onClearApiKey()
                                        apiKeyInput = ""
                                        saveApiKeySuccessMessage = "API anahtarı yerel bellekten silindi."
                                    },
                                    modifier = Modifier.size(28.dp).testTag("delete_api_key_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Anahtarı Sil", tint = SoftRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Input Box ("Gemini API Key Giriniz")
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                saveApiKeySuccessMessage = null
                            },
                            singleLine = true,
                            label = { Text("Gemini API Key Giriniz", fontSize = 12.sp) },
                            placeholder = { Text("AIzaSy...", color = TextTertiary, fontSize = 12.sp) },
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isApiKeyVisible) "Gizle" else "Göster",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gemini_api_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurple,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = PastelPurple,
                                unfocusedLabelColor = TextSecondary,
                                focusedContainerColor = PureBlack,
                                unfocusedContainerColor = PureBlack,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Save Button
                        Button(
                            onClick = {
                                val trimmed = apiKeyInput.trim()
                                if (trimmed.isNotBlank()) {
                                    onSaveApiKey(trimmed)
                                    saveApiKeySuccessMessage = "API anahtarı yerel belleğe ('localStorage') başarıyla kaydedildi! Fiş tarama ve asistan aktif."
                                } else {
                                    onClearApiKey()
                                    saveApiKeySuccessMessage = "API anahtarı temizlendi."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_api_key_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PureBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kaydet",
                                color = PureBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Success Feedback
                        AnimatedVisibility(
                            visible = saveApiKeySuccessMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            saveApiKeySuccessMessage?.let { msg ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonGreen.copy(alpha = 0.12f))
                                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = msg, color = NeonGreen, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Text(
                            text = "💡 Google AI Studio (aistudio.google.com) üzerinden ücretsiz 'Gemini API Key' alabilirsiniz.",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                // 2. Kurye Profili / Hitap İsmi Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kurye Hitap İsmi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    nameSuccessMessage = null
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("courier_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CourierOrange,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = PureBlack,
                                    unfocusedContainerColor = PureBlack,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    onSaveName(nameInput.trim())
                                    nameSuccessMessage = "İsim güncellendi"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Kaydet", color = PureBlack, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (nameSuccessMessage != null) {
                            Text(text = "✅ $nameSuccessMessage", color = NeonGreen, fontSize = 11.sp)
                        }
                    }
                }

                // 3. Hak Ediş Hesabı Yönetimi (Safe Payout Approval)
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
                                    textAlign = TextAlign.Center
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

                // 4. Sole proprietorship tax guide
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

                // 5. Reset data
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
