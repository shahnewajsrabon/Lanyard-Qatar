package com.example.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Composable
fun InventoryManagementModuleView() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Inventory & Raw Materials (ইনভেন্টরি ও কাঁচামাল)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val inventoryItems = listOf(
            Triple("Lanyard Ribbons (Blue)", "Low Stock", 150),
            Triple("Badge Holders (Plastic)", "In Stock", 2500),
            Triple("Lanyard Clips", "In Stock", 3000),
            Triple("Printer Ink (CMYK)", "Critical", 2)
        )

        items(inventoryItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.first, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Status: ${item.second}",
                            fontSize = 12.sp,
                            color = when (item.second) {
                                "Critical" -> MaterialTheme.colorScheme.error
                                "Low Stock" -> Color(0xFFE65100)
                                else -> Color(0xFF2E7D32)
                            }
                        )
                    }
                    Text(
                        text = "${item.third} Units",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
