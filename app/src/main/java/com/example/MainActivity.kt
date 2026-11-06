package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.localization.AppLanguage
import com.example.localization.Translations
import com.example.ui.navigation.ZakarAppScaffold
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.theme.ZakarTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavDestination

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission granted or denied handled gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        handleIntent(intent)

        setContent {
            val language by viewModel.languageFlow.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val isUnlocked by viewModel.isUnlocked.collectAsState()
            val appLockEnabled by viewModel.appLockEnabled.collectAsState()
            val currentNav by viewModel.currentNav.collectAsState()
            val pastoralTitle by viewModel.pastoralTitle.collectAsState()
            val allTasks by viewModel.allTasks.collectAsState()
            val activeTasks by viewModel.activeTasks.collectAsState()
            val completedTasks by viewModel.completedTasks.collectAsState()
            val alarmsList by viewModel.alarmsList.collectAsState()
            val allNotes by viewModel.allNotes.collectAsState()
            val chatMessages by viewModel.chatMessages.collectAsState()
            val isThinking by viewModel.isAssistantThinking.collectAsState()
            val lastConflictResult by viewModel.lastConflictResult.collectAsState()
            val currentPin by viewModel.appLockPin.collectAsState()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
            val alarmRingtoneUri by viewModel.alarmRingtoneUri.collectAsState()
            val alarmRingtoneTitle by viewModel.alarmRingtoneTitle.collectAsState()

            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ZakarTheme(darkTheme = isDarkTheme) {
                if (language == null) {
                    // First launch language selection screen
                    LanguageSelectionScreen(
                        currentLanguage = AppLanguage.FR,
                        onLanguageSelected = { selected ->
                            viewModel.setLanguage(selected)
                        }
                    )
                } else {
                    val strings = Translations.get(language!!)

                    if (appLockEnabled && !isUnlocked) {
                        PinLockScreen(
                            strings = strings,
                            onUnlockAttempt = { pin ->
                                viewModel.unlockApp(pin)
                            }
                        )
                    } else {
                        ZakarAppScaffold(
                            currentNav = currentNav,
                            strings = strings,
                            currentLanguage = language!!,
                            pastoralTitle = pastoralTitle,
                            allTasks = allTasks,
                            completedTasks = completedTasks,
                            alarmsList = alarmsList,
                            allNotes = allNotes,
                            chatMessages = chatMessages,
                            isThinking = isThinking,
                            lastConflictResult = lastConflictResult,
                            appLockEnabled = appLockEnabled,
                            appLockPin = currentPin,
                            vibrationEnabled = vibrationEnabled,
                            themeMode = themeMode,
                            currentRingtoneUri = alarmRingtoneUri,
                            currentRingtoneTitle = alarmRingtoneTitle,
                            onNavigate = { dest -> viewModel.navigateTo(dest) },
                            onToggleTaskComplete = { task -> viewModel.toggleTaskCompleted(task) },
                            onSaveTask = { task -> viewModel.saveTask(task) },
                            onDeleteTask = { task -> viewModel.deleteTask(task) },
                            onPostponeTask = { task, hours -> viewModel.postponeTask(task, hours) },
                            onCheckConflict = { time, excludeId -> viewModel.checkConflict(time, excludeId) },
                            onTriggerTestAlarm = { viewModel.triggerTestAlarm() },
                            onSaveNote = { note -> viewModel.saveNote(note) },
                            onDeleteNote = { note -> viewModel.deleteNote(note) },
                            onToggleFavoriteNote = { note -> viewModel.toggleNoteFavorite(note) },
                            onConvertNoteToTask = { note, time, hasAlarm ->
                                viewModel.convertNoteToTask(note, time, hasAlarm)
                            },
                            onSendAssistantMessage = { text ->
                                viewModel.sendAssistantMessage(text, language!!)
                            },
                            onConfirmAssistantTask = { msgId, task ->
                                viewModel.confirmAssistantTask(msgId, task)
                            },
                            onCancelAssistantProposal = { msgId ->
                                viewModel.cancelAssistantProposal(msgId)
                            },
                            onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                            onTitleChange = { title -> viewModel.setPastoralTitle(title) },
                            onAppLockChange = { enabled, pin -> viewModel.configureAppLock(enabled, pin) },
                            onVibrationChange = { enabled -> viewModel.setVibration(enabled) },
                            onThemeChange = { mode -> viewModel.setTheme(mode) },
                            onRingtoneChange = { uri, title -> viewModel.setAlarmRingtone(uri, title) },
                            onExportBackup = { callback -> viewModel.exportBackup(callback) },
                            onImportBackup = { json, callback -> viewModel.importBackup(json, callback) }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra("NAVIGATE_TO")
        if (target == "TASKS") {
            viewModel.navigateTo(NavDestination.TASKS)
        } else if (target == "ALARMS") {
            viewModel.navigateTo(NavDestination.ALARMS)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
