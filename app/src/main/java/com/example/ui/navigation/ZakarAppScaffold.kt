package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ConflictResult
import com.example.data.model.MinistryNote
import com.example.data.model.MinistryTask
import com.example.localization.AppLanguage
import com.example.localization.TranslationStrings
import com.example.ui.components.OfficialLogo
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DivineGold
import com.example.ui.theme.DivineGoldLight
import com.example.ui.theme.MidnightBlue
import com.example.viewmodel.AssistantChatMessage
import com.example.viewmodel.NavDestination
import kotlinx.coroutines.launch

data class DrawerItem(
    val destination: NavDestination,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakarAppScaffold(
    currentNav: NavDestination,
    strings: TranslationStrings,
    currentLanguage: AppLanguage,
    pastoralTitle: String,
    allTasks: List<MinistryTask>,
    completedTasks: List<MinistryTask>,
    alarmsList: List<MinistryTask>,
    allNotes: List<MinistryNote>,
    chatMessages: List<AssistantChatMessage>,
    isThinking: Boolean,
    lastConflictResult: ConflictResult?,
    appLockEnabled: Boolean,
    appLockPin: String,
    vibrationEnabled: Boolean,
    themeMode: String,
    currentRingtoneUri: String,
    currentRingtoneTitle: String,
    onNavigate: (NavDestination) -> Unit,
    onToggleTaskComplete: (MinistryTask) -> Unit,
    onSaveTask: (MinistryTask) -> Unit,
    onDeleteTask: (MinistryTask) -> Unit,
    onPostponeTask: (MinistryTask, Int) -> Unit,
    onCheckConflict: (Long, Long) -> Unit,
    onTriggerTestAlarm: () -> Unit,
    onSaveNote: (MinistryNote) -> Unit,
    onDeleteNote: (MinistryNote) -> Unit,
    onToggleFavoriteNote: (MinistryNote) -> Unit,
    onConvertNoteToTask: (MinistryNote, Long, Boolean) -> Unit,
    onSendAssistantMessage: (String) -> Unit,
    onConfirmAssistantTask: (String, MinistryTask) -> Unit,
    onCancelAssistantProposal: (String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onTitleChange: (String) -> Unit,
    onAppLockChange: (Boolean, String) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onRingtoneChange: (String, String) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Back handler to navigate back to Home if in another screen
    BackHandler(enabled = currentNav != NavDestination.HOME || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onNavigate(NavDestination.HOME)
        }
    }

    val drawerItems = listOf(
        DrawerItem(NavDestination.HOME, strings.navHome, Icons.Default.Home, "drawer_home"),
        DrawerItem(NavDestination.ASSISTANT, strings.navAssistant, Icons.Default.AutoAwesome, "drawer_assistant"),
        DrawerItem(NavDestination.TASKS, strings.navTasks, Icons.Default.Checklist, "drawer_tasks"),
        DrawerItem(NavDestination.AGENDA, strings.navAgenda, Icons.Default.CalendarMonth, "drawer_agenda"),
        DrawerItem(NavDestination.NOTES, strings.navNotes, Icons.Default.Description, "drawer_notes"),
        DrawerItem(NavDestination.ALARMS, strings.navAlarms, Icons.Default.Alarm, "drawer_alarms"),
        DrawerItem(NavDestination.HISTORY, strings.navHistory, Icons.Default.History, "drawer_history"),
        DrawerItem(NavDestination.STATS, strings.navStats, Icons.Default.Assessment, "drawer_stats"),
        DrawerItem(NavDestination.SETTINGS, strings.navSettings, Icons.Default.Settings, "drawer_settings"),
        DrawerItem(NavDestination.ABOUT, strings.navAbout, Icons.Default.Info, "drawer_about")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header with Official Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DeepNavy,
                                    CelestialBlue
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        OfficialLogo(size = 64)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "MPAMPAHATSIARO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = DivineGold
                        )
                        Text(
                            text = "זָכַר  (ZAKAR)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DivineGoldLight
                            )
                        )
                        Text(
                            text = "$pastoralTitle · ${currentLanguage.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                HorizontalDivider(color = DivineGold.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(8.dp))

                // 10 Drawer Navigation Items
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    drawerItems.forEach { item ->
                        val isSelected = currentNav == item.destination
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) MidnightBlue else DivineGold
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                onNavigate(item.destination)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DivineGold,
                                selectedTextColor = MidnightBlue,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentNav) {
                                    NavDestination.HOME -> "זָכַר · ${strings.appTitle}"
                                    NavDestination.ASSISTANT -> strings.navAssistant
                                    NavDestination.TASKS -> strings.navTasks
                                    NavDestination.AGENDA -> strings.navAgenda
                                    NavDestination.NOTES -> strings.navNotes
                                    NavDestination.ALARMS -> strings.navAlarms
                                    NavDestination.HISTORY -> strings.navHistory
                                    NavDestination.STATS -> strings.navStats
                                    NavDestination.SETTINGS -> strings.navSettings
                                    NavDestination.ABOUT -> strings.navAbout
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1,
                                color = DivineGold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("app_topbar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = DivineGold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigate(NavDestination.ABOUT) }) {
                            OfficialLogo(size = 32)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = DivineGold
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val bottomItems = listOf(
                        DrawerItem(NavDestination.HOME, strings.navHome, Icons.Default.Home, "bottom_home"),
                        DrawerItem(NavDestination.ASSISTANT, strings.navAssistant, Icons.Default.AutoAwesome, "bottom_assistant"),
                        DrawerItem(NavDestination.TASKS, strings.navTasks, Icons.Default.Checklist, "bottom_tasks"),
                        DrawerItem(NavDestination.AGENDA, strings.navAgenda, Icons.Default.CalendarMonth, "bottom_agenda"),
                        DrawerItem(NavDestination.NOTES, strings.navNotes, Icons.Default.Description, "bottom_notes")
                    )

                    bottomItems.forEach { item ->
                        val isSelected = currentNav == item.destination
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    maxLines = 1
                                )
                            },
                            selected = isSelected,
                            onClick = { onNavigate(item.destination) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MidnightBlue,
                                selectedTextColor = DivineGold,
                                indicatorColor = DivineGold,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentNav) {
                    NavDestination.HOME -> HomeScreen(
                        strings = strings,
                        pastoralTitle = pastoralTitle,
                        allTasks = allTasks,
                        completedTasks = completedTasks,
                        alarmsList = alarmsList,
                        onNavigate = onNavigate,
                        onToggleTaskComplete = onToggleTaskComplete,
                        onQuickAssistantQuery = { query ->
                            onSendAssistantMessage(query)
                        }
                    )
                    NavDestination.ASSISTANT -> AssistantScreen(
                        strings = strings,
                        currentLanguage = currentLanguage,
                        messages = chatMessages,
                        isThinking = isThinking,
                        onSendMessage = onSendAssistantMessage,
                        onConfirmTask = onConfirmAssistantTask,
                        onCancelProposal = onCancelAssistantProposal
                    )
                    NavDestination.TASKS -> TasksScreen(
                        strings = strings,
                        allTasks = allTasks,
                        lastConflictResult = lastConflictResult,
                        onSaveTask = onSaveTask,
                        onDeleteTask = onDeleteTask,
                        onToggleComplete = onToggleTaskComplete,
                        onPostponeTask = onPostponeTask,
                        onCheckConflict = onCheckConflict
                    )
                    NavDestination.AGENDA -> AgendaScreen(
                        strings = strings,
                        allTasks = allTasks,
                        onToggleComplete = onToggleTaskComplete
                    )
                    NavDestination.NOTES -> NotesScreen(
                        strings = strings,
                        notes = allNotes,
                        onSaveNote = onSaveNote,
                        onDeleteNote = onDeleteNote,
                        onToggleFavorite = onToggleFavoriteNote,
                        onConvertNoteToTask = onConvertNoteToTask
                    )
                    NavDestination.ALARMS -> AlarmsScreen(
                        strings = strings,
                        alarms = alarmsList,
                        currentRingtoneUri = currentRingtoneUri,
                        currentRingtoneTitle = currentRingtoneTitle,
                        onRingtoneChange = onRingtoneChange,
                        onToggleAlarm = { task -> onSaveTask(task.copy(hasAlarm = !task.hasAlarm)) },
                        onTestAlarm = onTriggerTestAlarm
                    )
                    NavDestination.HISTORY -> HistoryScreen(
                        strings = strings,
                        completedTasks = completedTasks
                    )
                    NavDestination.STATS -> StatsScreen(
                        strings = strings,
                        allTasks = allTasks,
                        completedTasks = completedTasks
                    )
                    NavDestination.SETTINGS -> SettingsScreen(
                        strings = strings,
                        currentLanguage = currentLanguage,
                        currentPastoralTitle = pastoralTitle,
                        appLockEnabled = appLockEnabled,
                        currentPin = appLockPin,
                        vibrationEnabled = vibrationEnabled,
                        themeMode = themeMode,
                        currentRingtoneUri = currentRingtoneUri,
                        currentRingtoneTitle = currentRingtoneTitle,
                        onLanguageChange = onLanguageChange,
                        onTitleChange = onTitleChange,
                        onAppLockChange = onAppLockChange,
                        onVibrationChange = onVibrationChange,
                        onThemeChange = onThemeChange,
                        onRingtoneChange = onRingtoneChange,
                        onExportBackup = onExportBackup,
                        onImportBackup = onImportBackup
                    )
                    NavDestination.ABOUT -> AboutScreen(
                        strings = strings
                    )
                }
            }
        }
    }
}
