package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assignedTo: String,
    val category: String, // e.g., "Operations", "Logistics", "IT", "HR"
    val status: String,   // "TODO", "IN_PROGRESS", "COMPLETED" or Order status "New", "Quoted", "Confirmed", "In Production", "Done"
    val priority: String, // "LOW", "MEDIUM", "HIGH"
    val dueDate: Long,
    val syncState: String, // "SYNCED", "PENDING_LOCAL"
    val currentStage: Int = 1, // 1 to 5 linking our automated corporate workflow
    val lastUpdated: Long = System.currentTimeMillis(),
    
    // --- Digitized LanyardQatar Order Lifecycle Fields ---
    val clientName: String = "Corporate Client",       // e.g. "UDC", "FM", "Pearl Island"
    val companyName: String = "LanyardQatar Customer",  // e.g. "United Development Company"
    val productType: String = "Lanyard",               // "Lanyard", "Badge", "Wristband", "Mousepad"
    val quantity: Int = 250,
    
    // Design & Approval
    val designApproved: Boolean = false,
    val designFeedback: String = "",
    val designImageUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400", // simulated mockup
    
    // Production Tracker Sub-steps
    val prodPrintingDone: Boolean = false,
    val prodQcDone: Boolean = false,
    val prodPackagingDone: Boolean = false,
    val prodWorker: String = "Ahmed Al-Mansoori",
    
    // Delivery Manager
    val deliveryAddress: String = "West Bay Financial District, Doha, Qatar",
    val deliveryContact: String = "+974 5511 2233",
    val deliveryPhotoUrl: String = "",
    val deliveryStatus: String = "Pending", // "Pending", "Out for Delivery", "Delivered"
    
    // CRM follow-ups
    val salesFollowUpRequired: Boolean = false,
    val salesFollowUpDate: Long = 0,
    val salesFollowUpNotes: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String, // "Employee", "Project Manager", "Admin"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val channel: String = "General", // "General", "Tasks Support", "System Alerts"
    val syncState: String = "SYNCED" // "SYNCED", "PENDING_LOCAL"
)

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long, // Epoch timestamp (day representation)
    val time: String, // e.g., "10:30 AM"
    val attendees: String, // Comma separated names
    val room: String, // Virtual or physical, e.g., "Office Doha 3B"
    val syncState: String = "SYNCED"
)
