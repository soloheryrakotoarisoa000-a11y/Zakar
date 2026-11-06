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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.MinistryNote
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
fun NotesScreen(
    strings: TranslationStrings,
    notes: List<MinistryNote>,
    onSaveNote: (MinistryNote) -> Unit,
    onDeleteNote: (MinistryNote) -> Unit,
    onToggleFavorite: (MinistryNote) -> Unit,
    onConvertNoteToTask: (MinistryNote, Long, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var noteToEdit by remember { mutableStateOf<MinistryNote?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var noteToConvert by remember { mutableStateOf<MinistryNote?>(null) }

    val filteredNotes = notes.filter { note ->
        val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "ALL" || note.categoryKey == selectedCategory
        matchesSearch && matchesCat
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("notes_search_input"),
                placeholder = { Text(strings.noteSearchHint, style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = DivineGold)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSel = selectedCategory == "ALL"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) DivineGold else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = "ALL" }
                    ) {
                        Text(
                            text = strings.all,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) MidnightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                val categories = listOf("catSermons", "catPastoralVisits", "catPrayers", "catMeetings", "catBibleStudy", "catPersonal")
                items(categories) { catKey ->
                    val isSel = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) DivineGold else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = catKey }
                    ) {
                        Text(
                            text = MainViewModel.getCategoryLocalizedName(catKey, strings),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) MidnightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.emptyNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            strings = strings,
                            onEdit = {
                                noteToEdit = note
                                showEditDialog = true
                            },
                            onDelete = { onDeleteNote(note) },
                            onToggleFavorite = { onToggleFavorite(note) },
                            onConvertToTask = { noteToConvert = note }
                        )
                    }
                }
            }
        }

        // FAB to add note
        FloatingActionButton(
            onClick = {
                noteToEdit = null
                showEditDialog = true
            },
            containerColor = DivineGold,
            contentColor = MidnightBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_note")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = strings.addNote)
        }
    }

    if (showEditDialog) {
        NoteEditDialog(
            note = noteToEdit,
            strings = strings,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onSaveNote(updated)
                showEditDialog = false
            }
        )
    }

    if (noteToConvert != null) {
        ConvertToTaskDialog(
            note = noteToConvert!!,
            strings = strings,
            onDismiss = { noteToConvert = null },
            onConfirm = { timeMillis, hasAlarm ->
                onConvertNoteToTask(noteToConvert!!, timeMillis, hasAlarm)
                noteToConvert = null
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: MinistryNote,
    strings: TranslationStrings,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConvertToTask: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }

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
                Text(
                    text = MainViewModel.getCategoryLocalizedName(note.categoryKey, strings),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DivineGold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (note.isFavorite) DivineGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusOverdue.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(note.updatedAtMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Button to convert note to task
                Button(
                    onClick = onConvertToTask,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DivineGold.copy(alpha = 0.15f),
                        contentColor = DivineGold
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.convertNoteToTask, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NoteEditDialog(
    note: MinistryNote?,
    strings: TranslationStrings,
    onDismiss: () -> Unit,
    onSave: (MinistryNote) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var categoryKey by remember { mutableStateOf(note?.categoryKey ?: "catSermons") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (note == null) strings.addNote else strings.editNote,
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
                    label = { Text(strings.noteTitle) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(strings.noteContent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 8
                )

                Text(
                    text = "${strings.taskCategory} : ${MainViewModel.getCategoryLocalizedName(categoryKey, strings)}",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val cats = listOf("catSermons", "catPastoralVisits", "catPrayers", "catMeetings", "catBibleStudy", "catPersonal")
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
                        val updated = note?.copy(
                            title = title,
                            content = content,
                            categoryKey = categoryKey
                        ) ?: MinistryNote(
                            title = title,
                            content = content,
                            categoryKey = categoryKey
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

@Composable
private fun ConvertToTaskDialog(
    note: MinistryNote,
    strings: TranslationStrings,
    onDismiss: () -> Unit,
    onConfirm: (Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    var targetMillis by remember { mutableLongStateOf(System.currentTimeMillis() + (24 * 3600 * 1000L)) }
    val cal = remember(targetMillis) { Calendar.getInstance().apply { timeInMillis = targetMillis } }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.convertNoteToTask,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DivineGold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "« ${note.title} »",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

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
                                        timeInMillis = targetMillis
                                        set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    targetMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dateFormat.format(Date(targetMillis)), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = targetMillis
                                        set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, min)
                                    }
                                    targetMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(timeFormat.format(Date(targetMillis)), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(targetMillis, true) },
                colors = ButtonDefaults.buttonColors(containerColor = DivineGold, contentColor = MidnightBlue)
            ) {
                Text(strings.confirm, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
