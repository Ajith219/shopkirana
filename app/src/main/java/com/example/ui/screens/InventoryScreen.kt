package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SuggestedItem
import com.example.data.local.ProductEntity
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.QuickPriceFixDialog
import com.example.ui.components.StockBadge
import com.example.ui.components.SuggestedCatalogDialog
import com.example.ui.theme.StockAmber
import java.util.Locale

@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    lowStockProducts: List<ProductEntity>,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onClearAllProducts: () -> Unit,
    onFixPrice: (productId: String, newPrice: Double) -> Unit,
    onUpdateStock: (productId: String, newStock: Double) -> Unit,
    onAddSuggestedItem: (item: SuggestedItem, fixedPrice: Double, initialStock: Double) -> Unit,
    onAddToCart: (ProductEntity, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showSuggestedDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var fixingPriceProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("All", "Grains", "Pulses", "Oils", "Spices", "Dairy", "Vegetables", "Essentials", "Beverages", "Household")

    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesSearch = query.isEmpty() ||
                product.nameEnglish.lowercase(Locale.ROOT).contains(query) ||
                product.nameTelugu.lowercase(Locale.ROOT).contains(query) ||
                product.aliases.lowercase(Locale.ROOT).contains(query)
        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSuggestedDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_from_catalog_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Suggested Menu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Action Header with Suggested Menu, Manual Add, and Clear All
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showSuggestedDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("open_suggested_menu_button")
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Suggested Menu", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showManualAddDialog = true },
                        modifier = Modifier.testTag("open_manual_add_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Manual Add", fontSize = 12.sp)
                    }
                }

                if (products.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showClearConfirmation = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("clear_all_inventory_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", fontSize = 11.sp)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("inventory_search_field"),
                placeholder = { Text("Search by Rice, బియ్యం, noone, sugar…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Low Stock Warning Banner
            if (lowStockProducts.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == "All") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = StockAmber.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StockAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${lowStockProducts.size} items have low stock / తక్కువ స్టాక్ ఉన్నవి",
                            color = StockAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Products Count Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Store Products (${filteredProducts.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap price to fix rate / ధరను సరిచేయండి",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Empty State
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching products found" else "Inventory is Empty / సరుకులు లేవు",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching with another keyword or clear search."
                            else "Add products using the Suggested Kirana Menu with fixed custom prices, or add items manually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showSuggestedDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suggested Menu (29+ Items)")
                            }

                            OutlinedButton(onClick = { showManualAddDialog = true }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Manually")
                            }
                        }
                    }
                }
            } else {
                // Products List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductInventoryCard(
                            product = product,
                            onFixPrice = { fixingPriceProduct = product },
                            onEdit = { editingProduct = product },
                            onDelete = { onDeleteProduct(product) },
                            onQuickRestock = { onUpdateStock(product.id, product.stockQuantity + 10.0) },
                            onAddToCart = { onAddToCart(product, 1.0) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                    }
                }
            }
        }
    }

    // Dialog 1: Suggested Kirana Catalog Picker
    if (showSuggestedDialog) {
        SuggestedCatalogDialog(
            existingProducts = products,
            onAddSuggestedItem = { item, fixedPrice, stock ->
                onAddSuggestedItem(item, fixedPrice, stock)
            },
            onDismiss = { showSuggestedDialog = false }
        )
    }

    // Dialog 2: Manual Add Product
    if (showManualAddDialog) {
        AddEditProductDialog(
            initialProduct = null,
            onSave = {
                onSaveProduct(it)
                showManualAddDialog = false
            },
            onDismiss = { showManualAddDialog = false }
        )
    }

    // Dialog 3: Edit Product
    if (editingProduct != null) {
        AddEditProductDialog(
            initialProduct = editingProduct,
            onSave = {
                onSaveProduct(it)
                editingProduct = null
            },
            onDismiss = { editingProduct = null }
        )
    }

    // Dialog 4: Quick Price Fix Dialog
    if (fixingPriceProduct != null) {
        QuickPriceFixDialog(
            product = fixingPriceProduct!!,
            onSavePrice = { newPrice ->
                onFixPrice(fixingPriceProduct!!.id, newPrice)
                fixingPriceProduct = null
            },
            onDismiss = { fixingPriceProduct = null }
        )
    }

    // Dialog 5: Clear All Confirmation
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Clear All Inventory Items?") },
            text = {
                Text("This will remove all items from your store inventory so you can start completely fresh with your own suggested or manual items.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllProducts()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProductInventoryCard(
    product: ProductEntity,
    onFixPrice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickRestock: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nameEnglish,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = product.nameTelugu,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Category: ${product.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Clickable Price Badge
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clickable(onClick = onFixPrice)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${formatAmount(product.pricePerUnit)}/${product.unit}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Fix Price",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    StockBadge(
                        quantity = product.stockQuantity,
                        unit = product.unit,
                        minThreshold = product.minStockThreshold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Fix Price, Edit, Quick Restock +10, Add to Bill, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onFixPrice,
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.testTag("fix_price_btn_${product.id}")
                ) {
                    Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Fix Price", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onQuickRestock,
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("+10", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(13.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Bill",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format("%.2f", amount)
}
