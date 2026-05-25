package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.Screen
import com.example.Utils
import com.example.WorkspaceViewModel
import com.example.data.ChatMessage
import com.example.data.Meeting
import com.example.data.Task
import com.example.ui.modules.*
import com.example.ui.screens.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceMainView(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Core Screen Selection Grid ---
        if (false) {
            // Security / login feature disabled for now
        } else {
            // Main Responsive Scaffold (Dashboard, Tasks, Chat, Calendar, Portal, Settings)
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = "Logo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "LanyardQatar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        actions = {
                            // Offline Connection Toggle Demo
                            IconButton(
                                onClick = { viewModel.toggleConnectionMode() },
                                modifier = Modifier.testTag("connection_toggle")
                            ) {
                                Icon(
                                    imageVector = when (connectionStatus) {
                                        "Offline Mode" -> Icons.Default.CloudOff
                                        "Pending Updates" -> Icons.Default.CloudSync
                                        "Synchronizing..." -> Icons.Default.Sync
                                        else -> Icons.Default.CloudDone
                                    },
                                    contentDescription = "Sync Toggle",
                                    tint = when (connectionStatus) {
                                        "Offline Mode" -> MaterialTheme.colorScheme.error
                                        "Pending Updates" -> MaterialTheme.colorScheme.tertiary
                                        "Synchronizing..." -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }

                            // Manual Trigger Sync
                            IconButton(onClick = { viewModel.triggerSync() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync Now"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                    )
                },
                bottomBar = {
                    // Mobile navigation bar, shown for standard mobile view
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ) {
                        NavigationBarItem(
                            selected = currentTab == Screen.Dashboard,
                            onClick = { viewModel.currentTab.value = Screen.Dashboard },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentTab == Screen.Tasks,
                            onClick = { viewModel.currentTab.value = Screen.Tasks },
                            icon = { Icon(Icons.Default.Task, contentDescription = "Tasks") },
                            label = { Text("Tasks", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_tasks")
                        )
                        NavigationBarItem(
                            selected = currentTab == Screen.Chat,
                            onClick = { viewModel.currentTab.value = Screen.Chat },
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                            label = { Text("Chat", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_chat")
                        )
                        NavigationBarItem(
                            selected = currentTab == Screen.Calendar,
                            onClick = { viewModel.currentTab.value = Screen.Calendar },
                            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") },
                            label = { Text("Calendar", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_calendar")
                        )
                        NavigationBarItem(
                            selected = currentTab == Screen.Portal || currentTab == Screen.Settings,
                            onClick = { viewModel.currentTab.value = Screen.Settings },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        Screen.Dashboard -> DashboardScreenView(viewModel = viewModel)
                        Screen.Tasks -> TasksScreenView(viewModel = viewModel)
                        Screen.Chat -> ChatScreenView(viewModel = viewModel)
                        Screen.Calendar -> CalendarScreenView(viewModel = viewModel)
                        Screen.Portal -> PortalScreenView(viewModel = viewModel)
                        Screen.Settings -> SettingsScreenView(viewModel = viewModel)
                        else -> DashboardScreenView(viewModel = viewModel)
                    }
                }
            }
        }

        // --- Simulated In-App Heads-up Notification Alert Banner ---
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            activeNotification?.let { txt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_alert_toast")
                        .clickable { viewModel.activeNotification.value = null },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alert Alert",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automated Alert • LanyardQatar",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = txt,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.activeNotification.value = null },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.inverseOnSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. BIOMETRIC LOCK SCREEN VIEW
// ==========================================
@Composable
fun LockScreenView(viewModel: WorkspaceViewModel) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isAuthenticating by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo and Corporate Identity
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Secure Security Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "LanyardQatar Unified Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Automated Corporate Operations & Logistics",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Secure Role-Based Access Control Selection before auth
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Role-Based Access Control (RBAC)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Select active employee credentials to simulate secure access rights:",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Admin", "Sales", "Design", "Production").forEach { role ->
                        val isSelected = currentUserRole == role
                        Button(
                            onClick = { viewModel.selectRole(role) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_role_$role"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Identity: $currentUserName (${currentUserRole})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Biometric Unlock Section
        if (isBiometricEnabled) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAuthenticating) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    .clickable {
                        if (isAuthenticating) return@clickable
                        isAuthenticating = true
                        authError = null
                        scope.launch {
                            delay(1200) // Simulated sensor read delay
                            isAuthenticating = false
                            viewModel.triggerLocalAlert("Biometric Verified. Welcome $currentUserName.")
                            viewModel.currentTab.value = Screen.Dashboard
                        }
                    }
                    .testTag("biometric_fingerprint_touch"),
                contentAlignment = Alignment.Center
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(112.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Simulate Fingerprint Biometrics",
                    tint = if (isAuthenticating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isAuthenticating) "Scanning Biometrics..." else "Tap to authenticate with Biometrics",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Supports offline secure hardware enclave",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            // Biometric Disabled Prompt
            Text(
                text = "Biometric Authentication Disabled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { viewModel.isBiometricEnabled.value = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Re-enable Biometrics")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bypass Security key pin code for fast emulator usage
        Text(
            text = "OR ENTER SECURE EMPLOYEE PIN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = pinCode,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        pinCode = it
                        authError = null
                        if (it.length == 4) {
                            if (it == "2026") { // Simulating secure company PIN
                                viewModel.triggerLocalAlert("PIN verified successfully.")
                                viewModel.currentTab.value = Screen.Dashboard
                            } else {
                                authError = "Invalid Employee PIN"
                                pinCode = ""
                            }
                        }
                    }
                },
                placeholder = { Text("xxxx") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("security_pin_field")
            )

            Button(
                onClick = {
                    if (pinCode.isNotEmpty()) {
                        viewModel.triggerLocalAlert("Developer PIN bypass active.")
                        viewModel.currentTab.value = Screen.Dashboard
                    } else {
                        // Quick click bypass
                        viewModel.triggerLocalAlert("Authentication bypass applied.")
                        viewModel.currentTab.value = Screen.Dashboard
                    }
                },
                modifier = Modifier.testTag("bypass_login_button")
            ) {
                Text("Unlock")
            }
        }

        authError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Default Secure Bypass PIN: 2026",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ==========================================
// 2. DASHBOARD VIEW WITH ACTIVE ANALYTICS
// ==========================================
@Composable
fun DashboardScreenView(viewModel: WorkspaceViewModel) {
    var selectedSubTab by remember { mutableStateOf("Overview") }
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val notificationHistory by viewModel.notificationHistory.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Redesigned Topbar Header Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F6E56), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "LanyardQatar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Internal Insights — May 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    Text(
                        text = "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F6E56),
                        modifier = Modifier
                            .background(Color(0xFF0F6E56).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Sub-tabs Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Overview", "Orders", "Production", "Clients").forEach { tab ->
                        val isActive = selectedSubTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent
                                )
                                .clickable { selectedSubTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- Render Sub-tab Content ---
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedSubTab) {
                "Overview" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. 4 Metrics in responsive Card block representation
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                    OverviewMetricCard(
                                        title = "Active Orders",
                                        value = "47",
                                        delta = "↑ 12 from last month",
                                        isDeltaUp = true,
                                        icon = Icons.Default.ShoppingCart,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OverviewMetricCard(
                                        title = "Revenue (QAR)",
                                        value = "284,500",
                                        delta = "↑ 8.4% vs last month",
                                        isDeltaUp = true,
                                        icon = Icons.Default.Payments,
                                        modifier = Modifier.weight(1f)
                                    )
                               }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                    OverviewMetricCard(
                                        title = "Units Produced",
                                        value = "18,240",
                                        delta = "↑ 3,100 this week",
                                        isDeltaUp = true,
                                        icon = Icons.Default.Inventory,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OverviewMetricCard(
                                        title = "Active Clients",
                                        value = "23",
                                        delta = "↓ 2 churned",
                                        isDeltaUp = false,
                                        icon = Icons.Default.Business,
                                        modifier = Modifier.weight(1f)
                                    )
                               }
                            }
                        }

                        // 2. Charts Cards (Revenue Bar Chart and Donut Block)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF0F6E56), modifier = Modifier.size(16.dp))
                                        Text("Revenue Trend (6 Months)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    RevenueBarChartCompose(modifier = Modifier.height(180.dp).padding(horizontal = 8.dp))
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.DonutLarge, contentDescription = null, tint = Color(0xFF185FA5), modifier = Modifier.size(16.dp))
                                        Text("Product Mixture Mix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ProductMixDonut(modifier = Modifier.size(120.dp))
                                        
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ProductMixLegendRow(label = "Lanyards (52%)", color = Color(0xFF0F6E56))
                                            ProductMixLegendRow(label = "Badges (31%)", color = Color(0xFF185FA5))
                                            ProductMixLegendRow(label = "Wristbands (17%)", color = Color(0xFFBA7517))
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Recent Orders List Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF0F6E56), modifier = Modifier.size(16.dp))
                                        Text("Recent Orders Hub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OverviewOrderRow(id = "#LQ-2244", client = "Al Rayyan Sports Club", type = "Lanyards", statusLabel = "In Production", statusColor = Color(0xFF085041), statusBgColor = Color(0xFFE1F5EE))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    OverviewOrderRow(id = "#LQ-2243", client = "Qatar Airways", type = "Badges", statusLabel = "Ready", statusColor = Color(0xFF27500A), statusBgColor = Color(0xFFEAF3DE))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    OverviewOrderRow(id = "#LQ-2241", client = "Hamad Medical Corp.", type = "Wristbands", statusLabel = "Dispatched", statusColor = Color(0xFF633806), statusBgColor = Color(0xFFFAEEDA))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    OverviewOrderRow(id = "#LQ-2239", client = "QNB Group", type = "Badges", statusLabel = "Design Review", statusColor = Color(0xFF712B13), statusBgColor = Color(0xFFFAECE7))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    OverviewOrderRow(id = "#LQ-2237", client = "Aspire Zone", type = "Lanyards", statusLabel = "In Production", statusColor = Color(0xFF085041), statusBgColor = Color(0xFFE1F5EE))
                                }
                            }
                        }

                        // 4. Staff Performance Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF185FA5), modifier = Modifier.size(16.dp))
                                        Text("Staff Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    StaffPerformanceRow(initials = "AM", name = "Ahmed Al-Mansoori", role = "Production Lead", pct = 0.92f, avatarColor = Color(0xFFE1F5EE), avatarTextColor = Color(0xFF0F6E56))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    StaffPerformanceRow(initials = "FK", name = "Fatima Khalid", role = "Sales Executive", pct = 0.85f, avatarColor = Color(0xFFFFF3E0), avatarTextColor = Color(0xFFE65100))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    StaffPerformanceRow(initials = "RS", name = "Ravi Subramaniam", role = "Quality Control", pct = 0.78f, avatarColor = Color(0xFFFFEBEE), avatarTextColor = Color(0xFFC62828))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    StaffPerformanceRow(initials = "NA", name = "Noor Al-Ansari", role = "Design & Artwork", pct = 0.96f, avatarColor = Color(0xFFE0F2F1), avatarTextColor = Color(0xFF00796B))
                                }
                            }
                        }

                        // 5. Production Capacity full-width details
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFBA7517), modifier = Modifier.size(16.dp))
                                        Text("Production Capacity & Limits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        ProductionProgressTrack(label = "Lanyards Checkpoint", pct = 0.76f, color = Color(0xFF0F6E56))
                                        ProductionProgressTrack(label = "Badges Alignment", pct = 0.54f, color = Color(0xFF185FA5))
                                        ProductionProgressTrack(label = "Wristbands Dispatch", pct = 0.42f, color = Color(0xFFBA7517))
                                    }
                                    
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = "Avg. turnaround", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text(text = "4.2 days", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                Text(text = "↓ 0.8 days faster", fontSize = 11.sp, color = Color(0xFF0F6E56), fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text(text = "Rejection rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text(text = "1.3%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                Text(text = "↓ Below threshold", fontSize = 11.sp, color = Color(0xFF0F6E56), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = "Top client this month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text(text = "Qatar Airways", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                Text(text = "QAR 48,200 · 3 orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            Column {
                                                Text(text = "Pending proofs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text(text = "6", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFFBA7517))
                                                Text(text = "Awaiting approval", fontSize = 11.sp, color = Color(0xFFBA7517), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Historical Synced regional Hub Info
                        item {
                            Text(
                                text = "Regional Hub Synced Nodes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HubStateItem(name = "Qatar West Bay (Master Host)", location = "Doha", speed = "0.4 ms", isLive = true)
                                    HubStateItem(name = "KSA Riyadh Branch", location = "Riyadh", speed = "18 ms", isLive = true)
                                    HubStateItem(name = "UAE Dubai Logistics Node", location = "Dubai", speed = "22 ms", isLive = true)
                                    HubStateItem(name = "Oman Muscat Office", location = "Muscat", speed = "34 ms", isLive = false)
                                }
                            }
                        }
                    }
                }
                
                "Orders" -> {
                    // Embeds the real, interactive OrderManagement view so they can directly use it
                    var searchQuery by remember { mutableStateOf("") }
                    var showCreateOrderDialog by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        OrderManagementModuleView(
                            tasks = tasks,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onAddNewOrderClicked = { showCreateOrderDialog = true },
                            onUpdateStage = { task, stage -> viewModel.updateTaskStage(task, stage) },
                            onUpdateStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                            onDeleteOrder = { id, title -> viewModel.deleteTask(id, title) }
                        )
                        
                        // Add order dialog inside dashboard tab
                        if (showCreateOrderDialog) {
                            AddTaskDialogFromDashboard(
                                onDismiss = { showCreateOrderDialog = false },
                                onAddOrder = { title, desc, assignedTo, priority, clientName, companyName, product, qty, dlvrAddress, dlvrContact ->
                                    viewModel.addNewTask(
                                        title = title,
                                        description = desc,
                                        assignedTo = assignedTo,
                                        category = "Operations",
                                        priority = priority,
                                        dueDate = System.currentTimeMillis() + (86400000L * 5),
                                        clientName = clientName,
                                        companyName = companyName,
                                        productType = product,
                                        quantity = qty,
                                        deliveryAddress = dlvrAddress,
                                        deliveryContact = dlvrContact
                                    )
                                    showCreateOrderDialog = false
                                }
                            )
                        }
                    }
                }
                
                "Production" -> {
                    // Embed active production queue tracker for total workforce coordination
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1.3f)) {
                                ProductionTrackerModuleView(
                                    tasks = tasks,
                                    onUpdateSteps = { task, print, qc, pack, worker -> viewModel.updateProductionSteps(task, print, qc, pack, worker) },
                                    onTriggerAlert = { viewModel.triggerLocalAlert(it) }
                                )
                            }
                            Box(modifier = Modifier.weight(0.7f)) {
                                DeliveryManagerModuleView(
                                    tasks = tasks,
                                    onUpdateDelivery = { task, address, contact, status, photo -> viewModel.updateDelivery(task, address, contact, status, photo) },
                                    onTriggerAlert = { viewModel.triggerLocalAlert(it) }
                                )
                            }
                        }
                    }
                }
                
                "Clients" -> {
                    // Embed real live Client CRM module
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        ClientCrmModuleView(
                            tasks = tasks,
                            onTriggerAlert = { viewModel.triggerLocalAlert(it) },
                            onSetReminder = { /* Trigger simple dialog if required */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialogFromDashboard(
    onDismiss: () -> Unit,
    onAddOrder: (String, String, String, String, String, String, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("Lanyard") }
    var quantity by remember { mutableStateOf(250) }
    var priority by remember { mutableStateOf("MEDIUM") }
    var assignedTo by remember { mutableStateOf("Ahmed Al-Mansoori") }
    var deliveryAddress by remember { mutableStateOf("Doha Marina") }
    var deliveryContact by remember { mutableStateOf("+974 ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Log Inquiry / New Corporate Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Client Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Contact Person Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Order Brief Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Artwork & Custom Requirements") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Product Setup", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Lanyard", "Badge", "Wristband").forEach { prod ->
                        val isSelected = productType == prod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { productType = prod }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prod,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull() ?: 250 },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Qatar Delivery Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deliveryContact,
                    onValueChange = { deliveryContact = it },
                    label = { Text("Delivery Mobile Contact") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (companyName.isNotBlank() && title.isNotBlank()) {
                                onAddOrder(
                                    title, desc, assignedTo, priority, clientName,
                                    companyName, productType, quantity, deliveryAddress, deliveryContact
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F6E56))
                    ) {
                        Text("Confirm Order", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewMetricCard(
    title: String,
    value: String,
    delta: String,
    isDeltaUp: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = delta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDeltaUp) Color(0xFF0F6E56) else Color(0xFFBA1A1A)
            )
        }
    }
}

@Composable
fun RevenueBarChartCompose(modifier: Modifier = Modifier) {
    val data = listOf(198f, 211f, 176f, 244f, 262f, 284f)
    val labels = listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May")
    val maxVal = 300f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { idx, value ->
            val fraction = value / maxVal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${value.toInt()}k",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .height(110.dp)
                        .width(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .background(Color(0xFF0F6E56))
                            .align(Alignment.BottomCenter)
                    )
                }
                Text(
                    text = labels[idx],
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun ProductMixDonut(modifier: Modifier = Modifier) {
    val data = listOf(52f, 31f, 17f)
    val colors = listOf(Color(0xFF0F6E56), Color(0xFF185FA5), Color(0xFFBA7517))
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - arcSize) / 2f, (size.height - arcSize) / 2f)
            
            var startAngle = -90f
            data.forEachIndexed { i, portion ->
                val sweep = (portion / 100f) * 360f
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "18.24k",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Units Mix",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ProductMixLegendRow(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OverviewOrderRow(
    id: String,
    client: String,
    type: String,
    statusLabel: String,
    statusColor: Color,
    statusBgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(statusBgColor)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = statusLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun StaffPerformanceRow(
    initials: String,
    name: String,
    role: String,
    pct: Float,
    avatarColor: Color,
    avatarTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = avatarTextColor
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pct)
                        .background(Color(0xFF0F6E56), RoundedCornerShape(3.dp))
                )
            }
            Text(
                text = "${(pct * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ProductionProgressTrack(
    label: String,
    pct: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Text(text = "${(pct * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun StatusItemLegend(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HubStateItem(name: String, location: String, speed: String, isLive: Boolean) {
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
                    .size(8.dp)
                    .background(
                        if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        CircleShape
                    )
            )
            Column {
                Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(
            text = if (isLive) "PING: $speed" else "OFFLINE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isLive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        )
    }
}

// ==========================================
// 3. TASK BOARD & TRACKING
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenView(viewModel: WorkspaceViewModel) {
    TasksScreenViewReal(viewModel)
}

// TasksScreenView and TaskCardItem moved to TasksScreenView.kt

// ==========================================
// 4. TEAM CHAT & INTERNAL COMMUNICATIONS
// ==========================================
// ChatScreenView moved to com.example.ui.screens

// ==========================================
// 5. CALENDAR & MEETING SCHEDULER
// ==========================================
// CalendarScreenView moved to com.example.ui.screens

// ==========================================
// 6. INTERNAL PORTAL DISPLAY VIEW
// ==========================================
// PortalScreenView moved to com.example.ui.screens

// ==========================================
// 7. SYSTEM SETTINGS & PREFERENCES
// ==========================================
// SettingsScreenView moved to com.example.ui.screens

// Helper: extension on MaterialTheme to provide tonal elevations
@Composable
fun ColorScheme.surfaceColorAtElevation(elevation: androidx.compose.ui.unit.Dp): Color {
    // Standard M3 tonal elevation color provider approximation
    return background // Light scheme fallbacks
}

// Simple stateful scroll state
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}

// OrderManagementModuleView moved to com.example.ui.modules

// DesignApprovalModuleView moved to com.example.ui.modules

// ProductionTrackerModuleView moved to com.example.ui.modules

// DeliveryManagerModuleView moved to com.example.ui.modules

// ClientCrmModuleView moved to com.example.ui.modules

// ReportsAnalyticsModuleView moved to com.example.ui.modules

// InventoryManagementModuleView moved to com.example.ui.modules

// EmployeeTaskModuleView moved to com.example.ui.modules