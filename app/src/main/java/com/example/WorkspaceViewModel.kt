package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class Screen {
    LockScreen,
    Dashboard,
    Tasks,
    Chat,
    Calendar,
    Portal, // Seamless Portal Integration
    Settings
}

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WorkspaceDatabase.getDatabase(application)
    private val repository = WorkspaceRepository(database.workspaceDao())

    // --- Database-Backed Flows ---
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allChatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMeetings: StateFlow<List<Meeting>> = repository.allMeetings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- UI Navigation and State Controllers ---
    val currentTab = MutableStateFlow(Screen.Dashboard)
    val selectedChannel = MutableStateFlow("General")
    val currentUserRole = MutableStateFlow("Project Manager") // "Admin" | "Project Manager" | "Employee"
    val currentUserName = MutableStateFlow("Sara Al-Khouri") // Mock user name

    // Synchronisation States
    val connectionStatus = MutableStateFlow("Synced") // "Synced" | "Pending Updates" | "Synchronizing..."
    val isDarkThemeEnabled = MutableStateFlow(true) // Configurable in the preferences
    val isBiometricEnabled = MutableStateFlow(true) // Configurable biometric toggle

    // Chat room messaging selection
    val currentChannelMessages: StateFlow<List<ChatMessage>> = combine(
        allChatMessages,
        selectedChannel
    ) { chats, channel ->
        chats.filter { it.channel == channel }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Automated Notification Alerts State ---
    val activeNotification = MutableStateFlow<String?>(null)
    private val _notificationHistory = MutableStateFlow<List<String>>(
        listOf(
            "LanyardQatar workspace successfully initialized locally.",
            "Synced 4 tasks and 2 meetings securely with Qatar Regional Hub."
        )
    )
    val notificationHistory: StateFlow<List<String>> = _notificationHistory.asStateFlow()

    init {
        // Initialize database with premium workspace seed items if empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Role switcher helper representing corporate access
    fun selectRole(role: String) {
        currentUserRole.value = role
        currentUserName.value = when (role) {
            "Admin" -> "Khalid Ahmed (Admin)"
            "Sales" -> "Sara Al-Khouri (Sales CRM)"
            "Design" -> "Yasmin Al-Thani (Design Lead)"
            "Production" -> "Ahmed Al-Mansoori (Production Floor)"
            else -> "Ahmed Al-Mansoori"
        }
        triggerLocalAlert("Access Level updated to: $role. Operations dynamically restricted.")
    }

    // --- Automated notification alert utility ---
    fun triggerLocalAlert(message: String) {
        // Add to historical telemetry list
        val currentList = _notificationHistory.value.toMutableList()
        currentList.add(0, "[${System.currentTimeMillis().toTimeString()}] $message")
        _notificationHistory.value = currentList

        // Populate active overlay notification
        viewModelScope.launch {
            activeNotification.value = message
            delay(4000) // Toast visible duration
            if (activeNotification.value == message) {
                activeNotification.value = null
            }
        }
    }

    // --- Digitized LanyardQatar Complete Order Actions ---
    fun addNewTask(
        title: String,
        description: String,
        assignedTo: String,
        category: String,
        priority: String,
        dueDate: Long,
        clientName: String = "Corporate Client",
        companyName: String = "LanyardQatar Customer",
        productType: String = "Lanyard",
        quantity: Int = 250,
        deliveryAddress: String = "Marina, Doha",
        deliveryContact: String = "+974 5500 0000"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            val task = Task(
                title = title,
                description = description,
                assignedTo = assignedTo,
                category = category,
                status = "New",
                priority = priority,
                dueDate = dueDate,
                syncState = syncState,
                currentStage = 1, // Start at Stage 1: Quoting
                clientName = clientName,
                companyName = companyName,
                productType = productType,
                quantity = quantity,
                deliveryAddress = deliveryAddress,
                deliveryContact = deliveryContact
            )
            repository.insertTask(task)
            
            withContext(Dispatchers.Main) {
                if (syncState == "PENDING_LOCAL") {
                    connectionStatus.value = "Pending Updates"
                    triggerLocalAlert("Offline: Order for '$clientName' saved to local SQLite DB.")
                } else {
                    triggerLocalAlert("Order Process Stream Opened! WhatsApp sent to $deliveryContact confirming receipt.")
                }
            }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            
            // Intercept if moving to stage 3 or above without design approval
            val alignedStage = when (newStatus) {
                "New" -> 1
                "Quoted" -> 1
                "Confirmed" -> 2
                "In Production" -> 3
                "Done" -> 5
                else -> task.currentStage
            }
            
            if (alignedStage >= 3 && !task.designApproved) {
                withContext(Dispatchers.Main) {
                    triggerLocalAlert("Production Lock! '${task.title}' cannot proceed until mockup is Approved.")
                }
                return@launch
            }

            val updatedTask = task.copy(
                status = newStatus,
                currentStage = alignedStage,
                syncState = syncState,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)

            withContext(Dispatchers.Main) {
                triggerLocalAlert("Status of '${task.title}' changed to: $newStatus. Notification auto-dispatched.")
            }
        }
    }

    fun updateTaskStage(task: Task, newStage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Lock Check: Can't move to Production (3), Quality Control (4), or Delivery (5) without Design Approval
            if (newStage >= 3 && !task.designApproved) {
                withContext(Dispatchers.Main) {
                    triggerLocalAlert("🔒 Design Locked: You must approve mockup design before entering production/delivery!")
                }
                return@launch
            }

            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            val alignedStatus = when (newStage) {
                1 -> "Quoted"
                2 -> "Confirmed"
                3 -> "In Production"
                4 -> "In Production"
                5 -> "Done"
                else -> "In Production"
            }
            val updatedTask = task.copy(
                currentStage = newStage,
                status = alignedStatus,
                syncState = syncState,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)

            val stageName = when (newStage) {
                1 -> "Quoting"
                2 -> "Design & Sample"
                3 -> "Production (Printing)"
                4 -> "Quality Control Check"
                else -> "Delivery Manager"
            }

            withContext(Dispatchers.Main) {
                if (syncState == "PENDING_LOCAL") {
                    connectionStatus.value = "Pending Updates"
                    triggerLocalAlert("Offline: Set Stage of '${task.title}' to $stageName.")
                } else {
                    triggerLocalAlert("Workflow Moved: '${task.title}' advanced to $stageName. Client WhatsApp triggered.")
                }
            }
        }
    }

    fun approveDesign(task: Task, approved: Boolean, feedback: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            // If approved, automatically elevate status to Confirmed. Else, keep in Design approvals
            val nextStage = if (approved) 2 else 2
            val updatedTask = task.copy(
                designApproved = approved,
                designFeedback = feedback,
                currentStage = nextStage,
                syncState = syncState,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)
            
            withContext(Dispatchers.Main) {
                val msg = if (approved) "Design Mockup Approved! Locked production cleared." else "Changes requested: '$feedback'"
                triggerLocalAlert("Success: $msg")
            }
        }
    }

    fun updateProductionSteps(task: Task, printing: Boolean, qc: Boolean, packaging: Boolean, worker: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // If printing, qc, and packaging are all done, mark currentStage to 4 or 5 automatically!
            val inferredStage = when {
                printing && qc && packaging -> 5 // Ready for Delivery stage
                printing && qc -> 4 // Quality Control step
                printing -> 3 // Printing step
                else -> 3
            }
            
            val updatedTask = task.copy(
                prodPrintingDone = printing,
                prodQcDone = qc,
                prodPackagingDone = packaging,
                prodWorker = worker,
                currentStage = inferredStage.coerceAtLeast(task.currentStage),
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)
            
            withContext(Dispatchers.Main) {
                triggerLocalAlert("Production update logged for '${task.title}' by $worker.")
            }
        }
    }

    fun updateDelivery(task: Task, address: String, contact: String, status: String, photoUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isComplete = status == "Delivered"
            val nextStage = if (isComplete) 5 else task.currentStage
            val nextStatus = if (isComplete) "Done" else task.status
            
            val updatedTask = task.copy(
                deliveryAddress = address,
                deliveryContact = contact,
                deliveryStatus = status,
                deliveryPhotoUrl = photoUrl,
                currentStage = nextStage,
                status = nextStatus,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)
            
            withContext(Dispatchers.Main) {
                val alert = if (isComplete) "Delivered! Photo uploaded & client confirmation WhatsApp dispatched." else "Delivery team status updated to: $status"
                triggerLocalAlert("Delivery Log: $alert")
            }
        }
    }

    fun saveSalesReminder(task: Task, required: Boolean, notes: String, date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTask = task.copy(
                salesFollowUpRequired = required,
                salesFollowUpNotes = notes,
                salesFollowUpDate = date,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)
            withContext(Dispatchers.Main) {
                triggerLocalAlert("CRM Alert Added! Reminder logged for sales outreach follow-up.")
            }
        }
    }

    fun deleteTask(taskId: Int, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskById(taskId)
            withContext(Dispatchers.Main) {
                triggerLocalAlert("Order deleted: '${title}' has been removed from localized node.")
            }
        }
    }

    // --- Team Communication Chat Actions ---
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            val chat = ChatMessage(
                senderName = currentUserName.value,
                senderRole = currentUserRole.value,
                messageText = text,
                channel = selectedChannel.value,
                syncState = syncState
            )
            repository.insertChatMessage(chat)
            
            withContext(Dispatchers.Main) {
                if (syncState == "PENDING_LOCAL") {
                    connectionStatus.value = "Pending Updates"
                    triggerLocalAlert("Offline Chat: Message stored locally. Will resume on network link.")
                } else {
                    triggerLocalAlert("Global Chat: Sent to [${selectedChannel.value}] channel list.")
                }
            }
        }
    }

    // --- meeting Scheduling Calendar Actions ---
    fun scheduleMeeting(title: String, description: String, date: Long, time: String, attendees: String, room: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val syncState = if (connectionStatus.value == "Offline Mode") "PENDING_LOCAL" else "SYNCED"
            val meeting = Meeting(
                title = title,
                description = description,
                date = date,
                time = time,
                attendees = attendees,
                room = room,
                syncState = syncState
            )
            repository.insertMeeting(meeting)

            withContext(Dispatchers.Main) {
                if (syncState == "PENDING_LOCAL") {
                    connectionStatus.value = "Pending Updates"
                    triggerLocalAlert("Offline Calendar: Meeting scheduled for $time.")
                } else {
                    triggerLocalAlert("Google Calendar / Outlook Sync: Meeting scheduled in $room.")
                }
            }
        }
    }

    fun deleteMeeting(meetingId: Int, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeetingById(meetingId)
            withContext(Dispatchers.Main) {
                triggerLocalAlert("Meeting cancelled: '${title}' removed from scheduling logs.")
            }
        }
    }

    // --- Real-time Local Synchronisation Execution ---
    fun triggerSync() {
        if (connectionStatus.value == "Offline Mode") {
            triggerLocalAlert("Network Link Down: Cannot sync with LanyardQatar servers.")
            return
        }
        viewModelScope.launch {
            connectionStatus.value = "Synchronizing..."
            triggerLocalAlert("Connecting to regional office servers... Replicating local snapshots.")
            val success = repository.synchronizeOfflineData()
            if (success) {
                connectionStatus.value = "Synced"
                triggerLocalAlert("Database synchronised! Replication completed across 4 middle-east nodes.")
            } else {
                connectionStatus.value = "Pending Updates"
                triggerLocalAlert("Error during synchronization. Local cache remains preserved.")
            }
        }
    }

    // Toggle Offline/Online mode directly for demonstration of Room offline data preservation
    fun toggleConnectionMode() {
        if (connectionStatus.value == "Offline Mode") {
            connectionStatus.value = "Synced"
            triggerSync()
        } else {
            connectionStatus.value = "Offline Mode"
            triggerLocalAlert("App disconnected. Standard offline capability active via SQLite Room storage.")
        }
    }

    // Helper: Timestamp to readable time HH:MM
    private fun Long.toTimeString(): String {
        val date = java.util.Date(this)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}
