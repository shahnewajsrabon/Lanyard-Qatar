package com.example.ui.modules

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task

// Data Class for Mock Machines
data class Machine(
    val id: String,
    val name: String,
    val product: String,
    val status: String, // "run", "idle", "setup", "maint"
    val jobDesc: String,
    val util: Int,
    val clientInfo: String,
    val operator: String,
    val eta: String
)

// Data Class for Mock QC Log entry
data class QcEntry(
    val orderId: String,
    val client: String,
    val product: String,
    val batch: String,
    val inspected: Int,
    val rejected: Int,
    val isPass: Boolean,
    val reason: String,
    val inspector: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProductionTrackerModuleView(
    tasks: List<Task>,
    onUpdateSteps: (Task, Boolean, Boolean, Boolean, String) -> Unit,
    onTriggerAlert: (String) -> Unit = {}
) {
    var activeSubTab by remember { mutableStateOf("Floor overview") }
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }
    
    // Hardcoded static data directly from design specification
    val machinesList = remember {
        listOf(
            Machine("LQ-M01", "Sublimation Press #1", "Lanyards", "run", "LQ-2244 · Al Rayyan Sports Club", 88, "Al Rayyan Sports Club · 2,500 units", "Operator: Ahmed A.", "Est. done: 14:30"),
            Machine("LQ-M02", "Sublimation Press #2", "Lanyards", "run", "LQ-2242 · Aspire Zone", 74, "Aspire Zone · 1,200 units", "Operator: Ahmed A.", "Est. done: 16:00"),
            Machine("LQ-M03", "UV Flatbed Printer #1", "Badges", "run", "LQ-2237 · RasGas", 91, "RasGas · 450 units", "Operator: Noor A.", "Est. done: 11:45"),
            Machine("LQ-M04", "UV Flatbed Printer #2", "Badges", "idle", "Idle — awaiting next job", 0, "—", "Operator: Fatima K.", "Ready for assignment"),
            Machine("LQ-M05", "Silicone Mould Press", "Wristbands", "run", "LQ-2235 · Ooredoo Qatar", 62, "Ooredoo Qatar · 2,000 units", "Operator: Ravi S.", "Est. done: 17:00"),
            Machine("LQ-M06", "Tyvek Cutter & Printer", "Wristbands", "run", "LQ-2241 · Hamad Medical Corp.", 79, "Hamad Medical Corp. · 5,000 units", "Operator: Ravi S.", "Est. done: 12:15"),
            Machine("LQ-M07", "Badge Laminator #1", "Badges", "setup", "Setup: LQ-2234 · Qatar Foundation", 15, "Qatar Foundation · 700 units", "Operator: Noor A.", "Setup ETA: 25 min"),
            Machine("LQ-M08", "Badge Laminator #2", "Badges", "idle", "Idle — shift break", 0, "—", "Operator: unassigned", "Available after 10:00"),
            Machine("LQ-M09", "Heat Transfer Press", "Lanyards", "maint", "Scheduled maintenance", 100, "—", "Technician: external", "ETA back: 13:00")
        )
    }

    val staticJobs = remember {
        listOf(
            Task(id = 2243, title = "Qatar Airways polyvinyl badges", description = "PVC ID badges, full-colour print, CR80 size", assignedTo = "Noor A.", category = "Operations", status = "IN_PROGRESS", priority = "HIGH", dueDate = 0L, syncState = "SYNCED", clientName = "Qatar Airways", companyName = "Qatar Airways", productType = "Badges", quantity = 800, currentStage = 3, designApproved = true, prodPrintingDone = true, prodQcDone = false, prodPackagingDone = false, prodWorker = "Noor A."),
            Task(id = 2241, title = "Hamad Medical wristbands", description = "Tyvek wristbands, sequential numbering", assignedTo = "Ravi S.", category = "Operations", status = "IN_PROGRESS", priority = "HIGH", dueDate = 0L, syncState = "SYNCED", clientName = "Hamad Medical Corp.", companyName = "Hamad Medical Corp.", productType = "Wristbands", quantity = 5000, currentStage = 3, designApproved = true, prodPrintingDone = true, prodQcDone = true, prodPackagingDone = false, prodWorker = "Ravi S."),
            Task(id = 2237, title = "RasGas security ID badges", description = "Safety ID badges with magnetic strip, HSE approved", assignedTo = "Ravi S.", category = "Operations", status = "COMPLETED", priority = "HIGH", dueDate = 0L, syncState = "SYNCED", clientName = "RasGas", companyName = "RasGas", productType = "Badges", quantity = 450, currentStage = 4, designApproved = true, prodPrintingDone = true, prodQcDone = true, prodPackagingDone = true, prodWorker = "Ravi S."),
            Task(id = 2244, title = "Al Rayyan SC custom lanyards", description = "Custom logo print, red & white cord, metal clip", assignedTo = "Ahmed A.", category = "Operations", status = "IN_PROGRESS", priority = "MEDIUM", dueDate = 0L, syncState = "SYNCED", clientName = "Al Rayyan Sports Club", companyName = "Al Rayyan Sports Club", productType = "Lanyards", quantity = 2500, currentStage = 3, designApproved = true, prodPrintingDone = true, prodQcDone = false, prodPackagingDone = false, prodWorker = "Ahmed A."),
            Task(id = 2242, title = "Aspire Zone promotional lanyards", description = "Single-colour sublimation, breakaway safety clip", assignedTo = "Ahmed A.", category = "Operations", status = "TODO", priority = "MEDIUM", dueDate = 0L, syncState = "SYNCED", clientName = "Aspire Zone", companyName = "Aspire Zone", productType = "Lanyards", quantity = 1200, currentStage = 3, designApproved = true, prodPrintingDone = false, prodQcDone = false, prodPackagingDone = false, prodWorker = "Ahmed A."),
            Task(id = 2235, title = "Ooredoo silicone wristbands", description = "Silicone wristbands, debossed logo", assignedTo = "Ahmed A.", category = "Operations", status = "TODO", priority = "MEDIUM", dueDate = 0L, syncState = "SYNCED", clientName = "Ooredoo Qatar", companyName = "Ooredoo Qatar", productType = "Wristbands", quantity = 2000, currentStage = 3, designApproved = true, prodPrintingDone = false, prodQcDone = false, prodPackagingDone = false, prodWorker = "Ahmed A."),
            Task(id = 2234, title = "Qatar Foundation delegate badges", description = "Multi-tier delegate badges, RFID chip", assignedTo = "Noor A.", category = "Operations", status = "TODO", priority = "LOW", dueDate = 0L, syncState = "SYNCED", clientName = "Qatar Foundation", companyName = "Qatar Foundation", productType = "Badges", quantity = 700, currentStage = 3, designApproved = true, prodPrintingDone = false, prodQcDone = false, prodPackagingDone = false, prodWorker = "Noor A.")
        )
    }

    val qcLogList = remember {
        listOf(
            QcEntry("LQ-2241", "Hamad Medical", "Wristbands", "B-04", 500, 0, true, "—", "Ravi S."),
            QcEntry("LQ-2244", "Al Rayyan SC", "Lanyards", "B-02", 400, 12, false, "Colour fade on edge — sublimation temp variance", "Ravi S."),
            QcEntry("LQ-2237", "RasGas", "Badges", "B-01", 450, 0, true, "—", "Ravi S."),
            QcEntry("LQ-2242", "Aspire Zone", "Lanyards", "B-01", 300, 22, false, "Breakaway clip misaligned — tooling issue", "Ravi S."),
            QcEntry("LQ-2235", "Ooredoo Qatar", "Wristbands", "B-02", 250, 7, true, "Under 3% threshold — approved", "Ravi S."),
            QcEntry("LQ-2243", "Qatar Airways", "Badges", "B-03", 800, 22, false, "Lamination bubbles on 22 units — reprint queued", "Ravi S."),
            QcEntry("LQ-2241", "Hamad Medical", "Wristbands", "B-05", 500, 0, true, "—", "Ravi S.")
        )
    }

    // Combine active tasks from DB with static catalog jobs for high representation
    val combinedJobs = remember(tasks) {
        val databaseJobs = tasks.filter { it.currentStage >= 3 && it.designApproved }
        val databaseIds = databaseJobs.map { it.id }.toSet()
        val uniqueStaticJobs = staticJobs.filter { it.id !in databaseIds }
        (databaseJobs + uniqueStaticJobs).sortedBy { if (it.priority == "HIGH") 0 else if (it.priority == "MEDIUM") 1 else 2 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // top internal navbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF185FA5), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                        Column {
                            Text("Production floor node", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Live Operations Control", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Button(
                        onClick = { onTriggerAlert("Daily report summary generated for Mon 25 May 2026. File ID: QRPR-491") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Daily Report ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Sub-tabs Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Floor overview", "Job queue", "Shifts & staff", "QC log").forEach { subTab ->
                        val isSelected = activeSubTab == subTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable {
                                    activeSubTab = subTab
                                    selectedMachine = null
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = subTab,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Stats Row Block representation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProductionMiniStatCard(
                title = "Machines",
                value = "6 / 9",
                sub = "2 idle · 1 maint",
                modifier = Modifier.weight(1f)
            )
            ProductionMiniStatCard(
                title = "Job Q",
                value = "${combinedJobs.size}",
                sub = "${combinedJobs.count { it.priority == "HIGH" }} urgent",
                modifier = Modifier.weight(1f)
            )
            ProductionMiniStatCard(
                title = "Units Today",
                value = "4,820",
                sub = "Target: 6k",
                modifier = Modifier.weight(1f)
            )
            ProductionMiniStatCard(
                title = "QC Reject",
                value = "1.3%",
                sub = "Target < 2%",
                modifier = Modifier.weight(1f)
            )
        }

        // Render Active sub tab panel content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                "Floor overview" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Live Machine Status Board",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // 3x3 Grid Represented linearly
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            machinesList.chunked(2).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunk.forEach { machine ->
                                        val isSelected = selectedMachine?.id == machine.id
                                        val statusColor = when (machine.status) {
                                            "run" -> Color(0xFF0F6E56)
                                            "idle" -> Color(0xFF888780)
                                            "setup" -> Color(0xFFEF9F27)
                                            else -> Color(0xFFE24B4A)
                                        }

                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.5.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedMachine = if (isSelected) null else machine },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Column {
                                                        Text(machine.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                        Text(machine.id + " · " + machine.product, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(statusColor, CircleShape)
                                                    )
                                                }

                                                Text(machine.jobDesc, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                                // Progress bar representation
                                                LinearProgressIndicator(
                                                    progress = machine.util / 100f,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .padding(vertical = 1.dp),
                                                    color = statusColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${machine.util}% util", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Box(
                                                        modifier = Modifier
                                                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(machine.status.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Detailed machine section if clicked
                        AnimatedVisibility(
                            visible = selectedMachine != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            selectedMachine?.let { machine ->
                                val statusColor = when (machine.status) {
                                    "run" -> Color(0xFF0F6E56)
                                    "idle" -> Color(0xFF888780)
                                    "setup" -> Color(0xFFEF9F27)
                                    else -> Color(0xFFE24B4A)
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(machine.name + " [" + machine.id + "]", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { selectedMachine = null }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Current assignment", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(machine.jobDesc, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Client & volume", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(machine.clientInfo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Staff in charge", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(machine.operator, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Completion / Status Info", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(machine.eta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = { onTriggerAlert("Logged offline maintenance form for Machine ${machine.id}") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Log Maintenance", fontSize = 10.sp, color = Color.White)
                                            }

                                            Button(
                                                onClick = { onTriggerAlert("Triggered auto-job routing algorithm to route queued items to ${machine.id}") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F6E56)),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Assign Next Job", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Today's Output Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Today's Output by Product Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutputProgressRow("Lanyards", "2,140 / 3,700 Units", 0.58f, Color(0xFF0F6E56))
                                    OutputProgressRow("Badges", "1,380 / 1,500 Units", 0.92f, Color(0xFF185FA5))
                                    OutputProgressRow("Wristbands", "1,300 / 800 Units", 1.0f, Color(0xFFBA7517), true)
                                }
                            }
                        }
                    }
                }

                "Job queue" -> {
                    // List of interactive queued tasks with print, qc, pack toggles!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Active Operational Queue (${combinedJobs.size} Jobs)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                TextButton(
                                    onClick = { onTriggerAlert("Re-computing due date heuristics and machine capacities... Queue re-ordered for peak dispatch efficiency!") }
                                ) {
                                    Text("Optimize Queue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(combinedJobs) { job ->
                            var isExpanded by remember { mutableStateOf(false) }
                            var localWorker by remember(job.id, job.prodWorker) { mutableStateOf(job.prodWorker) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("#" + job.id, fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                            Text(job.clientName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        val priorityColor = when (job.priority) {
                                            "HIGH" -> Color(0xFFBA1A1A)
                                            "MEDIUM" -> Color(0xFFEF9F27)
                                            else -> Color(0xFF0F6E56)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(priorityColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(job.priority, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(job.quantity.toString() + " · " + job.productType, fontSize = 11.sp)
                                        Text("Due: " + if (job.id == 2243) "Tomorrow" else "5 Days", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    }

                                    val progressVal = if (job.prodPackagingDone) 1f else if (job.prodQcDone) 0.66f else if (job.prodPrintingDone) 0.33f else 0.05f
                                    val progressText = if (job.prodPackagingDone) "Packaged" else if (job.prodQcDone) "QC Checked" else if (job.prodPrintingDone) "Printing Done" else "Awaiting Print"

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = progressVal,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp),
                                                color = Color(0xFF0F6E56)
                                            )
                                            Text("${(progressVal * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(progressText, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F6E56))
                                    }

                                    // Expand to show checklists (printing, qc, packaging and workers)
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("Line Checkpoints & Operator controls", fontSize = 10.sp, fontWeight = FontWeight.Bold)

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Checkbox(
                                                    checked = job.prodPrintingDone,
                                                    onCheckedChange = { v -> onUpdateSteps(job, v, job.prodQcDone, job.prodPackagingDone, localWorker) },
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text("1. Print & Sublimation Completed", fontSize = 11.sp)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Checkbox(
                                                    checked = job.prodQcDone,
                                                    onCheckedChange = { v -> onUpdateSteps(job, job.prodPrintingDone, v, job.prodPackagingDone, localWorker) },
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text("2. Quality Control Check Status", fontSize = 11.sp)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Checkbox(
                                                    checked = job.prodPackagingDone,
                                                    onCheckedChange = { v -> onUpdateSteps(job, job.prodPrintingDone, job.prodQcDone, v, localWorker) },
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text("3. Bundled & Packaged Securely", fontSize = 11.sp)
                                            }

                                            OutlinedTextField(
                                                value = localWorker,
                                                onValueChange = { s -> localWorker = s },
                                                label = { Text("Worker Assignee", fontSize = 8.sp) },
                                                singleLine = true,
                                                trailingIcon = {
                                                    if (localWorker != job.prodWorker) {
                                                        IconButton(
                                                            onClick = { onUpdateSteps(job, job.prodPrintingDone, job.prodQcDone, job.prodPackagingDone, localWorker) }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Save Assignee",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        onUpdateSteps(job, job.prodPrintingDone, job.prodQcDone, job.prodPackagingDone, localWorker)
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth().height(48.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Shifts & staff" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Shifts & Factory Staffing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // 1. Morning Shift Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Morning Shift (Active Node)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("06:00 – 14:00 · Lead: Ahmed Al-Mansoori", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE1F5EE), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F6E56))
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                StaffShiftRow("AA", "Ahmed Al-Mansoori", "Production Lead · Sublimation", "Present", Color(0xFF0F6E56))
                                StaffShiftRow("NA", "Noor Al-Ansari", "Design & Artwork · Badges", "Present", Color(0xFF0F6E56))
                                StaffShiftRow("RS", "Ravi Subramaniam", "QC · Wristbands", "Late 18 min", Color(0xFFBA7517))
                                StaffShiftRow("MJ", "Mohammed Jassim", "Machine Operator", "Present", Color(0xFF0F6E56))
                            }
                        }

                        // 2. Afternoon Shift Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Afternoon Shift (Upcoming)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("14:00 – 22:00 · Lead: Fatima Khalid", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE6F1FB), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("UPCOMING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C447C))
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                StaffShiftRow("FK", "Fatima Khalid", "Supervisor · Lanyards", "Scheduled", MaterialTheme.colorScheme.secondary)
                                StaffShiftRow("SM", "Salim Al-Mutawa", "Operator · Badges", "Scheduled", MaterialTheme.colorScheme.secondary)
                                StaffShiftRow("DM", "Divya Menon", "QC Inspector", "Scheduled", MaterialTheme.colorScheme.secondary)
                            }
                        }

                        // 3. Alerts Card for Night Shift
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                    Text("Night Shift supervisor not yet confirmed (22:00).", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                                Button(
                                    onClick = { onTriggerAlert("System SMS & Push reminder broadcast to all night shift supervisors: Hassan, Basma, and Karim.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Send Broadcast Reminder", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                "QC log" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("QC Inspection Logs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { onTriggerAlert("Extracting overall visual defect analytics trends for sublimation press limits...") }) {
                                Text("Analyze Trends ↗", fontSize = 11.sp)
                            }
                        }

                        // Inspection stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Batches Inspected", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("18 Batches", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Overall Pass Rate", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("98.7%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F6E56))
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Units Rejected", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("63 Units", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                                }
                            }
                        }

                        // List Table entries
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            qcLogList.forEach { entry ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(entry.orderId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("·", fontSize = 11.sp)
                                                Text(entry.client, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (entry.isPass) Color(0xFFE1F5EE) else Color(0xFFFFEBEE),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (entry.isPass) "PASS" else "FAIL",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (entry.isPass) Color(0xFF085041) else Color(0xFFC62828)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(entry.product + " [Batch " + entry.batch + "]", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                            Text("Insp: " + entry.inspected + " · Rej: " + entry.rejected, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (!entry.isPass) {
                                            Text(
                                                text = "Defect reason: " + entry.reason,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828)
                                            )
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

// Stats Card Component Helper
@Composable
fun ProductionMiniStatCard(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 8.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(sub, fontSize = 8.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        }
    }
}

// Output Category Progress Row Helper Component
@Composable
fun OutputProgressRow(
    label: String,
    volumeText: String,
    percentage: Float,
    indicatorColor: Color,
    isTargetExceeded: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(volumeText, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = if (isTargetExceeded) "Target exceeded ✓" else "${(percentage * 100).toInt()}% of daily target",
            fontSize = 9.sp,
            color = if (isTargetExceeded) Color(0xFF0F6E56) else MaterialTheme.colorScheme.secondary,
            fontWeight = if (isTargetExceeded) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Staff Attendance Row Component
@Composable
fun StaffShiftRow(
    initials: String,
    name: String,
    role: String,
    attendanceText: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column {
                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(role, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Text(
            text = attendanceText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}
