package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

sealed class CheckoutResult {
    data class Success(val billId: String, val billNumber: String, val totalAmount: Double) : CheckoutResult()
    data class InsufficientStock(val productName: String, val requested: Double, val available: Double, val unit: String) : CheckoutResult()
    data class Error(val message: String) : CheckoutResult()
}

@Dao
interface KiranaDao {

    @Query("SELECT * FROM products ORDER BY nameEnglish ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE nameEnglish LIKE '%' || :query || '%' OR nameTelugu LIKE '%' || :query || '%' OR aliases LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("UPDATE products SET pricePerUnit = :newPrice, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateProductPrice(productId: String, newPrice: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQuantity = :newStock, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Double, timestamp: Long = System.currentTimeMillis())

    @Transaction
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBillsWithItems(): Flow<List<BillWithItems>>

    @Transaction
    @Query("SELECT * FROM bills WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
    fun getBillsSince(startOfDayTimestamp: Long): Flow<List<BillWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun getBillCount(): Int

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    /**
     * Atomic checkout transaction:
     * 1. Validates stock for every item to prevent negative inventory
     * 2. Deducts stock from all products
     * 3. Inserts the bill and bill items
     * Entire transaction rolls back if any item has insufficient stock.
     */
    @Transaction
    suspend fun completeCheckout(bill: BillEntity, items: List<BillItemEntity>): CheckoutResult {
        if (items.isEmpty()) {
            return CheckoutResult.Error("Bill cannot be empty")
        }

        // Step 1: Strict verification of stock levels
        for (item in items) {
            val product = getProductById(item.productId)
                ?: return CheckoutResult.Error("Product not found: ${item.productName}")
            if (product.stockQuantity < item.quantity) {
                return CheckoutResult.InsufficientStock(
                    productName = product.nameEnglish,
                    requested = item.quantity,
                    available = product.stockQuantity,
                    unit = product.unit
                )
            }
        }

        // Step 2: Atomic stock deduction
        val now = System.currentTimeMillis()
        for (item in items) {
            val product = getProductById(item.productId)!!
            val updatedStock = (product.stockQuantity - item.quantity).coerceAtLeast(0.0)
            updateProduct(
                product.copy(
                    stockQuantity = updatedStock,
                    syncStatus = "PENDING_UPDATE",
                    updatedAt = now
                )
            )
        }

        // Step 3: Insert bill record and itemized lines
        insertBill(bill.copy(syncStatus = "PENDING_INSERT"))
        insertBillItems(items)

        return CheckoutResult.Success(
            billId = bill.id,
            billNumber = bill.billNumber,
            totalAmount = bill.totalAmount
        )
    }
}
