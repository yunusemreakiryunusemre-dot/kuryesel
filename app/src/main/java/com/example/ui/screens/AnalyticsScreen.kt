package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnvelopeEntity
import com.example.data.model.TransactionEntity
import com.example.ui.CourierSummary
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

data class EnvelopeSlice(
    val envelope: EnvelopeEntity,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun AnalyticsScreen(
    summary: CourierSummary,
    envelopes: List<EnvelopeEntity>,
    transactions: List<TransactionEntity>
) {
    val totalExpense = summary.totalExpense

    // Calculate slices for spending by envelope
    val slices = remember(transactions, envelopes, totalExpense) {
        val spendingMap = transactions.filter { !it.isIncome && it.envelopeId != null }
            .groupBy { it.envelopeId!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val envelopeMap = envelopes.associateBy { it.id }

        spendingMap.mapNotNull { (envId, spent) ->
            val env = envelopeMap[envId] ?: return@mapNotNull null
            val pct = if (totalExpense > 0) (spent / totalExpense).toFloat() else 0f
            val color = try {
                Color(android.graphics.Color.parseColor(env.colorHex))
            } catch (_: Exception) {
                CourierOrange
            }
            EnvelopeSlice(env, spent, pct, color)
        }.sortedByDescending { it.amount }
    }

    val companyPct = if (totalExpense > 0) {
        (summary.companyExpenses / totalExpense).toFloat()
    } else 0f

    val personalPct = if (totalExpense > 0) {
        (summary.personalExpenses / totalExpense).toFloat()
    } else 0f

    // Smart courier advice generation
    val adviceList = remember(summary, envelopes, slices) {
        val list = mutableListOf<Triple<String, String, Color>>()

        if (summary.deductibleVat > 0) {
            list.add(
                Triple(
                    "KDV Tasarrufu Sağlandı",
                    "Şirket harcamaların sayesinde devlete ödenecek KDV'den ${CourierViewModel.formatCurrency(summary.deductibleVat)} düşüldü!",
                    NeonGreen
                )
            )
        }

        // Check fuel envelope
        val gasSlice = slices.find { it.envelope.name.contains("Benzin", ignoreCase = true) }
        if (gasSlice != null && gasSlice.envelope.monthlyBudget > 0) {
            val gasBudgetRatio = gasSlice.amount / gasSlice.envelope.monthlyBudget
            if (gasBudgetRatio >= 0.85) {
                list.add(
                    Triple(
                        "Benzin Bütçesi Uyarısı",
                        "Aylık benzin zarfının %${(gasBudgetRatio * 100).toInt()}'ini harcadın. Ay sonuna kadar tasarruflu rota tercih edebilirsin.",
                        CourierOrange
                    )
                )
            } else {
                list.add(
                    Triple(
                        "Benzin Harcaman İdeal",
                        "Benzin zarfının henüz %${(gasBudgetRatio * 100).toInt()}'i kullanıldı. Bütçen dengeli ilerliyor.",
                        NeonGreen
                    )
                )
            }
        }

        // Company vs Personal ratio advice
        if (totalExpense > 0) {
            if (companyPct >= 0.60f) {
                list.add(
                    Triple(
                        "Yüksek Vergi İndirimi Oranı",
                        "Harcamalarının %${(companyPct * 100).toInt()}'i şirket gideri. Bu durum ay sonu gelir vergisi ve KDV matrahını önemli ölçüde hafifletecek.",
                        NeonGreen
                    )
                )
            } else {
                list.add(
                    Triple(
                        "Kişisel Harcama Dengesi",
                        "Kişisel harcamaların toplam giderlerin %${(personalPct * 100).toInt()}'i seviyesinde. Şahıs şirketi nakit dengeni korumak için ideal.",
                        TextSecondary
                    )
                )
            }
        } else {
            list.add(
                Triple(
                    "Veri Girişi Bekleniyor",
                    "Henüz harcama kaydı eklenmedi. İşlem girdikçe akıllı vergi ve bütçe tavsiyeleri burada görünecektir.",
                    TextTertiary
                )
            )
        }

        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Analiz & Akıllı Yüzdeler",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Harcama dağılımı ve vergi optimizasyonu",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Company vs Personal Expenses Ratio Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ŞİRKET GİDERİ vs KİŞİSEL HARCAMA ORANI",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CourierOrange))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Şirket (KDV İndirimli)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                CourierViewModel.formatCurrency(summary.companyExpenses),
                                color = CourierOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("%${(companyPct * 100).toInt()}", color = TextSecondary, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Kişisel Harcamalar", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(NeonGreen))
                            }
                            Text(
                                CourierViewModel.formatCurrency(summary.personalExpenses),
                                color = NeonGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("%${(personalPct * 100).toInt()}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Split bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        if (totalExpense > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(companyPct.coerceAtLeast(0.01f))
                                    .fillMaxSize()
                                    .background(CourierOrange)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(personalPct.coerceAtLeast(0.01f))
                                    .fillMaxSize()
                                    .background(NeonGreen)
                            )
                        }
                    }
                }
            }
        }

        // Circular Spending Distribution (Donut Chart)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ZARF BAZLI HARCAMA DAĞILIMI",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalExpense <= 0 || slices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Harcama Yok", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(190.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                for (slice in slices) {
                                    val sweep = slice.percentage * 360f
                                    drawArc(
                                        color = slice.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Butt)
                                    )
                                    startAngle += sweep
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Toplam Gider", color = TextTertiary, fontSize = 11.sp)
                                Text(
                                    CourierViewModel.formatCurrency(totalExpense),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Envelope Breakdown List
        if (slices.isNotEmpty()) {
            item {
                Text(
                    text = "Kategori Dağılım Sıralaması",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(slices) { slice ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(slice.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(slice.envelope.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    CourierViewModel.formatCurrency(slice.amount),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "%${(slice.percentage * 100).toInt()}",
                                    color = slice.color,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { slice.percentage },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = slice.color,
                            trackColor = DarkBorder
                        )
                    }
                }
            }
        }

        // Smart Advice Cards Section
        item {
            Text(
                text = "💡 Akıllı Esnaf Tavsiyeleri",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(adviceList) { (title, desc, accentColor) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
