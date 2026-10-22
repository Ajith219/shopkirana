package com.example.data.repository

import com.example.data.local.BillEntity
import com.example.data.local.BillItemEntity
import com.example.data.local.BillWithItems
import com.example.data.local.CheckoutResult
import com.example.data.local.DailySalesSummary
import com.example.data.local.KiranaDao
import com.example.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class KiranaRepository(private val dao: KiranaDao) {

    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val allBills: Flow<List<BillWithItems>> = dao.getAllBillsWithItems()

    suspend fun getProductById(id: String): ProductEntity? = dao.getProductById(id)

    suspend fun searchProducts(query: String): List<ProductEntity> = dao.searchProducts(query)

    suspend fun insertProduct(product: ProductEntity) = dao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    suspend fun deleteAllProducts() = dao.deleteAllProducts()

    suspend fun updateProductPrice(productId: String, newPrice: Double) = dao.updateProductPrice(productId, newPrice)

    suspend fun updateStock(productId: String, newStock: Double) = dao.updateStock(productId, newStock)

    suspend fun completeCheckout(bill: BillEntity, items: List<BillItemEntity>): CheckoutResult {
        return dao.completeCheckout(bill, items)
    }

    suspend fun ensureSampleProductsLoaded() {
        if (dao.getProductCount() == 0) {
            com.example.data.local.KiranaDatabase.populateInitialGroceryItems(dao)
        }
    }

    /**
     * Compute today's sales summary (from 00:00 midnight of the current day)
     */
    fun getTodaySalesSummary(): Flow<DailySalesSummary> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        return dao.getBillsSince(startOfDay).map { bills ->
            var revenue = 0.0
            var units = 0.0
            val productCountMap = mutableMapOf<String, Double>()

            for (billWithItems in bills) {
                revenue += billWithItems.bill.totalAmount
                for (item in billWithItems.items) {
                    units += item.quantity
                    val currentCount = productCountMap[item.productName] ?: 0.0
                    productCountMap[item.productName] = currentCount + item.quantity
                }
            }

            val topProduct = productCountMap.maxByOrNull { it.value }?.key

            DailySalesSummary(
                totalRevenue = revenue,
                billCount = bills.size,
                totalUnitsSold = units,
                topSellingProductName = topProduct
            )
        }
    }
}
