package com.example.ui.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task

@Composable
fun ReportsAnalyticsModuleView(tasks: List<Task>) {
    val totalOrders = tasks.size
    val pending = tasks.count { it.status != "Done" }
    val avgQuantity = if (totalOrders > 0) tasks.sumOf { it.quantity } / totalOrders else 0
    val totalRevenue = tasks.sumOf { t ->
        val price = when (t.productType) {
            "Lanyard" -> 15
            "Wristband" -> 12
            "Badge" -> 20
            "Mousepad" -> 25
            else -> 15
        }
        t.quantity * price
    }

    val busiestProduct = tasks.groupBy { it.productType }.maxByOrNull { it.value.sumOf { k -> k.quantity } }?.key ?: "Lanyard"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Qatar Analytics Command Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Live manufacturing yields, revenue metrics, capacity planning benchmarks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Two-column responsive metric layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Monthly Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalRevenue QAR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF43A047))
                    Text("Interactive Real-time yield", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Top Demand Product", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(busiestProduct, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Highest volume requested", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pending In-Flight Orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$pending Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                    Text("Currently in pipeline", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Average Order Deal Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$avgQuantity pcs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("Consistent account demand", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Live Yield Chart Representation (Pure jetpack canvas drawing)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Operational Capacity & Yield Index", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        drawLine(Color.LightGray.copy(alpha = 0.5f), start = Offset(0f, height/2), end = Offset(width, height/2), strokeWidth = 1f)
                        drawLine(Color.LightGray.copy(alpha = 0.5f), start = Offset(0f, height), end = Offset(width, height), strokeWidth = 2f)
                        
                        val points = listOf(
                            Offset(width * 0.1f, height * 0.9f),
                            Offset(width * 0.3f, height * 0.7f),
                            Offset(width * 0.5f, height * 0.2f),
                            Offset(width * 0.7f, height * 0.6f),
                            Offset(width * 0.9f, height * 0.3f)
                        )
                        
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = Color(0xFF43A047),
                                start = points[i],
                                end = points[i+1],
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color(0xFF1B5E20),
                                radius = 8f,
                                center = points[i]
                            )
                        }
                        
                        drawCircle(
                            color = Color(0xFF1B5E20),
                            radius = 8f,
                            center = points.last()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sales Peak Week (1)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Qatar Expo peak (3)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                    Text("Current Week (5)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
