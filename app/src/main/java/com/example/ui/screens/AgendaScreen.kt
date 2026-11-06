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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MinistryTask
import com.example.localization.TranslationStrings
import com.example.ui.theme.DivineGold
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusOverdue
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AgendaScreen(
    strings: TranslationStrings,
    allTasks: List<MinistryTask>,
    onToggleComplete: (MinistryTask) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Day, 1: Week, 2: Month
    var currentBaseDate by remember { mutableStateOf(Calendar.getInstance()) }

    val dateFormat = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Calculate active window based on selectedTab
    val cal = currentBaseDate.clone() as Calendar
    val (windowStart, windowEnd, headerLabel) = remember(selectedTab, currentBaseDate.timeInMillis) {
        when (selectedTab) {
            0 -> {
                // Day view
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val s = cal.timeInMillis
                val e = s + (24 * 3600 * 1000L) - 1
                Triple(s, e, dateFormat.format(Date(s)).replaceFirstChar { it.uppercase() })
            }
            1 -> {
                // Week view
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val s = cal.timeInMillis
                val e = s + (7 * 24 * 3600 * 1000L) - 1
                val sDate = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(s))
                val eDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(e))
                Triple(s, e, "$sDate – $eDate")
            }
            else -> {
                // Month view
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val s = cal.timeInMillis
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                val e = cal.timeInMillis
                Triple(s, e, monthFormat.format(Date(s)).replaceFirstChar { it.uppercase() })
            }
        }
    }

    val tasksInWindow = allTasks.filter { it.dateTimeMillis in windowStart..windowEnd }
        .sortedBy { it.dateTimeMillis }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row: Day, Week, Month
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DivineGold
                )
            }
        ) {
            val tabs = listOf(strings.agendaDayView, strings.agendaWeekView, strings.agendaMonthView)
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedTab == index) DivineGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Navigation Header (Previous, Date, Next)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val newCal = currentBaseDate.clone() as Calendar
                        when (selectedTab) {
                            0 -> newCal.add(Calendar.DAY_OF_YEAR, -1)
                            1 -> newCal.add(Calendar.WEEK_OF_YEAR, -1)
                            2 -> newCal.add(Calendar.MONTH, -1)
                        }
                        currentBaseDate = newCal
                    }
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.previous, tint = DivineGold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = headerLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.today,
                        style = MaterialTheme.typography.labelSmall.copy(color = DivineGold, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clickable { currentBaseDate = Calendar.getInstance() }
                            .padding(2.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val newCal = currentBaseDate.clone() as Calendar
                        when (selectedTab) {
                            0 -> newCal.add(Calendar.DAY_OF_YEAR, 1)
                            1 -> newCal.add(Calendar.WEEK_OF_YEAR, 1)
                            2 -> newCal.add(Calendar.MONTH, 1)
                        }
                        currentBaseDate = newCal
                    }
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = strings.next, tint = DivineGold)
                }
            }
        }

        // Content
        if (tasksInWindow.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = DivineGold.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.noTasksToday,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasksInWindow, key = { it.id }) { task ->
                    AgendaItemCard(
                        task = task,
                        strings = strings,
                        onToggleComplete = { onToggleComplete(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaItemCard(
    task: MinistryTask,
    strings: TranslationStrings,
    onToggleComplete: () -> Unit
) {
    val dateTimeFormat = remember { SimpleDateFormat("EEEE d MMM · HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) StatusCompleted else DivineGold)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = dateTimeFormat.format(Date(task.dateTimeMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = DivineGold
                )

                Text(
                    text = MainViewModel.getCategoryLocalizedName(task.categoryKey, strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (task.hasAlarm) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = DivineGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
