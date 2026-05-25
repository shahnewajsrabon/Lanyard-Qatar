package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class WorkspaceRepository(private val workspaceDao: WorkspaceDao) {

    val allTasks: Flow<List<Task>> = workspaceDao.getAllTasks()
    val allChatMessages: Flow<List<ChatMessage>> = workspaceDao.getAllChatMessages()
    val allMeetings: Flow<List<Meeting>> = workspaceDao.getAllMeetings()

    fun getChatMessagesByChannel(channel: String): Flow<List<ChatMessage>> {
        return workspaceDao.getChatMessagesByChannel(channel)
    }

    suspend fun insertTask(task: Task) {
        workspaceDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        workspaceDao.updateTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        workspaceDao.deleteTaskById(id)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        workspaceDao.insertChatMessage(message)
    }

    suspend fun insertMeeting(meeting: Meeting) {
        workspaceDao.insertMeeting(meeting)
    }

    suspend fun deleteMeetingById(id: Int) {
        workspaceDao.deleteMeetingById(id)
    }

    // --- Simulated Synchronisation with LanyardQatar Cloud Servers ---
    suspend fun synchronizeOfflineData(): Boolean {
        // Simulating network delay
        kotlinx.coroutines.delay(1800)
        
        try {
            // Bulk sync changes
            workspaceDao.markAllTasksSynced()
            workspaceDao.markAllChatsSynced()
            workspaceDao.markAllMeetingsSynced()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    // --- Pre-populate / Seeding initial data ---
    suspend fun seedDatabaseIfEmpty() {
        val currentTasks = allTasks.first()
        if (currentTasks.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            // Initial Seed Tasks (Representing LanyardQatar Corporate Orders)
            val seedTasks = listOf(
                Task(
                    title = "UDC National Day Premium Lanyards",
                    description = "Satin finish lanyards with silver chrome hooks & quick-release safety buckles. Approved corporate colours matching Qatar National Day specs.",
                    assignedTo = "Zainab Rashid",
                    category = "Operations",
                    status = "Confirmed",
                    priority = "HIGH",
                    dueDate = now + (dayMs * 3),
                    syncState = "SYNCED",
                    currentStage = 3, // In Production
                    clientName = "UDC (Pearl Island)",
                    companyName = "United Development Company",
                    productType = "Lanyard",
                    quantity = 1500,
                    designApproved = true,
                    designFeedback = "Awesome design, blue matches Qatar flag contrast specs perfectly.",
                    designImageUrl = "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=400",
                    prodPrintingDone = true,
                    prodQcDone = false,
                    prodPackagingDone = false,
                    prodWorker = "Ahmed Al-Mansoori",
                    deliveryAddress = "Marina Tower, Pearl Island, Doha, Qatar",
                    deliveryContact = "+974 4409 1111"
                ),
                Task(
                    title = "FM Stadium Staff Credentials",
                    description = "Heavy duty double-clip plastic woven badges with holographic security overlay.",
                    assignedTo = "Ahmed Al-Mansoori",
                    category = "Operations",
                    status = "New",
                    priority = "MEDIUM",
                    dueDate = now + (dayMs * 7),
                    syncState = "SYNCED",
                    currentStage = 1, // New Inquiry
                    clientName = "FM (Doha)",
                    companyName = "Facilities Management Qatar",
                    productType = "Badge",
                    quantity = 800,
                    designApproved = false,
                    designFeedback = "Awaiting high-res logo upload from FM Qatar branding team.",
                    designImageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
                    prodPrintingDone = false,
                    prodQcDone = false,
                    prodPackagingDone = false,
                    deliveryAddress = "FM Qatar HQ, Al Ring Rd, Doha",
                    deliveryContact = "+974 5511 2233"
                ),
                Task(
                    title = "Qatar Airways Expo Wristbands",
                    description = "Soft elastic fabric RFID wristbands with custom printed logo and chips programmed for automated gate ingress.",
                    assignedTo = "Sara Al-Khouri",
                    category = "Operations",
                    status = "Done",
                    priority = "HIGH",
                    dueDate = now - (dayMs * 2), // Past due, delivered!
                    syncState = "SYNCED",
                    currentStage = 5, // Delivery
                    clientName = "Qatar Airways",
                    companyName = "Qatar Airways Group",
                    productType = "Wristband",
                    quantity = 5000,
                    designApproved = true,
                    designFeedback = "Excellent sample, chips read correctly on Android tag readers.",
                    designImageUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=400",
                    prodPrintingDone = true,
                    prodQcDone = true,
                    prodPackagingDone = true,
                    prodWorker = "Zainab Rashid",
                    deliveryAddress = "Qatar Airways Tower 1, Airport Rd, Doha",
                    deliveryContact = "+974 4449 2222",
                    deliveryStatus = "Delivered",
                    deliveryPhotoUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=400", // package box photo
                    salesFollowUpRequired = true,
                    salesFollowUpDate = now + (dayMs * 20), // 3 weeks out
                    salesFollowUpNotes = "Reach out regarding the upcoming winter flight steward credentials renewal event!"
                ),
                Task(
                    title = "MoE IT Support Wristbands & Badges",
                    description = "Woven polyester lanyards with cardholders for students of Qatar Science and Technology School.",
                    assignedTo = "Youssef Ibrahim",
                    category = "Operations",
                    status = "Quoted",
                    priority = "LOW",
                    dueDate = now + (dayMs * 12),
                    syncState = "SYNCED",
                    currentStage = 2, // Design and approvals
                    clientName = "Ministry of Ed",
                    companyName = "Qatar Ministry of Education",
                    productType = "Mousepad",
                    quantity = 1200,
                    designApproved = false,
                    designFeedback = "Logo size must be exactly 1.5 inches. Please revise and resubmit mockups on Saturday.",
                    designImageUrl = "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400",
                    prodPrintingDone = false,
                    prodQcDone = false,
                    prodPackagingDone = false,
                    deliveryAddress = "MoE HQ, West Bay, Doha",
                    deliveryContact = "+974 4404 8888"
                )
            )

            for (task in seedTasks) {
                workspaceDao.insertTask(task)
            }

            // Initial Seed Chats
            val seedChats = listOf(
                ChatMessage(
                    senderName = "Sara Al-Khouri",
                    senderRole = "HR & Safety Manager",
                    messageText = "Welcome to LanyardQatar Unified Workspace! Ensure your biometric sign-in is enabled in settings.",
                    timestamp = now - (dayMs / 4),
                    channel = "General"
                ),
                ChatMessage(
                    senderName = "Ahmed Al-Mansoori",
                    senderRole = "Logistics Admin",
                    messageText = "Port shipment clearance is underway. I've logged a high priority task.",
                    timestamp = now - (dayMs / 6),
                    channel = "General"
                ),
                ChatMessage(
                    senderName = "Youssef Ibrahim",
                    senderRole = "Technical Lead",
                    messageText = "Corporate website tool updated successfully - testing database replication across hubs.",
                    timestamp = now - (dayMs / 12),
                    channel = "Tasks Support"
                )
            )

            for (chat in seedChats) {
                workspaceDao.insertChatMessage(chat)
            }

            // Initial Seed Meetings
            val seedMeetings = listOf(
                Meeting(
                    title = "Weekly LanyardQatar Operations Alignment",
                    description = "Weekly sync on port operations, local stock, and customer distributions.",
                    date = now + dayMs,
                    time = "09:00 AM",
                    attendees = "Youssef, Ahmed, Zainab, Sara, Khalid",
                    room = "Doha Boardroom A & Zoom"
                ),
                Meeting(
                    title = "IT Disaster Recovery & Redundancy Test",
                    description = "Evaluating offline local database sync across branch offices.",
                    date = now + (dayMs * 3),
                    time = "02:30 PM",
                    attendees = "Zainab Rashid, IT Team",
                    room = "Doha Sever Room Hub"
                )
            )

            for (meeting in seedMeetings) {
                workspaceDao.insertMeeting(meeting)
            }
        }
    }
}
