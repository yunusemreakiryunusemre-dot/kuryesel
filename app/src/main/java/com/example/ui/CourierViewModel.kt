package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiService
import com.example.ai.ParsedReceipt
import com.example.data.model.EnvelopeEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.CourierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Scanning : ScanUiState
    data class Success(val parsed: ParsedReceipt) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class CourierSummary(
    val netBalance: Double = 0.0,
    val pocketCash: Double = 0.0,            // Cepteki Nakit Kasa (Fiziksel para)
    val companyReceivable: Double = 0.0,     // Şirket Hak Ediş Alacağı (İçeride biriken bekleyen hak ediş)
    val paidHakedis: Double = 0.0,           // Ödenen / Hesaba Yatan Hak Ediş
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val companyExpenses: Double = 0.0,
    val personalExpenses: Double = 0.0,
    val deductibleVat: Double = 0.0,
    val vatExcludedBalance: Double = 0.0,
    val daysUntilDeclaration: Int = 0
)

class CourierViewModel(
    private val repository: CourierRepository,
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    val envelopes: StateFlow<List<EnvelopeEntity>> = repository.allEnvelopes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Selam Burak! Ben KuryeCüzdan mali asistanınım. Bu ayki KDV durumun, kapıda nakit tahsilat mahsuplaşması, hak edişlerin veya Bağkur ödemelerinle ilgili aklına takılan her şeyi bana sorabilirsin.",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _courierName = MutableStateFlow("Burak")
    val courierName: StateFlow<String> = _courierName.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaultEnvelopes()
        }
    }

    val summary: StateFlow<CourierSummary> = combine(
        repository.allTransactions,
        repository.allEnvelopes
    ) { transactions, envelopesList ->
        var income = 0.0
        var expense = 0.0
        var compExpense = 0.0
        var persExpense = 0.0
        var deductibleVat = 0.0
        var vatExcludedRevenue = 0.0
        var vatExcludedCompExpense = 0.0

        var pocketCash = 0.0
        var companyReceivable = 0.0
        var paidHakedis = 0.0

        val envelopeMap = envelopesList.associateBy { it.id }

        for (tx in transactions) {
            when (tx.transactionType) {
                "HAKEDIS" -> {
                    companyReceivable += tx.amount
                    income += tx.amount
                    vatExcludedRevenue += tx.baseAmount
                }
                "CASH_COLLECTION" -> {
                    // Müşteriden nakit alındı: Cepteki nakit artar, firmanın hakedişinden düşülür (mahsup edilir)
                    pocketCash += tx.amount
                    companyReceivable -= tx.amount
                }
                "TIP" -> {
                    // Hızlı bahşiş: Tamamen kuryenin cebine giren net para
                    pocketCash += tx.amount
                    income += tx.amount
                    vatExcludedRevenue += tx.amount
                }
                "HAKEDIS_PAYOUT" -> {
                    // Şirket hakedişi banka hesabına yattı ve onaylandı
                    companyReceivable -= tx.amount
                    paidHakedis += tx.amount
                }
                "EXPENSE" -> {
                    // Varsayılan olarak cepteki nakitten ödenir
                    pocketCash -= tx.amount
                    expense += tx.amount
                    val env = tx.envelopeId?.let { envelopeMap[it] }
                    if (env?.type == "COMPANY") {
                        compExpense += tx.amount
                        deductibleVat += tx.vatAmount
                        vatExcludedCompExpense += tx.baseAmount
                    } else {
                        persExpense += tx.amount
                    }
                }
                else -> {
                    // Legacy fallback
                    if (tx.isIncome) {
                        companyReceivable += tx.amount
                        income += tx.amount
                        vatExcludedRevenue += tx.baseAmount
                    } else {
                        pocketCash -= tx.amount
                        expense += tx.amount
                    }
                }
            }
        }

        // Calculate days until 28th of current month (KDV 1 Beyanname son günü)
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val declarationTargetDay = 28
        val daysUntil = if (currentDay <= declarationTargetDay) {
            declarationTargetDay - currentDay
        } else {
            (maxDay - currentDay) + declarationTargetDay
        }

        CourierSummary(
            netBalance = pocketCash + companyReceivable,
            pocketCash = pocketCash,
            companyReceivable = companyReceivable,
            paidHakedis = paidHakedis,
            totalIncome = income,
            totalExpense = expense,
            companyExpenses = compExpense,
            personalExpenses = persExpense,
            deductibleVat = deductibleVat,
            vatExcludedBalance = vatExcludedRevenue - vatExcludedCompExpense,
            daysUntilDeclaration = daysUntil
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourierSummary())

    fun addTransaction(
        title: String,
        amount: Double,
        isIncome: Boolean,
        envelopeId: Int?,
        envelopeName: String?,
        vatRate: Int,
        note: String? = null,
        transactionType: String = if (isIncome) "HAKEDIS" else "EXPENSE"
    ) {
        viewModelScope.launch {
            val calculatedVat = if (vatRate > 0) {
                amount * vatRate / (100.0 + vatRate)
            } else 0.0
            val base = amount - calculatedVat

            val entity = TransactionEntity(
                title = title.ifBlank {
                    when (transactionType) {
                        "HAKEDIS" -> "Firma Hak Ediş Kazancı"
                        "CASH_COLLECTION" -> "Kapıda Nakit Sipariş Tahsilatı"
                        "TIP" -> "Elden Bahşiş"
                        else -> "Gider Harcaması"
                    }
                },
                amount = amount,
                transactionType = transactionType,
                isIncome = isIncome,
                envelopeId = envelopeId,
                envelopeName = envelopeName,
                vatRate = vatRate,
                vatAmount = Math.round(calculatedVat * 100.0) / 100.0,
                baseAmount = Math.round(base * 100.0) / 100.0,
                note = note
            )
            repository.insertTransaction(entity)
        }
    }

    fun addQuickTip(amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = "Elden Bahşiş (Tip)",
                amount = amount,
                transactionType = "TIP",
                isIncome = true,
                vatRate = 0,
                vatAmount = 0.0,
                baseAmount = amount,
                note = "Hızlı bahşiş eklendi"
            )
            repository.insertTransaction(entity)
        }
    }

    fun addCashCollection(amount: Double, note: String? = null) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = "Kapıda Nakit Tahsilat",
                amount = amount,
                transactionType = "CASH_COLLECTION",
                isIncome = false,
                vatRate = 0,
                vatAmount = 0.0,
                baseAmount = amount,
                note = note ?: "Müşteriden elden nakit alındı (Hak edişten mahsup edildi)"
            )
            repository.insertTransaction(entity)
        }
    }

    fun confirmHakedisPayout() {
        viewModelScope.launch {
            val currentReceivable = summary.value.companyReceivable
            if (currentReceivable > 0) {
                val entity = TransactionEntity(
                    title = "Hak Ediş Hesaba Yattı (Ödeme Alındı)",
                    amount = currentReceivable,
                    transactionType = "HAKEDIS_PAYOUT",
                    isIncome = false,
                    vatRate = 0,
                    vatAmount = 0.0,
                    baseAmount = currentReceivable,
                    note = "Haftalık/Dönemsel hak ediş ödemesi hesaba aktarıldı"
                )
                repository.insertTransaction(entity)
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addEnvelope(
        name: String,
        type: String,
        vatRate: Int,
        colorHex: String,
        monthlyBudget: Double
    ) {
        viewModelScope.launch {
            val env = EnvelopeEntity(
                name = name,
                type = type,
                vatRate = vatRate,
                colorHex = colorHex,
                monthlyBudget = monthlyBudget
            )
            repository.insertEnvelope(env)
        }
    }

    fun updateEnvelope(envelope: EnvelopeEntity) {
        viewModelScope.launch {
            repository.updateEnvelope(envelope)
        }
    }

    fun deleteEnvelope(id: Int) {
        viewModelScope.launch {
            repository.deleteEnvelope(id)
        }
    }

    fun setCourierName(name: String) {
        _courierName.value = name
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    fun scanReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning
            val result = geminiService.scanReceipt(bitmap)
            result.fold(
                onSuccess = { parsed ->
                    _scanState.value = ScanUiState.Success(parsed)
                },
                onFailure = { error ->
                    _scanState.value = ScanUiState.Error(error.localizedMessage ?: "Fiş taranamadı")
                }
            )
        }
    }

    fun clearScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank() || _isChatLoading.value) return

        val userMsg = ChatMessage(text = prompt, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val currentSummary = summary.value
            val contextInfo = """
                - Kurye Adı: ${_courierName.value}
                - Net Cüzdan Toplamı: ${formatCurrency(currentSummary.netBalance)}
                - Cepteki Nakit Kasa: ${formatCurrency(currentSummary.pocketCash)}
                - Şirket Hak Ediş Alacağı (Bekleyen): ${formatCurrency(currentSummary.companyReceivable)}
                - Ödenen / Tahsil Edilen Hak Ediş: ${formatCurrency(currentSummary.paidHakedis)}
                - Toplam Gider: ${formatCurrency(currentSummary.totalExpense)}
                - Şirket Giderleri (KDV İndirimli): ${formatCurrency(currentSummary.companyExpenses)}
                - İndirilecek KDV (Vergi Tasarrufu): ${formatCurrency(currentSummary.deductibleVat)}
                - KDV Hariç Matrah Bakiyesi: ${formatCurrency(currentSummary.vatExcludedBalance)}
                - Beyannameye Kalan Gün: ${currentSummary.daysUntilDeclaration} gün (Her ayın 28'i)
            """.trimIndent()

            val result = geminiService.chatWithAssistant(
                history = _chatMessages.value,
                userMessage = prompt,
                courierContext = contextInfo
            )

            result.fold(
                onSuccess = { reply ->
                    val assistantMsg = ChatMessage(text = reply, isUser = false)
                    _chatMessages.value = _chatMessages.value + assistantMsg
                },
                onFailure = { err ->
                    val errorMsg = ChatMessage(
                        text = "Üzgünüm, şu anda yanıt veremedim: ${err.localizedMessage}",
                        isUser = false
                    )
                    _chatMessages.value = _chatMessages.value + errorMsg
                }
            )
            _isChatLoading.value = false
        }
    }

    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            try {
                val list = allTransactions.value
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
                val sb = java.lang.StringBuilder()
                sb.append("ID,Tarih,Açıklama,İşlem Türü,Tip,Zarf,Tutar(TL),KDV Oranı(%),KDV Tutarı(TL),Matrah(TL),Not\n")

                for (item in list) {
                    val dateStr = sdf.format(Date(item.timestamp))
                    val typeStr = if (item.isIncome) "Gelir" else "Gider"
                    val envStr = (item.envelopeName ?: "").replace(",", " ")
                    val titleStr = item.title.replace(",", " ")
                    val noteStr = (item.note ?: "").replace(",", " ")
                    sb.append("${item.id},$dateStr,$titleStr,${item.transactionType},$typeStr,$envStr,${item.amount},${item.vatRate},${item.vatAmount},${item.baseAmount},$noteStr\n")
                }

                val file = File(context.cacheDir, "kurye_cuzdan_islem_raporu.csv")
                file.writeText(sb.toString(), Charsets.UTF_8)

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "KuryeCüzdan KDV ve Harcama Raporu (CSV)")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Raporu Paylaş / Muhasebeciye Gönder"))
            } catch (e: Exception) {
                Toast.makeText(context, "Dışa aktarma hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun exportBackupJson(context: Context) {
        viewModelScope.launch {
            try {
                val root = JSONObject()
                val txArray = JSONArray()
                for (tx in allTransactions.value) {
                    txArray.put(JSONObject().apply {
                        put("id", tx.id)
                        put("title", tx.title)
                        put("amount", tx.amount)
                        put("transactionType", tx.transactionType)
                        put("isIncome", tx.isIncome)
                        put("envelopeId", tx.envelopeId ?: JSONObject.NULL)
                        put("envelopeName", tx.envelopeName ?: "")
                        put("vatRate", tx.vatRate)
                        put("vatAmount", tx.vatAmount)
                        put("baseAmount", tx.baseAmount)
                        put("timestamp", tx.timestamp)
                    })
                }
                root.put("transactions", txArray)

                val envArray = JSONArray()
                for (env in envelopes.value) {
                    envArray.put(JSONObject().apply {
                        put("id", env.id)
                        put("name", env.name)
                        put("type", env.type)
                        put("vatRate", env.vatRate)
                        put("colorHex", env.colorHex)
                        put("monthlyBudget", env.monthlyBudget)
                    })
                }
                root.put("envelopes", envArray)
                root.put("exportedAt", System.currentTimeMillis())

                val file = File(context.cacheDir, "kurye_cuzdan_yedek.json")
                file.writeText(root.toString(2), Charsets.UTF_8)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "KuryeCüzdan JSON Yedek")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "JSON Yedeğini Paylaş / Kaydet"))
            } catch (e: Exception) {
                Toast.makeText(context, "Yedekleme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private val turkishSymbols = DecimalFormatSymbols(Locale.forLanguageTag("tr-TR")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        private val currencyFormat = DecimalFormat("#,##0.00 ₺", turkishSymbols)

        fun formatCurrency(amount: Double): String {
            return currencyFormat.format(amount)
        }
    }
}
