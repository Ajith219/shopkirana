package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BillEntity
import com.example.data.local.BillItemEntity
import com.example.data.local.BillWithItems
import com.example.data.local.CheckoutResult
import com.example.data.local.DailySalesSummary
import com.example.data.local.KiranaDatabase
import com.example.data.local.ProductEntity
import com.example.data.repository.KiranaRepository
import com.example.voice.ParsedVoiceAction
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.SpeechRecognitionState
import com.example.voice.VoiceActionType
import com.example.voice.VoiceCommandProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    BILLING,
    INVENTORY,
    SALES
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Double,
    val unitPrice: Double
) {
    val subtotal: Double get() = quantity * unitPrice
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KiranaDatabase.getDatabase(application, viewModelScope)
    val repository = KiranaRepository(database.kiranaDao())

    val speechHelper = SpeechRecognizerHelper(application)
    private val commandProcessor = VoiceCommandProcessor()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.BILLING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Active Billing Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems.combine(MutableStateFlow(Unit)) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = _cartItems.combine(MutableStateFlow(Unit)) { items, _ ->
        items.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Inventory & Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Daily Sales & Bill History
    val todaySalesSummary: StateFlow<DailySalesSummary> = repository.getTodaySalesSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySalesSummary())

    val allBills: StateFlow<List<BillWithItems>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice & Ambiguity Handling
    val speechState: StateFlow<SpeechRecognitionState> = speechHelper.recognitionState
    val rmsAudioLevel: StateFlow<Float> = speechHelper.rmsAudioLevel

    private val _activeVoiceAction = MutableStateFlow<ParsedVoiceAction?>(null)
    val activeVoiceAction: StateFlow<ParsedVoiceAction?> = _activeVoiceAction.asStateFlow()

    private val _stockLookupOverlay = MutableStateFlow<Pair<ProductEntity, Double>?>(null)
    val stockLookupOverlay: StateFlow<Pair<ProductEntity, Double>?> = _stockLookupOverlay.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Checkout & Dialogs
    private val _lastCompletedBill = MutableStateFlow<BillWithItems?>(null)
    val lastCompletedBill: StateFlow<BillWithItems?> = _lastCompletedBill.asStateFlow()

    private val _checkoutShortageError = MutableStateFlow<CheckoutResult.InsufficientStock?>(null)
    val checkoutShortageError: StateFlow<CheckoutResult.InsufficientStock?> = _checkoutShortageError.asStateFlow()

    private var nextBillNumber = 1001

    init {
        speechHelper.setOnSpeechResultListener { transcript: String ->
            handleVoiceTranscript(transcript)
        }
    }

    // Voice Processing Logic
    fun startVoiceListening(preferTelugu: Boolean = true) {
        speechHelper.startListening(preferTelugu)
    }

    fun stopVoiceListening() {
        speechHelper.stopListening()
    }

    fun simulateVoiceCommand(text: String) {
        speechHelper.simulateVoiceInput(text)
    }

    fun handleVoiceTranscript(transcript: String) {
        val currentProducts = allProducts.value
        val parsedAction = commandProcessor.processCommand(transcript, currentProducts)

        _userFeedbackMessage.value = parsedAction.feedbackMessage

        when (parsedAction.actionType) {
            VoiceActionType.SHOW_DAILY_SALES -> {
                _currentScreen.value = AppScreen.SALES
                _activeVoiceAction.value = null
            }
            VoiceActionType.QUERY_STOCK -> {
                if (parsedAction.requiresConfirmation) {
                    _activeVoiceAction.value = parsedAction
                } else if (parsedAction.targetProduct != null) {
                    _stockLookupOverlay.value = Pair(parsedAction.targetProduct, parsedAction.targetProduct.stockQuantity)
                    _activeVoiceAction.value = null
                }
            }
            VoiceActionType.ADD_ITEM -> {
                if (parsedAction.requiresConfirmation) {
                    _activeVoiceAction.value = parsedAction
                } else if (parsedAction.targetProduct != null) {
                    // Safe direct add with auto toast
                    addToCart(parsedAction.targetProduct, parsedAction.extractedQuantity)
                    _activeVoiceAction.value = null
                }
            }
            VoiceActionType.COMPLETE_BILL -> {
                _activeVoiceAction.value = parsedAction
            }
            VoiceActionType.CLEAR_BILL -> {
                _activeVoiceAction.value = parsedAction
            }
            else -> {
                _activeVoiceAction.value = null
            }
        }
    }

    fun confirmVoiceAction(action: ParsedVoiceAction, selectedProduct: ProductEntity? = null) {
        when (action.actionType) {
            VoiceActionType.ADD_ITEM -> {
                val productToAdd = selectedProduct ?: action.targetProduct
                if (productToAdd != null) {
                    addToCart(productToAdd, action.extractedQuantity)
                }
            }
            VoiceActionType.QUERY_STOCK -> {
                val product = selectedProduct ?: action.targetProduct
                if (product != null) {
                    _stockLookupOverlay.value = Pair(product, product.stockQuantity)
                }
            }
            VoiceActionType.COMPLETE_BILL -> {
                completeBill()
            }
            VoiceActionType.CLEAR_BILL -> {
                clearCart()
            }
            else -> {}
        }
        _activeVoiceAction.value = null
    }

    fun dismissVoiceDialog() {
        _activeVoiceAction.value = null
        speechHelper.resetState()
    }

    fun dismissStockLookup() {
        _stockLookupOverlay.value = null
    }

    fun dismissFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun dismissCompletedBillReceipt() {
        _lastCompletedBill.value = null
    }

    fun dismissCheckoutShortageError() {
        _checkoutShortageError.value = null
    }

    // Cart Operations
    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }

        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.quantity + quantity
            current[index] = existing.copy(quantity = newQty)
        } else {
            current.add(CartItem(product = product, quantity = quantity, unitPrice = product.pricePerUnit))
        }
        _cartItems.value = current
        _currentScreen.value = AppScreen.BILLING
    }

    fun updateCartItemQuantity(productId: String, newQty: Double) {
        if (newQty <= 0.0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQty)
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Atomic Checkout Operation
    fun completeBill(paymentMode: String = "CASH") {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val billId = UUID.randomUUID().toString()
        val billNum = "INV-${nextBillNumber++}"
        val total = items.sumOf { it.subtotal }

        val billEntity = BillEntity(
            id = billId,
            billNumber = billNum,
            totalAmount = total,
            itemCount = items.size,
            paymentMode = paymentMode,
            timestamp = System.currentTimeMillis()
        )

        val billItemEntities = items.map { item ->
            BillItemEntity(
                id = UUID.randomUUID().toString(),
                billId = billId,
                productId = item.product.id,
                productName = item.product.nameEnglish,
                quantity = item.quantity,
                unit = item.product.unit,
                unitPrice = item.unitPrice,
                subtotal = item.subtotal
            )
        }

        viewModelScope.launch {
            val result = repository.completeCheckout(billEntity, billItemEntities)
            when (result) {
                is CheckoutResult.Success -> {
                    _lastCompletedBill.value = BillWithItems(bill = billEntity, items = billItemEntities)
                    clearCart()
                    _checkoutShortageError.value = null
                }
                is CheckoutResult.InsufficientStock -> {
                    _checkoutShortageError.value = result
                }
                is CheckoutResult.Error -> {
                    _userFeedbackMessage.value = result.message
                }
            }
        }
    }

    // Inventory Management
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun updateStock(productId: String, newStock: Double) {
        viewModelScope.launch {
            repository.updateStock(productId, newStock)
        }
    }

    fun clearAllInventory() {
        viewModelScope.launch {
            repository.deleteAllProducts()
            _userFeedbackMessage.value = "All items removed / సరుకులు తొలగించబడ్డాయి. Inventory is clean."
        }
    }

    fun fixProductPrice(productId: String, newPrice: Double) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, newPrice)
            _userFeedbackMessage.value = "Price updated to ₹${if (newPrice % 1.0 == 0.0) newPrice.toInt() else String.format("%.2f", newPrice)}"
        }
    }

    fun addSuggestedProduct(suggestedItem: com.example.data.SuggestedItem, price: Double, stock: Double) {
        val product = ProductEntity(
            id = java.util.UUID.randomUUID().toString(),
            nameEnglish = suggestedItem.nameEnglish,
            nameTelugu = suggestedItem.nameTelugu,
            aliases = suggestedItem.aliases,
            category = suggestedItem.category,
            unit = suggestedItem.unit,
            pricePerUnit = price,
            stockQuantity = stock,
            minStockThreshold = 5.0,
            syncStatus = "PENDING_INSERT",
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertProduct(product)
            val formatted = if (price % 1.0 == 0.0) price.toInt().toString() else String.format("%.2f", price)
            _userFeedbackMessage.value = "Added ${product.nameEnglish} at ₹$formatted/${product.unit}"
        }
    }
}
