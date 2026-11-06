package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MinistryTask
import com.example.localization.TranslationStrings
import com.example.ui.components.BibleVerseCard
import com.example.ui.components.OfficialLogo
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DivineGold
import com.example.ui.theme.DivineGoldLight
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusScheduled
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavDestination
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    strings: TranslationStrings,
    pastoralTitle: String,
    allTasks: List<MinistryTask>,
    completedTasks: List<MinistryTask>,
    alarmsList: List<MinistryTask>,
    onNavigate: (NavDestination) -> Unit,
    onToggleTaskComplete: (MinistryTask) -> Unit,
    onQuickAssistantQuery: (String) -> Unit
) {
    val now = System.currentTimeMillis()
    val todayCalendar = Calendar.getInstance()
    val startOfDay = todayCalendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val endOfDay = startOfDay + (24 * 3600 * 1000L) - 1

    val tasksToday = allTasks.filter { !it.isCompleted && it.dateTimeMillis in startOfDay..endOfDay }
    val overdueTasks = allTasks.filter { !it.isCompleted && it.dateTimeMillis < startOfDay }
    val upcomingAppointments = allTasks.filter { !it.isCompleted && it.dateTimeMillis > endOfDay }
        .take(5)

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val todayFormatted = dateFormat.format(Date(now)).replaceFirstChar { it.uppercase() }

    var quickInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Identity Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    DeepNavy,
                                    CelestialBlue
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OfficialLogo(size = 72)

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MPAMPAHATSIARO",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = DivineGold
                            )
                            Text(
                                text = "זָכַר  (ZAKAR)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DivineGoldLight
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${strings.greetingPrefix}, $pastoralTitle !",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // Bible Verse Card (Mandatory Roman 13:8)
        item {
            BibleVerseCard(
                verseText = strings.verseText,
                verseReference = strings.verseReference
            )
        }

        // Quick Assistant Access Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DivineGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DivineGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MidnightBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.quickAssistantTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DivineGold
                            )
                            Text(
                                text = strings.quickAssistantSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quickInput,
                        onValueChange = { quickInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_quick_assistant_input"),
                        placeholder = {
                            Text(
                                text = strings.askAssistantHint,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (quickInput.isNotBlank()) {
                                        onQuickAssistantQuery(quickInput)
                                        quickInput = ""
                                        onNavigate(NavDestination.ASSISTANT)
                                    }
                                },
                                modifier = Modifier.testTag("home_quick_assistant_send")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Send",
                                    tint = DivineGold
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Dashboard Metrics Grid
        item {
            Text(
                text = "Tableau de Bord du Ministère",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = strings.tasksToday,
                        count = tasksToday.size,
                        icon = Icons.Default.CalendarMonth,
                        iconTint = StatusPending,
                        onClick = { onNavigate(NavDestination.TASKS) }
                    )
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = strings.overdueTasks,
                        count = overdueTasks.size,
                        icon = Icons.Default.Warning,
                        iconTint = StatusOverdue,
                        onClick = { onNavigate(NavDestination.TASKS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = strings.scheduledAlarms,
                        count = alarmsList.size,
                        icon = Icons.Default.Alarm,
                        iconTint = StatusScheduled,
                        onClick = { onNavigate(NavDestination.ALARMS) }
                    )
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = strings.completedTasks,
                        count = completedTasks.size,
                        icon = Icons.Default.CheckCircle,
                        iconTint = StatusCompleted,
                        onClick = { onNavigate(NavDestination.HISTORY) }
                    )
                }
            }
        }

        // Today's Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.tasksToday,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Voir tout",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DivineGold
                    ),
                    modifier = Modifier
                        .clickable { onNavigate(NavDestination.TASKS) }
                        .padding(4.dp)
                )
            }
        }

        if (tasksToday.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.noTasksToday,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(tasksToday) { task ->
                TaskCompactCard(
                    task = task,
                    strings = strings,
                    onToggleComplete = { onToggleTaskComplete(task) }
                )
            }
        }

        // Upcoming Appointments Section
        if (upcomingAppointments.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.upcomingAppointments,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(upcomingAppointments) { task ->
                TaskCompactCard(
                    task = task,
                    strings = strings,
                    onToggleComplete = { onToggleTaskComplete(task) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TaskCompactCard(
    task: MinistryTask,
    strings: TranslationStrings,
    onToggleComplete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${dateFormat.format(Date(task.dateTimeMillis))} à ${timeFormat.format(Date(task.dateTimeMillis))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DivineGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${MainViewModel.getCategoryLocalizedName(task.categoryKey, strings)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (task.hasAlarm) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alarm Active",
                    tint = DivineGold,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp)
                )
            }
        }
    }
}
