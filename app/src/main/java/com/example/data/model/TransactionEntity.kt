package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double, // Gross amount (KDV Dahil)
    val transactionType: String = "EXPENSE", // "HAKEDIS", "CASH_COLLECTION", "TIP", "EXPENSE", "HAKEDIS_PAYOUT"
    val isIncome: Boolean = false, // true = Gelir, false = Gider
    val envelopeId: Int? = null,
    val envelopeName: String? = null,
    val vatRate: Int = 20, // 0, 1, 10, 20 (%)
    val vatAmount: Double = 0.0, // Calculated VAT (Tutar * Oran / (100 + Oran))
    val baseAmount: Double = 0.0, // Matrah (Tutar - vatAmount)
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
