package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StockAmber
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed

@Composable
fun StockBadge(
    quantity: Double,
    unit: String,
    minThreshold: Double = 5.0,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        quantity <= 0 -> Triple(StockRed.copy(alpha = 0.15f), StockRed, "Out of Stock")
        quantity <= minThreshold -> Triple(StockAmber.copy(alpha = 0.15f), StockAmber, "Low: ${formatQty(quantity)} $unit")
        else -> Triple(StockGreen.copy(alpha = 0.15f), StockGreen, "${formatQty(quantity)} $unit")
    }

    Box(
        modifier = modifier
            .testTag("stock_badge")
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatQty(qty: Double): String {
    return if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
}
