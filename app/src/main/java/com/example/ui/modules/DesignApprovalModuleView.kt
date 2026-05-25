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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task

@Composable
fun DesignApprovalModuleView(
    tasks: List<Task>,
    onApproveMockup: (Task, Boolean, String) -> Unit,
    onTriggerAlert: (String) -> Unit
) {
    var feedbackTextMap = remember { mutableStateMapOf<Int, String>() }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text("Creative Design & Client Review Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Revise print specs, generate approvals links, and toggle manufacturing locks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tasks) { task ->
                val fb = feedbackTextMap[task.id] ?: ""
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
                            Column {
                                Text("${task.companyName} Credentials Mockup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Product: ${task.quantity} ${task.productType}s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (task.designApproved) Color(0xFF43A047) else Color(0xFFC62828),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (task.designApproved) "Design Approved" else "Awaiting Approval",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Simulated Mockup Graphics Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Lanyard Print Mockup [Primary Vector Spec]", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Format: Vector Sublimation | Width: 20mm | Double Safety Buckle", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Mock URL ID: https://lanyardqatar.com/preview/${task.id * 89}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (task.designFeedback.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Latest Revision Comments:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(task.designFeedback, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        // Locking Warning Notification
                        if (!task.designApproved) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                                    Text("🔒 Lanyard Production Locked: This order cannot be promoted to manufacturing steps until the client approves this mockup.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }

                        // Input Feedback revision line
                        OutlinedTextField(
                            value = fb,
                            onValueChange = { feedbackTextMap[task.id] = it },
                            placeholder = { Text("E.g., Revision requested: Make logo 10% larger...") },
                            singleLine = true,
                            label = { Text("Client Feedback / Revision Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action Controllers: Generate link, Approve, Request revisions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onTriggerAlert("Shareable link copied: https://lanyardqatar.com/approve/${task.id * 102} (Client form accessible online)")
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Approval Link", fontSize = 10.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val comments = fb.ifBlank { "Client requested design revisions." }
                                        onApproveMockup(task, false, comments)
                                        feedbackTextMap[task.id] = ""
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Reject / Edit Mode", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        onApproveMockup(task, true, "Approved instantly via operations master panel.")
                                        feedbackTextMap[task.id] = ""
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                    modifier = Modifier.height(32.dp).testTag("approve_design_btn_${task.id}")
                                ) {
                                    Text("Approve Design", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
