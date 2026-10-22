package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nameEnglish: String,
    val nameTelugu: String,
    val aliases: String = "", // Comma-separated search terms e.g. "biyyam, chawal, sonamasuri"
    val category: String,     // Grains, Pulses, Oils, Spices, Dairy, Snacks, Essentials
    val unit: String,         // kg, g, l, ml, pkt, piece
    val pricePerUnit: Double,
    val stockQuantity: Double,
    val minStockThreshold: Double = 5.0,
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_INSERT, PENDING_UPDATE
    val updatedAt: Long = System.currentTimeMillis()
)
