package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class BillWithItems(
    @Embedded
    val bill: BillEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val items: List<BillItemEntity>
)

data class DailySalesSummary(
    val totalRevenue: Double = 0.0,
    val billCount: Int = 0,
    val totalUnitsSold: Double = 0.0,
    val topSellingProductName: String? = null
)
