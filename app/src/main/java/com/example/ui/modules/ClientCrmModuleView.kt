package com.example.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task

@Composable
fun ClientCrmModuleView(
    tasks: List<Task>,
    onTriggerAlert: (String) -> Unit,
    onSetReminder: (Task) -> Unit
) {
    // Unique clients in order lists
    val corporateClients = remember(tasks) {
        listOf(
            "UDC (Pearl Island)",
            "FM (Doha)",
            "Qatar Airways",
            "Ministry of Ed"
        )
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text("LanyardQatar CRM & Corporate Client Registry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Full account history, lifetime spending calculations, and upcoming event reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(corporateClients) { clientTag ->
                val clientOrders = tasks.filter { it.clientName.contains(clientTag.take(6), ignoreCase = true) }
                val totalVolume = clientOrders.sumOf { it.quantity }
                val estimatedSpent = clientOrders.sumOf { o ->
                    val pricePerUnit = when (o.productType) {
                        "Lanyard" -> 15
                        "Badge" -> 20
                        "Wristband" -> 12
                        "Mousepad" -> 25
                        else -> 15
                    }
                    o.quantity * pricePerUnit
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text(clientTag, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("Active Accounts Tracking Hub", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalVolume pcs", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Financial spent summary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated Lifetime Value (LTV):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("$estimatedSpent QAR", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = Color(0xFF43A047))
                        }

                        // Outreach alert
                        val reminderTask = clientOrders.firstOrNull { it.salesFollowUpRequired }
                        if (reminderTask != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                        Text("⏰ CRITICAL SALES OUTREACH OUTSTANDING:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(reminderTask.salesFollowUpNotes, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))
                                }
                            }
                        }

                        // Order history list
                        Text("Interactive Orders Log (${clientOrders.size} active):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            clientOrders.forEach { ord ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                    Text("• ${ord.title} (${ord.quantity} pcs)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Stage: ${ord.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val target = clientOrders.firstOrNull()
                            if (target != null) {
                                Button(
                                    onClick = { onSetReminder(target) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp).testTag("set_reminder_${target.id}")
                                ) {
                                    Text("Schedule Event Reminder Alert", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
