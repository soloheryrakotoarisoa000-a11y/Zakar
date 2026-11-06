package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConflictResult
import com.example.data.model.MinistryTask
import com.example.localization.TranslationStrings
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DivineGold
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPending
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptBitmap
@Composable
fun TasksScreen(
    strings: TranslationStrings,
    allTasks: List<MinistryTask>,
    lastConflictResult: ConflictResult?,
    onSaveTask: (MinistryTask) -> Unit,
    onDeleteTask: (MinistryTask) -> Unit,
    onToggleComplete: (MinistryTask) -> Unit,
    onPostponeTask: (MinistryTask, Int) -> Unit,
    onCheckConflict: (Long, Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, OVERDUE, COMPLETED
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<MinistryTask?>(null) }

    val now = System.currentTimeMillis()
    val todayCal = Calendar.getInstance()
    val startOfDay = todayCal.apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfDay = startOfDay + (24 * 3600 * 1000L) - 1

    val filteredTasks = remember(allTasks, selectedFilter) {
        when (selectedFilter) {
            "TODAY" -> allTasks.filter { !it.isCompleted && it.dateTimeMillis in startOfDay..endOfDay }
            "OVERDUE" -> allTasks.filter { !it.isCompleted && it.dateTimeMillis < startOfDay }
            "COMPLETED" -> allTasks.filter { it.isCompleted }
            else -> allTasks.filter { !it.isCompleted }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filters Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to strings.all,
                    "TODAY" to strings.tasksToday,
                    "OVERDUE" to strings.overdueTasks,
                    "COMPLETED" to strings.completedTasks
                )
                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) DivineGold else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedFilter = key },
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MidnightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noTasksToday,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskDetailedCard(
                            task = task,
                            strings = strings,
                            onToggleComplete = { onToggleComplete(task) },
                            onEdit = {
                                taskToEdit = task
                                showEditDialog = true
                            },
                            onDelete = { onDeleteTask(task) },
                            onPostpone = { hours -> onPostponeTask(task, hours) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Task
        FloatingActionButton(
            onClick = {
                taskToEdit = null
                showEditDialog = true
            },
            containerColor = DivineGold,
            contentColor = MidnightBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = strings.addTask)
        }
    }

    if (showEditDialog) {
        TaskEditDialog(
            task = taskToEdit,
            strings = strings,
            conflictResult = lastConflictResult,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTask ->
                onSaveTask(updatedTask)
                showEditDialog = false
            },
            onCheckConflict = onCheckConflict
        )
    }
}

@Composable
fun TaskDetailedCard(
    task: MinistryTask,
    strings: TranslationStrings,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPostpone: (Int) -> Unit
) {
    val dateTimeFormat = remember { SimpleDateFormat("EEEE d MMM yyyy à HH:mm", Locale.getDefault()) }
    val isOverdue = !task.isCompleted && task.dateTimeMillis < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = DivineGold,
                        checkmarkColor = MidnightBlue
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = MainViewModel.getCategoryLocalizedName(task.categoryKey, strings),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DivineGold
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusOverdue.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time & Recurrence Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (isOverdue) StatusOverdue else DivineGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateTimeFormat.format(Date(task.dateTimeMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) StatusOverdue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                if (!task.isCompleted) {
                    OutlinedButton(
                        onClick = { onPostpone(24) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "+1j", fontSize = 11.sp, color = DivineGold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    task: MinistryTask?,
    strings: TranslationStrings,
    conflictResult: ConflictResult?,
    onDismiss: () -> Unit,
    onSave: (MinistryTask) -> Unit,
    onCheckConflict: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var categoryKey by remember { mutableStateOf(task?.categoryKey ?: "catSermons") }
    var dateTimeMillis by remember { mutableLongStateOf(task?.dateTimeMillis ?: (System.currentTimeMillis() + 3600 * 1000L)) }
    var hasAlarm by remember { mutableStateOf(task?.hasAlarm ?: true) }
    var repeatType by remember { mutableStateOf(task?.repeatType ?: "NONE") }

    val cal = remember(dateTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = dateTimeMillis }
    }

    LaunchedEffect(dateTimeMillis) {
        onCheckConflict(dateTimeMillis, task?.id ?: 0L)
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (task == null) strings.addTask else strings.editTask,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DivineGold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.taskTitle) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.taskDescription) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Date and Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dateTimeMillis
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    dateTimeMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateFormat.format(Date(dateTimeMillis)), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dateTimeMillis
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, min)
                                    }
                                    dateTimeMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(timeFormat.format(Date(dateTimeMillis)), fontSize = 12.sp)
                    }
                }

                // Conflict warning
                if (conflictResult?.hasConflict == true) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusOverdue.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusOverdue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.conflictWarning}\n${conflictResult.conflictingTask?.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusOverdue
                            )
                        }
                    }
                }

                // Alarm switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.taskAlarmToggle, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = hasAlarm,
                        onCheckedChange = { hasAlarm = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DivineGold)
                    )
                }

                // Category selector
                Text(text = "${strings.taskCategory} : ${MainViewModel.getCategoryLocalizedName(categoryKey, strings)}", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val cats = listOf(
                        "catSermons", "catPrayers", "catPastoralVisits",
                        "catMeetings", "catBibleStudy", "catWeddings", "catEvangelism"
                    )
                    items(cats) { c ->
                        val isSel = categoryKey == c
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) DivineGold else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { categoryKey = c }
                        ) {
                            Text(
                                text = MainViewModel.getCategoryLocalizedName(c, strings),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) MidnightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val updated = task?.copy(
                            title = title,
                            description = description,
                            categoryKey = categoryKey,
                            dateTimeMillis = dateTimeMillis,
                            hasAlarm = hasAlarm,
                            repeatType = repeatType
                        ) ?: MinistryTask(
                            title = title,
                            description = description,
                            categoryKey = categoryKey,
                            dateTimeMillis = dateTimeMillis,
                            hasAlarm = hasAlarm,
                            repeatType = repeatType
                        )
                        onSave(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DivineGold, contentColor = MidnightBlue)
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
annotation class OptBitmap
