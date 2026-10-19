package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.model.EnvelopeEntity
import com.example.data.repository.CourierRepository
import com.example.ui.CourierViewModel
import com.example.ui.components.AddEditEnvelopeDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.ArchiveDialog
import com.example.ui.components.EnvelopeDetailDialog
import com.example.ui.components.QuickVatCalculatorDialog
import com.example.ui.components.ScanReceiptDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.EnvelopesScreen
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.CourierOrange
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repo = CourierRepository(db.envelopeDao(), db.transactionDao())

        setContent {
            MyApplicationTheme {
                val viewModel: CourierViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CourierViewModel(repo, applicationContext) as T
                        }
                    }
                )
                CourierApp(viewModel)
            }
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Ana Sayfa", Icons.Default.Home),
    ENVELOPES("Zarflar", Icons.Default.MailOutline),
    ANALYTICS("Analiz", Icons.Default.Analytics),
    ASSISTANT("Gemini", Icons.Default.AutoAwesome)
}

@Composable
fun CourierApp(viewModel: CourierViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    val summary by viewModel.summary.collectAsState()
    val courierName by viewModel.courierName.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val envelopes by viewModel.envelopes.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    // Dialog States
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showScanReceiptDialog by remember { mutableStateOf(false) }
    var showVatCalculatorDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddEnvelopeDialog by remember { mutableStateOf(false) }
    var selectedEnvelopeForDetail by remember { mutableStateOf<EnvelopeEntity?>(null) }
    var envelopeToEdit by remember { mutableStateOf<EnvelopeEntity?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack),
        containerColor = PureBlack,
        bottomBar = {
            CourierBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onScanReceiptClick = { showScanReceiptDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        summary = summary,
                        courierName = courierName,
                        recentTransactions = recentTransactions,
                        envelopes = envelopes,
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenArchive = { showArchiveDialog = true },
                        onOpenScanReceipt = { showScanReceiptDialog = true },
                        onOpenAddTransaction = { showAddTransactionDialog = true },
                        onOpenVatCalculator = { showVatCalculatorDialog = true },
                        onAddQuickTip = { viewModel.addQuickTip(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                }

                NavigationTab.ENVELOPES -> {
                    EnvelopesScreen(
                        envelopes = envelopes,
                        transactions = allTransactions,
                        onOpenAddEnvelope = {
                            envelopeToEdit = null
                            showAddEnvelopeDialog = true
                        },
                        onEnvelopeClick = { selectedEnvelopeForDetail = it }
                    )
                }

                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(
                        summary = summary,
                        envelopes = envelopes,
                        transactions = allTransactions
                    )
                }

                NavigationTab.ASSISTANT -> {
                    GeminiChatScreen(
                        messages = chatMessages,
                        isLoading = isChatLoading,
                        summary = summary,
                        isApiKeyConfigured = viewModel.getEffectiveApiKey().isNotBlank(),
                        onOpenSettings = { showSettingsDialog = true },
                        onSendMessage = { viewModel.sendChatMessage(it) }
                    )
                }
            }
        }
    }

    // Dialog: Add Transaction
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            envelopes = envelopes,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { title, amount, isIncome, envId, envName, vatRate, note, txType ->
                viewModel.addTransaction(title, amount, isIncome, envId, envName, vatRate, note, txType)
            }
        )
    }

    // Dialog: Scan Receipt
    if (showScanReceiptDialog) {
        ScanReceiptDialog(
            envelopes = envelopes,
            scanState = scanState,
            isApiKeyConfigured = viewModel.getEffectiveApiKey().isNotBlank(),
            onOpenSettings = {
                showScanReceiptDialog = false
                showSettingsDialog = true
            },
            onDismiss = {
                viewModel.clearScanState()
                showScanReceiptDialog = false
            },
            onScanImage = { bmp -> viewModel.scanReceiptImage(bmp) },
            onConfirmParsed = { title, amount, envId, envName, vatRate, note ->
                viewModel.addTransaction(title, amount, false, envId, envName, vatRate, note)
                viewModel.clearScanState()
            }
        )
    }

    // Dialog: VAT Calculator
    if (showVatCalculatorDialog) {
        QuickVatCalculatorDialog(
            onDismiss = { showVatCalculatorDialog = false },
            onUseAsTransaction = { amount, vatRate ->
                viewModel.addTransaction(
                    title = "KDV Hesaplanan Harcama",
                    amount = amount,
                    isIncome = false,
                    envelopeId = envelopes.firstOrNull { it.type == "COMPANY" }?.id,
                    envelopeName = envelopes.firstOrNull { it.type == "COMPANY" }?.name,
                    vatRate = vatRate
                )
            }
        )
    }

    // Dialog: Archive & Past Months
    if (showArchiveDialog) {
        ArchiveDialog(
            transactions = allTransactions,
            envelopes = envelopes,
            onDismiss = { showArchiveDialog = false },
            onExportCsv = { viewModel.exportTransactionsCsv(context) },
            onExportJson = { viewModel.exportBackupJson(context) }
        )
    }

    // Dialog: Settings
    if (showSettingsDialog) {
        SettingsDialog(
            currentName = courierName,
            currentApiKey = geminiApiKey,
            companyReceivable = summary.companyReceivable,
            paidHakedis = summary.paidHakedis,
            onDismiss = { showSettingsDialog = false },
            onSaveName = { viewModel.setCourierName(it) },
            onSaveApiKey = { viewModel.saveGeminiApiKey(it) },
            onClearApiKey = { viewModel.clearGeminiApiKey() },
            onConfirmHakedisPayout = { viewModel.confirmHakedisPayout() },
            onResetData = { viewModel.resetAllData() }
        )
    }

    // Dialog: Add / Edit Envelope
    if (showAddEnvelopeDialog) {
        AddEditEnvelopeDialog(
            initialEnvelope = envelopeToEdit,
            onDismiss = { showAddEnvelopeDialog = false },
            onSave = { name, type, vatRate, colorHex, budget ->
                if (envelopeToEdit != null) {
                    viewModel.updateEnvelope(
                        envelopeToEdit!!.copy(
                            name = name,
                            type = type,
                            vatRate = vatRate,
                            colorHex = colorHex,
                            monthlyBudget = budget
                        )
                    )
                } else {
                    viewModel.addEnvelope(name, type, vatRate, colorHex, budget)
                }
            }
        )
    }

    // Dialog: Envelope Detail
    selectedEnvelopeForDetail?.let { env ->
        EnvelopeDetailDialog(
            envelope = env,
            transactions = allTransactions,
            onDismiss = { selectedEnvelopeForDetail = null },
            onEdit = {
                envelopeToEdit = env
                selectedEnvelopeForDetail = null
                showAddEnvelopeDialog = true
            },
            onDeleteEnvelope = {
                viewModel.deleteEnvelope(env.id)
                selectedEnvelopeForDetail = null
            },
            onDeleteTransaction = { txId -> viewModel.deleteTransaction(txId) }
        )
    }
}

@Composable
fun CourierBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onScanReceiptClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = DarkSurface,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = DarkBorder)
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val isAssistant = tab == NavigationTab.ASSISTANT

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isAssistant && isSelected -> PastelPurple.copy(alpha = 0.35f)
                                        isAssistant -> PastelPurple.copy(alpha = 0.15f)
                                        isSelected -> CourierOrange.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = when {
                                    isAssistant -> PastelPurple
                                    isSelected -> CourierOrange
                                    else -> TextSecondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.label,
                            color = when {
                                isAssistant && isSelected -> PastelPurple
                                isSelected -> CourierOrange
                                else -> TextTertiary
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 5. Fixed Camera Scan Receipt Action on bottom right
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onScanReceiptClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("nav_camera_scan_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CourierOrange.copy(alpha = 0.15f))
                            .border(1.dp, CourierOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Fiş Tara",
                            tint = CourierOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Fiş Tara",
                        color = CourierOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
