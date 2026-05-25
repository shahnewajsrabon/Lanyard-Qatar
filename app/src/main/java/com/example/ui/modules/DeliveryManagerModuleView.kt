package com.example.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
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
fun DeliveryManagerModuleView(
    tasks: List<Task>,
    onUpdateDelivery: (Task, String, String, String, String) -> Unit,
    onTriggerAlert: (String) -> Unit
) {
    // Only display completed / ready orders in stage 4-5
    val deliveryOrders = remember(tasks) {
        tasks.filter { it.currentStage >= 4 }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text("Qatar Delivery Hub Dispatch System", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Coordinate local door-to-door deliveries, log photo confirmations, and message UDC", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (deliveryOrders.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Delivery", modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No dispatched deliveries available yet")
                            Text("Pending orders must complete physical Production & QC checklist first to reach dispatch queue.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                items(deliveryOrders) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        var localAddress by remember(task.id, task.deliveryAddress) { mutableStateOf(task.deliveryAddress) }
                        var localContact by remember(task.id, task.deliveryContact) { mutableStateOf(task.deliveryContact) }

                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Title row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = "Shipping", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Text("${task.clientName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Box(
                                    modifier = Modifier.background(Color(0xFFE8EAF6), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(task.deliveryStatus, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                                }
                            }

                            // Inputs for Logistics
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = localAddress,
                                    onValueChange = { ad -> localAddress = ad },
                                    label = { Text("Exact Maps Logistics Address", fontSize = 10.sp) },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (localAddress != task.deliveryAddress) {
                                            IconButton(
                                                onClick = { onUpdateDelivery(task, localAddress, localContact, task.deliveryStatus, task.deliveryPhotoUrl) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Save Address",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            onUpdateDelivery(task, localAddress, localContact, task.deliveryStatus, task.deliveryPhotoUrl)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                )

                                OutlinedTextField(
                                    value = localContact,
                                    onValueChange = { co -> localContact = co },
                                    label = { Text("Driver / Client Phone Number", fontSize = 10.sp) },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (localContact != task.deliveryContact) {
                                            IconButton(
                                                onClick = { onUpdateDelivery(task, localAddress, localContact, task.deliveryStatus, task.deliveryPhotoUrl) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Save Contact",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            onUpdateDelivery(task, localAddress, localContact, task.deliveryStatus, task.deliveryPhotoUrl)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                )
                            }

                            // Status Dispatch Pipeline
                            Text("Dispatch State:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Pending Dispatch", "Out for Delivery", "Delivered").forEach { ds ->
                                    Button(
                                        onClick = { onUpdateDelivery(task, localAddress, localContact, ds, task.deliveryPhotoUrl) },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (task.deliveryStatus == ds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (task.deliveryStatus == ds) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(ds, fontSize = 9.sp)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onTriggerAlert("Opening WhatsApp for Client: ${task.clientName} (+974 ${localContact})...") },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ChatBubble, contentDescription = "WA", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ping Client WA", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { onUpdateDelivery(task, localAddress, localContact, task.deliveryStatus, "photo_uploaded_mock_123.jpg") },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Photo", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (task.deliveryPhotoUrl.isEmpty()) "Log Photo Proof" else "Photo Logged", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
