package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Meeting
import com.example.WorkspaceViewModel
import com.example.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenView(viewModel: WorkspaceViewModel) {
    val meetings by viewModel.allMeetings.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    var showMeetingDialog by remember { mutableStateOf(false) }
    var calendarIntegratedSync by remember { mutableStateOf(true) }

    val daysMs = 24 * 60 * 60 * 1000L
    val todayMs = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meeting Scheduler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic sync integration toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        calendarIntegratedSync = !calendarIntegratedSync
                        viewModel.triggerLocalAlert(
                            if (calendarIntegratedSync) "Active direct synchronization with Outlook/Google Calendar."
                            else "Disconnected calendar cloud links."
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("calendar_sync_mode_toggle")
            ) {
                Icon(
                    imageVector = if (calendarIntegratedSync) Icons.Default.SyncAlt else Icons.Default.SyncDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (calendarIntegratedSync) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (calendarIntegratedSync) "Linked Calendar" else "Local Only",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (calendarIntegratedSync) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Text(
            text = "Coordinated agendas and client presentations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom horizontal week strip block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (-1..3).forEach { offset ->
                val dayTime = todayMs + (offset * daysMs)
                val isToday = offset == 0
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 10.dp)
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Utils.getDayOfWeek(dayTime).take(3).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = Utils.formatShortDate(dayTime).substringBefore("/"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Meetings list feed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No scheduled corporate forums logged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(meetings) { mt ->
                        MeetingItemCard(
                            meeting = mt,
                            onDelete = { viewModel.deleteMeeting(mt.id, mt.title) },
                            canCancel = currentUserRole == "Admin" || currentUserRole == "Project Manager"
                        )
                    }
                }
            }

            // Quick Floating schedule button
            ExtendedFloatingActionButton(
                onClick = { showMeetingDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("schedule_meeting_fab"),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ) {
                Icon(Icons.Default.Schedule, contentDescription = "Add Calendar Meet")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule Meet")
            }
        }
    }

    // Schedule Dialog builder popup
    if (showMeetingDialog) {
        var meetTitle by remember { mutableStateOf("") }
        var meetDesc by remember { mutableStateOf("") }
        var meetAttendees by remember { mutableStateOf("") }
        var meetRoom by remember { mutableStateOf("Main West Bay Boardroom") }
        var meetTime by remember { mutableStateOf("11:00 AM") }

        Dialog(onDismissRequest = { showMeetingDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Schedule Corporate Meet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = meetTitle,
                        onValueChange = { meetTitle = it },
                        label = { Text("Meeting Topic Topic *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meeting_title_input")
                    )

                    OutlinedTextField(
                        value = meetDesc,
                        onValueChange = { meetDesc = it },
                        label = { Text("Agenda / Deliverables Details *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = meetAttendees,
                        onValueChange = { meetAttendees = it },
                        label = { Text("Staff Attendees * (Comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = meetTime,
                        onValueChange = { meetTime = it },
                        label = { Text("Scheduled Time Slot * (e.g., 03:00 PM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Room Selection dropdown alternative
                    Text(text = "Doha Office Room Location", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Executive Hub A", "West Bay 3BC", "Virtual Zoom").forEach { loc ->
                            FilterChip(
                                selected = meetRoom == loc,
                                onClick = { meetRoom = loc },
                                label = { Text(loc, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showMeetingDialog = false }) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (meetTitle.isNotEmpty() && meetAttendees.isNotEmpty() && meetDesc.isNotEmpty()) {
                                    viewModel.scheduleMeeting(
                                        title = meetTitle,
                                        description = meetDesc,
                                        date = System.currentTimeMillis() + daysMs, // set to tomorrow
                                        time = meetTime,
                                        attendees = meetAttendees,
                                        room = meetRoom
                                    )
                                    showMeetingDialog = false
                                } else {
                                    viewModel.triggerLocalAlert("Please enter all required information.")
                                }
                            },
                            modifier = Modifier.testTag("submit_meeting_button")
                        ) {
                            Text("Confirm Schedule")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingItemCard(
    meeting: Meeting,
    onDelete: () -> Unit,
    canCancel: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Utils.formatDate(meeting.date)} — ${meeting.time}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (canCancel) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancel Meeting",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = meeting.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text(text = meeting.room, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text(text = "Attendees: ${meeting.attendees.split(",").size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
