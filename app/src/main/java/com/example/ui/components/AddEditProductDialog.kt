package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ProductEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onSave: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var nameEnglish by remember { mutableStateOf(initialProduct?.nameEnglish ?: "") }
    var nameTelugu by remember { mutableStateOf(initialProduct?.nameTelugu ?: "") }
    var aliases by remember { mutableStateOf(initialProduct?.aliases ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Grains") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "kg") }
    var pricePerUnitText by remember { mutableStateOf(initialProduct?.pricePerUnit?.toString() ?: "50") }
    var stockQuantityText by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "20") }
    var minStockThresholdText by remember { mutableStateOf(initialProduct?.minStockThreshold?.toString() ?: "5") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Grains", "Pulses", "Oils", "Spices", "Dairy", "Vegetables", "Essentials", "Household")
    val units = listOf("kg", "g", "l", "ml", "pkt", "piece")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Product / కొత్త సరుకు" else "Edit Product / సవరించండి",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEnglish,
                    onValueChange = { nameEnglish = it },
                    label = { Text("Product Name (English)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name_en")
                )

                OutlinedTextField(
                    value = nameTelugu,
                    onValueChange = { nameTelugu = it },
                    label = { Text("Product Name (Telugu / తెలుగు పేరు)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name_te")
                )

                OutlinedTextField(
                    value = aliases,
                    onValueChange = { aliases = it },
                    label = { Text("Voice Search Aliases (comma-separated)") },
                    placeholder = { Text("e.g. biyyam, chawal, rice") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Unit dropdown
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pricePerUnitText,
                        onValueChange = { pricePerUnitText = it },
                        label = { Text("Price (₹)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_price")
                    )

                    OutlinedTextField(
                        value = stockQuantityText,
                        onValueChange = { stockQuantityText = it },
                        label = { Text("Stock Qty") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_stock")
                    )
                }

                OutlinedTextField(
                    value = minStockThresholdText,
                    onValueChange = { minStockThresholdText = it },
                    label = { Text("Low Stock Alert Threshold") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = pricePerUnitText.toDoubleOrNull() ?: 0.0
                    val stock = stockQuantityText.toDoubleOrNull() ?: 0.0
                    val threshold = minStockThresholdText.toDoubleOrNull() ?: 5.0

                    if (nameEnglish.isNotBlank()) {
                        val product = initialProduct?.copy(
                            nameEnglish = nameEnglish.trim(),
                            nameTelugu = nameTelugu.trim().ifBlank { nameEnglish.trim() },
                            aliases = aliases.trim(),
                            category = category,
                            unit = unit,
                            pricePerUnit = price,
                            stockQuantity = stock,
                            minStockThreshold = threshold,
                            updatedAt = System.currentTimeMillis()
                        ) ?: ProductEntity(
                            id = UUID.randomUUID().toString(),
                            nameEnglish = nameEnglish.trim(),
                            nameTelugu = nameTelugu.trim().ifBlank { nameEnglish.trim() },
                            aliases = aliases.trim(),
                            category = category,
                            unit = unit,
                            pricePerUnit = price,
                            stockQuantity = stock,
                            minStockThreshold = threshold,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(product)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Save / భద్రపరచు")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
