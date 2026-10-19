package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "envelopes")
data class EnvelopeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "COMPANY" (Şirket Gideri / KDV'den Düşülebilir) or "PERSONAL" (Kişisel Harcama)
    val vatRate: Int, // 0, 1, 10, 20 (%)
    val colorHex: String, // e.g. "#B39DDB", "#A5D6A7"
    val monthlyBudget: Double = 0.0, // 0 means no limit
    val iconName: String = "category"
)
