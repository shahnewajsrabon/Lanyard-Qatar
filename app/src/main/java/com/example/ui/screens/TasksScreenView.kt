package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.WorkspaceViewModel
import com.example.ui.modules.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenViewReal(viewModel: WorkspaceViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    // Determine available modules for the active role
    val availableModules = remember(currentUserRole) {
        when (currentUserRole) {
            "Sales" -> listOf("Order Tracking", "Client CRM", "Reports & Analytics")
            "Design" -> listOf("Design Preview")
            "Production" -> listOf("Production & Delivery", "Inventory Management", "Employee Tasks")
            else -> listOf("Order Tracking", "Design Preview", "Production & Delivery", "Inventory Management", "Client CRM", "Reports & Analytics", "Employee Tasks")
        }
    }

    // Default to the first allowed module if active module becomes unavailable on role switch
    var activeTab by remember(currentUserRole) { mutableStateOf(availableModules.first()) }
    
    // Core states
    var searchQuery by remember { mutableStateOf("") }
    var showCreateOrderDialog by remember { mutableStateOf(false) }
    var showCrmFollowUpDialog by remember { mutableStateOf<Task?>(null) }
    
    // Delivery Photo Upload Simulation
    var showDeliveryPhotoDialog by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Role Indicator Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE53935), CircleShape)
                    )
                    Text(
                        text = "Access Mode: $currentUserRole Space",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Horizontal Scrollable Ribbon for Modules
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableModules.forEach { moduleName ->
                val isSelected = activeTab == moduleName
                val icon = when (moduleName) {
                    "Order Tracking", "Order Management" -> Icons.Default.Task
                    "Design Preview", "Design & Approval" -> Icons.Default.Brush
                    "Production & Delivery", "Production Tracker" -> Icons.Default.Build
                    "Inventory Management" -> Icons.Default.Inventory
                    "Delivery Manager" -> Icons.Default.LocalShipping
                    "Client CRM" -> Icons.Default.Business
                    "Reports & Analytics" -> Icons.Default.Analytics
                    "Employee Tasks" -> Icons.Default.Assignment
                    else -> Icons.Default.Task
                }
                
                FilterChip(
                    selected = isSelected,
                    onClick = { activeTab = moduleName },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(icon, contentDescription = moduleName, modifier = Modifier.size(16.dp))
                            Text(moduleName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("module_tab_$moduleName")
                )
            }
        }

        // Horizontal Divider
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

        // Render Active Module Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                "Order Tracking", "Order Management" -> {
                    OrderManagementModuleView(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onAddNewOrderClicked = { showCreateOrderDialog = true },
                        onUpdateStage = { task, stage -> viewModel.updateTaskStage(task, stage) },
                        onUpdateStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                        onDeleteOrder = { id, title -> viewModel.deleteTask(id, title) }
                    )
                }
                "Design Preview", "Design & Approval" -> {
                    DesignApprovalModuleView(
                        tasks = tasks,
                        onApproveMockup = { task, approved, feedback -> viewModel.approveDesign(task, approved, feedback) },
                        onTriggerAlert = { viewModel.triggerLocalAlert(it) }
                    )
                }
                "Production & Delivery" -> {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductionTrackerModuleView(
                                tasks = tasks,
                                onUpdateSteps = { task, print, qc, pack, worker -> viewModel.updateProductionSteps(task, print, qc, pack, worker) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DeliveryManagerModuleView(
                                tasks = tasks,
                                onUpdateDelivery = { task, address, contact, status, photo -> viewModel.updateDelivery(task, address, contact, status, photo) },
                                onTriggerAlert = { viewModel.triggerLocalAlert(it) }
                            )
                        }
                    }
                }
                "Inventory Management" -> {
                    InventoryManagementModuleView()
                }
                "Client CRM" -> {
                    ClientCrmModuleView(
                        tasks = tasks,
                        onTriggerAlert = { viewModel.triggerLocalAlert(it) },
                        onSetReminder = { showCrmFollowUpDialog = it }
                    )
                }
                "Reports & Analytics" -> {
                    ReportsAnalyticsModuleView(tasks = tasks)
                }
                "Employee Tasks" -> {
                    EmployeeTaskModuleView(
                        tasks = tasks
                    )
                }
            }
        }
    }

    // --- DIALOGS SECTION ---

    // 1. Log New Order Dialog
    if (showCreateOrderDialog) {
        var clientName by remember { mutableStateOf("") }
        var companyName by remember { mutableStateOf("") }
        var productType by remember { mutableStateOf("Lanyard") }
        var quantity by remember { mutableStateOf(250) }
        var description by remember { mutableStateOf("") }
        var assignedTo by remember { mutableStateOf("Ahmed Al-Mansoori") }
        var deliveryAddress by remember { mutableStateOf("") }
        var deliveryContact by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("MEDIUM") }
        var deadlineDays by remember { mutableStateOf(5) } // days to due

        AlertDialog(
            onDismissRequest = { showCreateOrderDialog = false },
            title = { Text("Log New Web / WhatsApp Inquiry") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Contact Client Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("order_client_name")
                    )
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name (e.g., UDC) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Product Selection", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Lanyard", "Badge", "Wristband", "Mousepad").forEach { type ->
                            InputChip(
                                selected = productType == type,
                                onClick = { productType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volume / Quantity: $quantity pcs", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = quantity.toFloat(),
                        onValueChange = { quantity = ((it / 50).toInt() * 50).coerceIn(50, 5000) },
                        valueRange = 50f..5000f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Inquiry & Specifications *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Destination Address *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryContact,
                        onValueChange = { deliveryContact = it },
                        label = { Text("WhatsApp Contact (+974) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Process Lead/Operator", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Ahmed Al-Mansoori", "Zainab Rashid", "Sara Al-Khouri").forEach { staff ->
                            FilterChip(
                                selected = assignedTo == staff,
                                onClick = { assignedTo = staff },
                                label = { Text(staff, fontSize = 10.sp) }
                            )
                        }
                    }

                    Text("Urgency", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { prio ->
                            Button(
                                onClick = { priority = prio },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (priority == prio) {
                                        when (prio) {
                                            "HIGH" -> Color(0xFFC62828)
                                            "MEDIUM" -> Color(0xFFEF6C00)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    } else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (priority == prio) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(prio, fontSize = 10.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Delivery: $deadlineDays days from now", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = deadlineDays.toFloat(),
                        onValueChange = { deadlineDays = it.toInt() },
                        valueRange = 2f..30f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.trim().isNotEmpty() && companyName.trim().isNotEmpty() && description.trim().isNotEmpty() && deliveryContact.trim().isNotEmpty()) {
                            viewModel.addNewTask(
                                title = "$companyName: $productType Order",
                                description = description,
                                assignedTo = assignedTo,
                                category = "Operations",
                                priority = priority,
                                dueDate = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000L),
                                clientName = clientName,
                                companyName = companyName,
                                productType = productType,
                                quantity = quantity,
                                deliveryAddress = deliveryAddress,
                                deliveryContact = deliveryContact
                            )
                            showCreateOrderDialog = false
                        } else {
                            viewModel.triggerLocalAlert("Please enter Client Contact, Company name, Specifications, and Contact number.")
                        }
                    },
                    modifier = Modifier.testTag("submit_order_button")
                ) {
                    Text("Digitize & Launch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateOrderDialog = false }) {
                    Text("Discard")
                }
            }
        )
    }

    // 2. CRM Follow Up Setter
    showCrmFollowUpDialog?.let { task ->
        var notes by remember { mutableStateOf(task.salesFollowUpNotes) }
        var selectWeeks by remember { mutableStateOf(3) }

        AlertDialog(
            onDismissRequest = { showCrmFollowUpDialog = null },
            title = { Text("Log Client Account Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Schedule outreach reminders for account: ${task.clientName}", style = MaterialTheme.typography.bodyMedium)
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Strategic Outreach Pitch/Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Alert Timing", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1 to "1 Week", 3 to "3 Weeks", 6 to "6 Weeks").forEach { (weeks, tag) ->
                            FilterChip(
                                selected = selectWeeks == weeks,
                                onClick = { selectWeeks = weeks },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val duration = selectWeeks * 7 * 24 * 60 * 60 * 1000L
                        viewModel.saveSalesReminder(
                            task = task,
                            required = true,
                            notes = notes,
                            date = System.currentTimeMillis() + duration
                        )
                        showCrmFollowUpDialog = null
                    }
                ) {
                    Text("Enable Reminder Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrmFollowUpDialog = null }) {
                    Text("Back")
                }
            }
        )
    }
}



@Composable
fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskCardItem(
    task: Task,
    onChangeStatus: (String) -> Unit,
    onStageSelected: (Int) -> Unit,
    onDelete: () -> Unit,
    canModify: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Priority level & Category indicator & SQLite sync status & Expand Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (task.priority) {
                                    "HIGH" -> MaterialTheme.colorScheme.errorContainer
                                    "MEDIUM" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (task.priority) {
                                "HIGH" -> MaterialTheme.colorScheme.onErrorContainer
                                "MEDIUM" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sync status and collapse icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (task.syncState == "SYNCED") Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = "Sync Snapshot",
                            tint = if (task.syncState == "SYNCED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (task.syncState == "SYNCED") "Synced" else "Local",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.syncState == "SYNCED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task Content
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (task.status == "COMPLETED") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic Step indicator (Preview / Header)
            val currentStageNum = task.currentStage.coerceIn(1, 5)
            val stageName = when (currentStageNum) {
                1 -> "Quoting"
                2 -> "Design & Sample"
                3 -> "Production"
                4 -> "Quality Control"
                else -> "Delivery"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (currentStageNum) {
                            1 -> Icons.Default.ReceiptLong
                            2 -> Icons.Default.Brush
                            3 -> Icons.Default.Build
                            4 -> Icons.Default.CheckCircle
                            else -> Icons.Default.LocalShipping
                        },
                        contentDescription = stageName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Fulfillment: Stage $currentStageNum — $stageName",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isExpanded) "Active" else "Tap to plan",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable section showing "Our Process" full timeline
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "LanyardQatar Order Fulfillment Process:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // The 5 Steps Stepper Layout
                    val stepsList = listOf(
                        Triple(1, "Quoting", "We listen to customers' needs, evaluate their event requirements, and prepare accurate pricing that fits their project."),
                        Triple(2, "Design & Sample", "Our team develops tailored design concepts and visual mock-ups to ensure everything is approved with real samples before production begins."),
                        Triple(3, "Production", "customers' credentials are produced with premium-grade materials and modern manufacturing techniques for exceptional quality."),
                        Triple(4, "Quality Control", "Each item is checked with care to make sure it meets our quality standards before being sent out."),
                        Triple(5, "Delivery", "their order is delivered quickly and securely to their preferred location.")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                    ) {
                        stepsList.forEach { (stepNum, name, desc) ->
                            val isActive = currentStageNum == stepNum
                            val isCompleted = currentStageNum > stepNum
                            
                            val containerBkg = if (isActive) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            }

                            val borderMod = if (isActive) {
                                Modifier.background(containerBkg, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(borderMod)
                                    .clickable { onStageSelected(stepNum) }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Stepper Node indicator
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (isActive) {
                                                    MaterialTheme.colorScheme.primary
                                                } else if (isCompleted) {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed step",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = stepNum.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                    
                                    if (stepNum < 5) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(30.dp)
                                                .background(
                                                    color = if (isCompleted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                                                )
                                        )
                                    }
                                }

                                // Narrative details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$stepNum. $name",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isActive) {
                                        Text(
                                            text = "★ Tap any step to transition order phase",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }

            // Assigned to operator staff and state update buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Worker",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Assigned operator:",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = task.assignedTo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive checkboxes/pills to progress state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.status != "COMPLETED") {
                        Button(
                            onClick = {
                                if (task.status == "TODO") onChangeStatus("IN_PROGRESS")
                                else onChangeStatus("COMPLETED")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (task.status == "TODO") "Set Active" else "Mark Complete",
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        // Completed badge
                        Text(
                            text = "Completed ✅",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Admin Delete option
                    if (canModify) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task log",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
