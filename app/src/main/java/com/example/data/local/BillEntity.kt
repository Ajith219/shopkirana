package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val billNumber: String, // e.g. "INV-1001"
    val totalAmount: Double,
    val itemCount: Int,
    val paymentMode: String = "CASH", // CASH, UPI, KHATA
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["billId"])]
)
data class BillItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val billId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val subtotal: Double
)
