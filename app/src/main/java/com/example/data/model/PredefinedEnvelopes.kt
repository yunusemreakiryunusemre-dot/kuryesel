package com.example.data.model

object PredefinedEnvelopes {
    val defaultEnvelopes = listOf(
        // Şirket Giderleri (Vergiden / KDV'den Düşülebilir)
        EnvelopeEntity(
            name = "Benzin & Yakıt",
            type = "COMPANY",
            vatRate = 20,
            colorHex = "#FF8A65",
            monthlyBudget = 4000.0,
            iconName = "local_gas_station"
        ),
        EnvelopeEntity(
            name = "Motor Bakım & Servis",
            type = "COMPANY",
            vatRate = 20,
            colorHex = "#64B5F6",
            monthlyBudget = 2500.0,
            iconName = "build"
        ),
        EnvelopeEntity(
            name = "Bağkur & Resmi Vergi",
            type = "COMPANY",
            vatRate = 0,
            colorHex = "#BA68C8",
            monthlyBudget = 6900.0,
            iconName = "account_balance"
        ),
        EnvelopeEntity(
            name = "Kask, Mont & Ekipman",
            type = "COMPANY",
            vatRate = 20,
            colorHex = "#FFD54F",
            monthlyBudget = 1500.0,
            iconName = "sports_motorsports"
        ),
        EnvelopeEntity(
            name = "Trafik Sigortası & Kasko",
            type = "COMPANY",
            vatRate = 0,
            colorHex = "#4DB6AC",
            monthlyBudget = 1000.0,
            iconName = "security"
        ),
        EnvelopeEntity(
            name = "Muhasebe & E-Fatura",
            type = "COMPANY",
            vatRate = 20,
            colorHex = "#90A4AE",
            monthlyBudget = 1200.0,
            iconName = "receipt_long"
        ),

        // Kişisel Harcamalar (Vergiden Düşülmez)
        EnvelopeEntity(
            name = "Ev Kirası & Faturalar",
            type = "PERSONAL",
            vatRate = 0,
            colorHex = "#F06292",
            monthlyBudget = 12000.0,
            iconName = "home"
        ),
        EnvelopeEntity(
            name = "Market & Mutfak",
            type = "PERSONAL",
            vatRate = 1,
            colorHex = "#81C784",
            monthlyBudget = 6000.0,
            iconName = "shopping_cart"
        ),
        EnvelopeEntity(
            name = "Kişisel Yemek & Kahve",
            type = "PERSONAL",
            vatRate = 10,
            colorHex = "#FFB74D",
            monthlyBudget = 3000.0,
            iconName = "restaurant"
        ),
        EnvelopeEntity(
            name = "Telefon & Dijital Servisler",
            type = "PERSONAL",
            vatRate = 20,
            colorHex = "#9575CD",
            monthlyBudget = 800.0,
            iconName = "smartphone"
        )
    )
}
