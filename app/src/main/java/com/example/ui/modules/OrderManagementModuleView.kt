package com.example.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task

@Composable
fun OrderManagementModuleView(
    tasks: List<Task>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddNewOrderClicked: () -> Unit,
    onUpdateStage: (Task, Int) -> Unit,
    onUpdateStatus: (Task, String) -> Unit,
    onDeleteOrder: (Int, String) -> Unit
) {
    val filtered = remember(tasks, searchQuery) {
        tasks.filter {
            it.clientName.contains(searchQuery, ignoreCase = true) ||
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.productType.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Inquiry & Order Pipeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Digitized orders, status tracking, and stage promotions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onAddNewOrderClicked,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("log_inquiry_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Order", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Inquiry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Filter order by Client Name or Product...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("order_search_bar")
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Task, contentDescription = "None", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No matching digitized orders found", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Click 'Log Inquiry' in the top right to start a new client workspace lifecycle", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { task ->
                    val now = System.currentTimeMillis()
                    val isAtRisk = task.dueDate < now + (3 * 24 * 60 * 60 * 1000L) && task.status != "Done"
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAtRisk) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Client and Product Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val icon = when (task.productType) {
                                        "Lanyard" -> Icons.Default.Task
                                        "Badge" -> Icons.Default.Brush
                                        "Wristband" -> Icons.Default.Build
                                        else -> Icons.Default.Task
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text(task.clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(task.companyName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Status Indicator
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (task.status) {
                                                "New" -> Color(0xFF1E88E5)
                                                "Quoted" -> Color(0xFFFFB300)
                                                "Confirmed" -> Color(0xFF43A047)
                                                "In Production" -> Color(0xFFEF6C00)
                                                else -> Color(0xFF5E35B1)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(task.status, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Order Title and Details
                            Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // Metadata Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column {
                                    Text("Product Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${task.quantity} ${task.productType}s", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Client Approve", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (task.designApproved) "✅ Approved" else "🔒 Pending",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.designApproved) Color(0xFF43A047) else Color(0xFFC62828)
                                    )
                                }
                                Column {
                                    Text("Target Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val daysDiff = ((task.dueDate - now) / (1000 * 60 * 60 * 24)).toInt()
                                    Text(
                                        text = if (daysDiff < 0) "Completed" else "$daysDiff Days left",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (daysDiff <= 3 && task.status != "Done") Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Urgent At Risk Badge
                            if (isAtRisk) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = "Risk", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                                        Text("🚨 RED FLAG DEADLINE ALERT: Order at risk of missing Qatar Delivery schedule! Advance immediately.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }

                            // Progressive 5-Stage Segment Tracker
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Workflow Stage Progress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Stage ${task.currentStage}/5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val stages = listOf("Quoting", "Design", "Production", "QC", "Delivery")
                                    stages.forEachIndexed { idx, stageLabel ->
                                        val stageNum = idx + 1
                                        val isCompleted = task.currentStage >= stageNum
                                        val isCurrent = task.currentStage == stageNum
                                        
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .background(
                                                        color = when {
                                                            isCurrent -> MaterialTheme.colorScheme.primary
                                                            isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                                        },
                                                        shape = RoundedCornerShape(3.dp)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = stageLabel,
                                                fontSize = 9.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Operation Changers Bar (One-Click Operations Promotion)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onDeleteOrder(task.id, task.title) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Cancel Order", fontSize = 11.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (task.currentStage > 1) {
                                        OutlinedButton(
                                            onClick = { onUpdateStage(task, (task.currentStage - 1).coerceAtLeast(1)) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Back Stage", fontSize = 10.sp)
                                        }
                                    }
                                    if (task.currentStage < 5) {
                                        Button(
                                            onClick = { onUpdateStage(task, (task.currentStage + 1).coerceAtMost(5)) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp).testTag("promote_stage_${task.id}")
                                        ) {
                                            Text("Promote Step", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
