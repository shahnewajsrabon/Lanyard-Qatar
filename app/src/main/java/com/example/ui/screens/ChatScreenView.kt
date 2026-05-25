package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.WorkspaceViewModel

@Composable
fun ChatScreenView(viewModel: WorkspaceViewModel) {
    val messages by viewModel.currentChannelMessages.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto scroll chat to end when messages log updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Team Unified Communication Channels",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Channel select list tabs ("General", "Tasks Support", "System Alerts")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("General", "Tasks Support", "System Alerts").forEach { ch ->
                val isSelected = selectedChannel == ch
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectedChannel.value = ch }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .weight(1f)
                        .testTag("chat_channel_tab_$ch"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ch,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Chat message bubble stream (Room DB backend)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No communications in this channel. Send the first message!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderName == currentUserName
                        ChatBubbleItem(msg = msg, isMe = isMe)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message input compose panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Write to ${selectedChannel}...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )

            FloatingActionButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.size(48.dp).testTag("chat_send_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ChatBubbleItem(msg: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Name and corporate role descriptor tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = msg.senderName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Box(
                modifier = Modifier
                    .background(
                        color = when (msg.senderRole) {
                            "Admin" -> MaterialTheme.colorScheme.errorContainer
                            "Project Manager" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = msg.senderRole,
                    fontSize = 8.sp,
                    color = when (msg.senderRole) {
                        "Admin" -> MaterialTheme.colorScheme.onErrorContainer
                        "Project Manager" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }

        // Color bubbles
        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = if (isMe) 8.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 8.dp
                    )
                )
                .padding(10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(
                    text = msg.messageText,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (msg.syncState == "SYNCED") Icons.Default.DoneAll else Icons.Default.AccessTime,
                        contentDescription = "Sync",
                        tint = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Today",
                        fontSize = 8.sp,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
