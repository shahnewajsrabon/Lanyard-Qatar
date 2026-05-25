package com.example.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.Task

@Composable
fun EmployeeTaskModuleView(tasks: List<Task>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Employee Task Assignment (কর্মচারী টাস্ক)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val tasksByAssignee = tasks.groupBy { it.assignedTo }

        tasksByAssignee.forEach { (assignee, assignedTasks) ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assignee: $assignee", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        assignedTasks.forEach { task ->
                            Text("- ${task.title} (Status: ${task.status})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
