package com.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BillWithItems
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BillReceiptDialog
import com.example.ui.components.StockLookupOverlay
import com.example.ui.components.StockShortageDialog
import com.example.ui.components.SupabaseSyncSheet
import com.example.ui.components.VoiceConfirmationDialog
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.theme.KiranaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KiranaTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                // Audio Record permission launcher
                var hasAudioPermission by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                    if (!isGranted) {
                        Toast.makeText(
                            context,
                            "Microphone permission is required for voice commands. You can also use Quick Voice buttons.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }

                // Observables from ViewModel
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
                val cartItemCount by viewModel.cartItemCount.collectAsStateWithLifecycle()

                val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
                val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
                val searchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

                val todaySummary by viewModel.todaySalesSummary.collectAsStateWithLifecycle()
                val allBills by viewModel.allBills.collectAsStateWithLifecycle()

                val speechState by viewModel.speechState.collectAsStateWithLifecycle()
                val rmsLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
                val activeVoiceAction by viewModel.activeVoiceAction.collectAsStateWithLifecycle()
                val stockLookup by viewModel.stockLookupOverlay.collectAsStateWithLifecycle()
                val completedReceipt by viewModel.lastCompletedBill.collectAsStateWithLifecycle()
                val shortageError by viewModel.checkoutShortageError.collectAsStateWithLifecycle()
                val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

                // Supabase Sync Sheet State
                var showSupabaseSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                var viewingBillReceipt by remember { mutableStateOf<BillWithItems?>(null) }

                // Feedback snackbar
                LaunchedEffect(feedbackMessage) {
                    feedbackMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.dismissFeedbackMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "KiranaVoice / కిరాణా వాయిస్",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSupabaseSheet = true },
                                    modifier = Modifier.testTag("open_supabase_sync_header_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Supabase Architecture",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.BILLING,
                                onClick = { viewModel.navigateTo(AppScreen.BILLING) },
                                icon = {
                                    if (cartItemCount > 0) {
                                        BadgedBox(badge = { Badge { Text(cartItemCount.toString()) } }) {
                                            Icon(Icons.Default.PointOfSale, contentDescription = "Billing")
                                        }
                                    } else {
                                        Icon(Icons.Default.PointOfSale, contentDescription = "Billing")
                                    }
                                },
                                label = { Text("Billing / బిల్లింగ్", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_billing_tab")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.INVENTORY,
                                onClick = { viewModel.navigateTo(AppScreen.INVENTORY) },
                                icon = {
                                    if (lowStockProducts.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                    Text(lowStockProducts.size.toString())
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Inventory2, contentDescription = "Inventory")
                                        }
                                    } else {
                                        Icon(Icons.Default.Inventory2, contentDescription = "Inventory")
                                    }
                                },
                                label = { Text("Inventory / స్టాక్", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_inventory_tab")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SALES,
                                onClick = { viewModel.navigateTo(AppScreen.SALES) },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Sales") },
                                label = { Text("Sales / అమ్మకాలు", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_sales_tab")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            AppScreen.BILLING -> {
                                BillingScreen(
                                    cartItems = cartItems,
                                    cartTotal = cartTotal,
                                    availableProducts = allProducts,
                                    speechState = speechState,
                                    rmsLevel = rmsLevel,
                                    onStartVoice = { preferTelugu ->
                                        if (hasAudioPermission) {
                                            viewModel.startVoiceListening(preferTelugu)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onStopVoice = { viewModel.stopVoiceListening() },
                                    onSimulateCommand = { viewModel.simulateVoiceCommand(it) },
                                    onAddToCart = { product, qty -> viewModel.addToCart(product, qty) },
                                    onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                                    onRemoveFromCart = { viewModel.removeFromCart(it) },
                                    onClearCart = { viewModel.clearCart() },
                                    onCompleteBill = { mode -> viewModel.completeBill(mode) }
                                )
                            }
                            AppScreen.INVENTORY -> {
                                InventoryScreen(
                                    products = allProducts,
                                    lowStockProducts = lowStockProducts,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    onSearchQueryChange = { viewModel.setInventorySearchQuery(it) },
                                    onCategoryChange = { viewModel.setSelectedCategory(it) },
                                    onSaveProduct = { viewModel.saveProduct(it) },
                                    onDeleteProduct = { viewModel.deleteProduct(it) },
                                    onClearAllProducts = { viewModel.clearAllInventory() },
                                    onFixPrice = { id, price -> viewModel.fixProductPrice(id, price) },
                                    onUpdateStock = { id, stock -> viewModel.updateStock(id, stock) },
                                    onAddSuggestedItem = { item, price, stock -> viewModel.addSuggestedProduct(item, price, stock) },
                                    onAddToCart = { product, qty -> viewModel.addToCart(product, qty) }
                                )
                            }
                            AppScreen.SALES -> {
                                SalesScreen(
                                    summary = todaySummary,
                                    bills = allBills,
                                    onViewBill = { viewingBillReceipt = it },
                                    onOpenSupabaseSync = { showSupabaseSheet = true }
                                )
                            }
                        }
                    }

                    // Dialog 1: Voice Confirmation & Disambiguation Dialog
                    activeVoiceAction?.let { action ->
                        VoiceConfirmationDialog(
                            action = action,
                            onConfirm = { selectedProduct ->
                                viewModel.confirmVoiceAction(action, selectedProduct)
                            },
                            onDismiss = { viewModel.dismissVoiceDialog() }
                        )
                    }

                    // Dialog 2: Instant Voice Stock Lookup Overlay
                    stockLookup?.let { (product, stock) ->
                        StockLookupOverlay(
                            product = product,
                            currentStock = stock,
                            onAddToCart = { viewModel.addToCart(it, 1.0) },
                            onDismiss = { viewModel.dismissStockLookup() }
                        )
                    }

                    // Dialog 3: Newly Completed Bill Receipt (Automatic after checkout)
                    completedReceipt?.let { billWithItems ->
                        BillReceiptDialog(
                            billWithItems = billWithItems,
                            onDismiss = { viewModel.dismissCompletedBillReceipt() },
                            onShareBill = { text ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Kirana Bill - ${billWithItems.bill.billNumber}")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Kirana Bill"))
                            }
                        )
                    }

                    // Dialog 4: Historical Bill Receipt (When clicked from Sales history)
                    viewingBillReceipt?.let { billWithItems ->
                        BillReceiptDialog(
                            billWithItems = billWithItems,
                            onDismiss = { viewingBillReceipt = null },
                            onShareBill = { text ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Kirana Bill - ${billWithItems.bill.billNumber}")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Kirana Bill"))
                            }
                        )
                    }

                    // Dialog 5: Stock Shortage Warning
                    shortageError?.let { shortage ->
                        StockShortageDialog(
                            shortage = shortage,
                            onDismiss = { viewModel.dismissCheckoutShortageError() }
                        )
                    }

                    // Modal Bottom Sheet: Supabase Sync Architecture
                    if (showSupabaseSheet) {
                        SupabaseSyncSheet(
                            sheetState = sheetState,
                            totalProductsCount = allProducts.size,
                            totalBillsCount = allBills.size,
                            onDismiss = { showSupabaseSheet = false }
                        )
                    }
                }
            }
        }
    }
}
