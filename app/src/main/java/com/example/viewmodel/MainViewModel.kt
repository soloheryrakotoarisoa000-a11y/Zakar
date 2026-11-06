package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.assistant.MinistryNlpParser
import com.example.assistant.MissingField
import com.example.assistant.ParsedTaskIntent
import com.example.data.PreferencesManager
import com.example.data.model.ConflictResult
import com.example.data.model.CustomCategory
import com.example.data.model.MinistryNote
import com.example.data.model.MinistryTask
import com.example.data.repository.ZakarRepository
import com.example.localization.AppLanguage
import com.example.localization.TranslationStrings
import com.example.localization.Translations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class NavDestination {
    HOME,
    ASSISTANT,
    TASKS,
    AGENDA,
    NOTES,
    ALARMS,
    HISTORY,
    STATS,
    SETTINGS,
    ABOUT
}

data class AssistantChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val proposedTask: MinistryTask? = null,
    val isConfirmed: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ZakarRepository(application)
    private val preferencesManager = PreferencesManager(application)
    private val alarmScheduler = AlarmScheduler(application)

    // Navigation
    private val _currentNav = MutableStateFlow(NavDestination.HOME)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    // Preferences & Localization
    val languageFlow = preferencesManager.appLanguageFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null // null means show language selector first
    )

    val pastoralTitle = preferencesManager.pastoralTitleFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "Pasteur"
    )

    val appLockEnabled = preferencesManager.appLockEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    val appLockPin = preferencesManager.appLockPinFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ""
    )

    val vibrationEnabled = preferencesManager.vibrationEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    val themeMode = preferencesManager.themeModeFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "SYSTEM"
    )

    // App Lock State
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Tasks & Notes Data
    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeTasks = repository.activeTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val completedTasks = repository.completedTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val alarmsList = repository.alarmsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val favoriteNotes = repository.favoriteNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val customCategories = repository.customCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Assistant Chat
    private val _chatMessages = MutableStateFlow<List<AssistantChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AssistantChatMessage>> = _chatMessages.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    private var pendingIntentForFollowUp: ParsedTaskIntent? = null

    // Search & Filter
    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    private val _taskFilterCategory = MutableStateFlow("ALL")
    val taskFilterCategory: StateFlow<String> = _taskFilterCategory.asStateFlow()

    // Conflict Check
    private val _lastConflictResult = MutableStateFlow<ConflictResult?>(null)
    val lastConflictResult: StateFlow<ConflictResult?> = _lastConflictResult.asStateFlow()

    init {
        // Initialize Welcome in Assistant
        viewModelScope.launch {
            languageFlow.collect { lang ->
                if (lang != null && _chatMessages.value.isEmpty()) {
                    val strings = Translations.get(lang)
                    _chatMessages.value = listOf(
                        AssistantChatMessage(
                            text = strings.assistantWelcome,
                            isUser = false
                        )
                    )
                }
            }
        }
    }

    fun navigateTo(dest: NavDestination) {
        _currentNav.value = dest
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
            val strings = Translations.get(language)
            _chatMessages.value = listOf(
                AssistantChatMessage(
                    text = strings.assistantWelcome,
                    isUser = false
                )
            )
        }
    }

    fun setPastoralTitle(title: String) {
        viewModelScope.launch {
            preferencesManager.setPastoralTitle(title)
        }
    }

    fun unlockApp(pin: String): Boolean {
        return if (pin == appLockPin.value || appLockPin.value.isEmpty()) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun configureAppLock(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            preferencesManager.setAppLock(enabled, pin)
            _isUnlocked.value = !enabled
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVibration(enabled)
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    // Task Management
    fun saveTask(task: MinistryTask, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveTask(task)
            onSaved(id)
        }
    }

    fun updateTask(task: MinistryTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: MinistryTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompleted(task: MinistryTask) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun postponeTask(task: MinistryTask, hours: Int = 24) {
        viewModelScope.launch {
            repository.postponeTask(task, hours)
        }
    }

    fun checkConflict(timeMillis: Long, excludeTaskId: Long = 0L) {
        viewModelScope.launch {
            _lastConflictResult.value = repository.checkConflict(timeMillis, excludeTaskId)
        }
    }

    fun triggerTestAlarm() {
        alarmScheduler.triggerTestAlarm()
    }

    // Note Management
    fun saveNote(note: MinistryNote) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.saveNote(note)
            } else {
                repository.updateNote(note.copy(updatedAtMillis = System.currentTimeMillis()))
            }
        }
    }

    fun deleteNote(note: MinistryNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleNoteFavorite(note: MinistryNote) {
        viewModelScope.launch {
            repository.toggleNoteFavorite(note)
        }
    }

    fun convertNoteToTask(note: MinistryNote, dateTimeMillis: Long, hasAlarm: Boolean = true) {
        viewModelScope.launch {
            repository.convertNoteToTask(note, dateTimeMillis, hasAlarm)
        }
    }

    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun setTaskFilterCategory(category: String) {
        _taskFilterCategory.value = category
    }

    // Assistant Conversation & Natural Language Processing
    fun sendAssistantMessage(userText: String, currentLanguage: AppLanguage) {
        if (userText.isBlank()) return
        val strings = Translations.get(currentLanguage)

        // Append user message
        val userMsg = AssistantChatMessage(text = userText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAssistantThinking.value = true

        viewModelScope.launch {
            // Check if user is replying to a missing field
            val pending = pendingIntentForFollowUp
            val parsed = if (pending != null) {
                // Combine with follow up
                val combined = "${pending.title} $userText"
                val reParsed = MinistryNlpParser.parse(combined, currentLanguage)
                pendingIntentForFollowUp = null
                reParsed
            } else {
                MinistryNlpParser.parse(userText, currentLanguage)
            }

            if (parsed.missingField == MissingField.TIME) {
                pendingIntentForFollowUp = parsed
                _chatMessages.value = _chatMessages.value + AssistantChatMessage(
                    text = strings.assistantMissingTimePrompt,
                    isUser = false
                )
            } else if (parsed.missingField == MissingField.DATE) {
                pendingIntentForFollowUp = parsed
                _chatMessages.value = _chatMessages.value + AssistantChatMessage(
                    text = strings.assistantMissingDatePrompt,
                    isUser = false
                )
            } else {
                // We have title and target time
                val taskTime = parsed.dateTimeMillis ?: (System.currentTimeMillis() + 3600 * 1000L)
                val proposed = MinistryTask(
                    title = parsed.title,
                    categoryKey = parsed.categoryKey,
                    dateTimeMillis = taskTime,
                    hasAlarm = true,
                    repeatType = parsed.repeatType
                )

                _chatMessages.value = _chatMessages.value + AssistantChatMessage(
                    text = "${strings.assistantConfirmSchedule}\n\n• ${strings.taskTitle} : ${proposed.title}\n• ${strings.taskCategory} : ${getCategoryLocalizedName(proposed.categoryKey, strings)}\n• ${strings.taskRepeat} : ${proposed.repeatType}",
                    isUser = false,
                    proposedTask = proposed
                )
            }
            _isAssistantThinking.value = false
        }
    }

    fun confirmAssistantTask(messageId: String, task: MinistryTask) {
        viewModelScope.launch {
            saveTask(task)
            val currentLang = languageFlow.value ?: AppLanguage.FR
            val strings = Translations.get(currentLang)

            _chatMessages.value = _chatMessages.value.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(isConfirmed = true)
                } else msg
            } + AssistantChatMessage(
                text = "✓ ${strings.assistantTaskCreatedConfirmation}",
                isUser = false
            )
        }
    }

    fun cancelAssistantProposal(messageId: String) {
        _chatMessages.value = _chatMessages.value.filterNot { it.id == messageId }
    }

    // Export & Import
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataJson()
            onResult(json)
        }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importDataJson(json)
            onResult(success)
        }
    }

    companion object {
        fun getCategoryLocalizedName(categoryKey: String, strings: TranslationStrings): String {
            return when (categoryKey) {
                "catSermons", "sermons" -> strings.catSermons
                "catBiblePrep" -> strings.catBiblePrep
                "catBibleStudy" -> strings.catBibleStudy
                "catPrayers", "prayers" -> strings.catPrayers
                "catPastoralVisits", "visits" -> strings.catPastoralVisits
                "catSpiritualCounsel" -> strings.catSpiritualCounsel
                "catMeetings", "meetings" -> strings.catMeetings
                "catCeremonies" -> strings.catCeremonies
                "catWeddings", "weddings" -> strings.catWeddings
                "catBaptisms", "baptisms" -> strings.catBaptisms
                "catFunerals", "funerals" -> strings.catFunerals
                "catEvangelism", "evangelism" -> strings.catEvangelism
                "catTraining" -> strings.catTraining
                "catAdministration", "admin" -> strings.catAdministration
                else -> strings.catPersonal
            }
        }
    }
}
