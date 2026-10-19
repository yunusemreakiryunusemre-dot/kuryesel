package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.ParsedReceipt
import com.example.data.model.EnvelopeEntity
import com.example.ui.CourierViewModel
import com.example.ui.ScanUiState
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

@Composable
fun ScanReceiptDialog(
    envelopes: List<EnvelopeEntity>,
    scanState: ScanUiState,
    isApiKeyConfigured: Boolean = true,
    onOpenSettings: () -> Unit = {},
    onDismiss: () -> Unit,
    onScanImage: (Bitmap) -> Unit,
    onConfirmParsed: (title: String, amount: Double, envelopeId: Int?, envelopeName: String?, vatRate: Int, note: String?) -> Unit
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bmp != null) {
                    selectedBitmap = bmp
                    onScanImage(bmp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CourierOrange, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI Fiş Tarayıcı",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_scan_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isApiKeyConfigured) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PastelPurple.copy(alpha = 0.15f))
                            .border(1.dp, PastelPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gemini API Key Gerekli",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Fiş tarama için kendi API anahtarınızı kaydedin.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = onOpenSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = PastelPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("⚙️ Ayarla", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Image upload / preview box
                if (selectedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, DarkBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CourierOrange, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Fiş / Fatura Fotoğrafı Yükle",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tutar, KDV ve Kategori otomatik ayrıştırılır",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CourierOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galeriden Fotoğraf Seç", color = TextPrimary)
                    }
                } else {
                    // Bitmap preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Fiş Önizleme",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // State representations
                when (scanState) {
                    is ScanUiState.Scanning -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CourierOrange, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Gemini Vision fişi okuyor...",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "İş yeri, KDV oranı ve matrah hesaplanıyor",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    is ScanUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoftRed.copy(alpha = 0.15f))
                                .border(1.dp, SoftRed, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Tarama Hatası: ${scanState.message}",
                                color = SoftRed,
                                fontSize = 13.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚙️ Ayarlar", color = TextPrimary)
                            }
                            Button(
                                onClick = { selectedBitmap?.let { onScanImage(it) } },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tekrar Dene", color = PureBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is ScanUiState.Success -> {
                        ParsedReceiptResultContent(
                            parsed = scanState.parsed,
                            envelopes = envelopes,
                            onConfirm = { chosenEnvId, chosenEnvName ->
                                onConfirmParsed(
                                    scanState.parsed.merchant,
                                    scanState.parsed.totalAmount,
                                    chosenEnvId,
                                    chosenEnvName,
                                    scanState.parsed.vatRate,
                                    "Fiş Tarihi: ${scanState.parsed.date}"
                                )
                                onDismiss()
                            }
                        )
                    }

                    ScanUiState.Idle -> {
                        // Ready for input
                    }
                }
            }
        }
    }
}

@Composable
private fun ParsedReceiptResultContent(
    parsed: ParsedReceipt,
    envelopes: List<EnvelopeEntity>,
    onConfirm: (envelopeId: Int?, envelopeName: String?) -> Unit
) {
    // Find best envelope match based on AI suggestion
    val initialEnv = envelopes.find {
        it.name.contains(parsed.suggestedCategory, ignoreCase = true) ||
                parsed.suggestedCategory.contains(it.name, ignoreCase = true)
    } ?: envelopes.firstOrNull { it.type == "COMPANY" }

    var selectedEnvId by remember { mutableStateOf(initialEnv?.id) }
    val selectedEnv = envelopes.find { it.id == selectedEnvId }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NeonGreen.copy(alpha = 0.15f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fiş başarıyla okundu!", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Details card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("İş Yeri:", color = TextSecondary, fontSize = 13.sp)
                    Text(parsed.merchant, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Toplam Tutar:", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        CourierViewModel.formatCurrency(parsed.totalAmount),
                        color = CourierOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("KDV Oranı & Tutarı:", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        "%${parsed.vatRate} (${CourierViewModel.formatCurrency(parsed.vatAmount)})",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Matrah (KDV Hariç):", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        CourierViewModel.formatCurrency(parsed.baseAmount),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Envelope selector
        Text("Kaydedilecek Zarf:", color = TextSecondary, fontSize = 12.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(envelopes) { env ->
                val isSelected = env.id == selectedEnvId
                val envColor = try {
                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(env.colorHex))
                } catch (_: Exception) {
                    CourierOrange
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) envColor.copy(alpha = 0.25f) else DarkSurfaceVariant)
                        .border(1.5.dp, if (isSelected) envColor else DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedEnvId = env.id }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(envColor))
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

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onConfirm(selectedEnvId, selectedEnv?.name) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Onayla ve Zarfına Ekle", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}
