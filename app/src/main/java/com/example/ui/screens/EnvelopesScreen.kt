package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun EnvelopesScreen(
    envelopes: List<EnvelopeEntity>,
    transactions: List<TransactionEntity>,
    onOpenAddEnvelope: () -> Unit,
    onEnvelopeClick: (EnvelopeEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf("ALL") } // "ALL", "COMPANY", "PERSONAL"

    val filteredEnvelopes = remember(envelopes, selectedTab) {
        when (selectedTab) {
            "COMPANY" -> envelopes.filter { it.type == "COMPANY" }
            "PERSONAL" -> envelopes.filter { it.type == "PERSONAL" }
            else -> envelopes
        }
    }

    // Pre-calculate spending by envelope
    val spendingByEnvelope = remember(transactions) {
        transactions.filter { !it.isIncome && it.envelopeId != null }
            .groupBy { it.envelopeId!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Zarflar & Kategoriler",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Şirket ve kişisel harcama bütçelerin",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onOpenAddEnvelope,
                    colors = ButtonDefaults.buttonColors(containerColor = CourierOrange),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("new_envelope_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yeni Zarf", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Filter Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Triple("ALL", "Tümü (${envelopes.size})", null),
                    Triple("COMPANY", "Şirket Gideri", Icons.Default.Business),
                    Triple("PERSONAL", "Kişisel", Icons.Default.Person)
                )

                tabs.forEach { (key, label, icon) ->
                    val isSelected = selectedTab == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CourierOrange else Color.Transparent)
                            .clickable { selectedTab = key }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (icon != null) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) PureBlack else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = label,
                                color = if (isSelected) PureBlack else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Summary Info Pill
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = if (selectedTab == "COMPANY") {
                        "💡 Şirket giderlerinin KDV'si (%20/%10) hakediş KDV'sinden indirilir ve vergi tasarrufu sağlar."
                    } else if (selectedTab == "PERSONAL") {
                        "ℹ️ Kişisel harcamalar (ev kirası, market vb.) şirketin vergi beyannamesinden düşülemez."
                    } else {
                        "✨ Zarf kartına tıklayarak harcama geçmişini, matrahı ve KDV dökümünü inceleyebilirsin."
                    },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Envelopes List
        items(filteredEnvelopes, key = { it.id }) { env ->
            val spent = spendingByEnvelope[env.id] ?: 0.0
            val envColor = try {
                Color(android.graphics.Color.parseColor(env.colorHex))
            } catch (_: Exception) {
                CourierOrange
            }

            val progress = if (env.monthlyBudget > 0) {
                (spent / env.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
            } else 0f

            val isOverBudget = env.monthlyBudget > 0 && spent > env.monthlyBudget

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                    .clickable { onEnvelopeClick(env) }
                    .testTag("envelope_card_${env.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(envColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = env.name,
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (env.type == "COMPANY") "Şirket Gideri (%${env.vatRate} KDV)" else "Kişisel Harcama (%${env.vatRate} KDV)",
                                    color = if (env.type == "COMPANY") NeonGreen else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Harcanan",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = CourierViewModel.formatCurrency(spent),
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (env.monthlyBudget > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Bütçe: ${CourierViewModel.formatCurrency(env.monthlyBudget)}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "%${(progress * 100).toInt()} harcandı",
                                    color = if (isOverBudget) SoftRed else envColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (env.monthlyBudget > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (isOverBudget) SoftRed else envColor,
                            trackColor = DarkSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
