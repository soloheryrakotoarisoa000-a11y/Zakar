package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.RingtoneHelper
import com.example.alarm.RingtoneOption
import com.example.localization.TranslationStrings
import com.example.ui.theme.DivineGold
import com.example.ui.theme.MidnightBlue

@Composable
fun RingtonePickerDialog(
    strings: TranslationStrings,
    currentRingtoneUri: String,
    currentRingtoneTitle: String,
    onDismiss: () -> Unit,
    onSelectRingtone: (String, String) -> Unit
) {
    val context = LocalContext.current
    var ringtoneList by remember { mutableStateOf<List<RingtoneOption>>(emptyList()) }
    var selectedUri by remember { mutableStateOf(currentRingtoneUri) }
    var selectedTitle by remember { mutableStateOf(currentRingtoneTitle) }
    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    // Load device ringtones
    remember {
        ringtoneList = RingtoneHelper.getDeviceRingtones(context)
        // If currentRingtoneUri matches none in the list, get title
        if (selectedTitle.isEmpty()) {
            selectedTitle = RingtoneHelper.getRingtoneTitle(context, currentRingtoneUri)
        }
        true
    }

    DisposableEffect(Unit) {
        onDispose {
            RingtoneHelper.stopPreview()
        }
    }

    // System Ringtone Picker launcher
    val systemPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                val title = RingtoneHelper.getRingtoneTitle(context, uri.toString())
                selectedUri = uri.toString()
                selectedTitle = title
                onSelectRingtone(selectedUri, selectedTitle)
                onDismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            RingtoneHelper.stopPreview()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = DivineGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.alarmRingtoneTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DivineGold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ringtone_picker_dialog")
            ) {
                Text(
                    text = strings.alarmRingtoneDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Button to open Native System Ringtone Picker
                OutlinedButton(
                    onClick = {
                        RingtoneHelper.stopPreview()
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE or RingtoneManager.TYPE_NOTIFICATION
                            )
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, strings.alarmRingtoneTitle)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            if (selectedUri.isNotEmpty()) {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(selectedUri))
                            }
                        }
                        systemPickerLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_system_ringtone_picker"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DivineGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = strings.systemRingtonePicker, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sonneries détectées sur l'appareil (${ringtoneList.size}) :",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable List of Device Ringtones with Play / Stop buttons
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ringtoneList) { option ->
                        val isSelected = selectedUri == option.uriString
                        val isPlaying = currentlyPlayingUri == option.uriString

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected)
                                DivineGold.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected)
                                androidx.compose.foundation.BorderStroke(1.dp, DivineGold)
                            else
                                null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUri = option.uriString
                                    selectedTitle = option.title
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUri = option.uriString
                                        selectedTitle = option.title
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = DivineGold)
                                )

                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) DivineGold else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )

                                // Play / Stop preview button
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            RingtoneHelper.stopPreview()
                                            currentlyPlayingUri = null
                                        } else {
                                            RingtoneHelper.playPreview(context, option.uriString)
                                            currentlyPlayingUri = option.uriString
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPlaying) DivineGold else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) strings.stopPreview else strings.playPreview,
                                        tint = if (isPlaying) MidnightBlue else DivineGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    RingtoneHelper.stopPreview()
                    onSelectRingtone(selectedUri, selectedTitle)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DivineGold,
                    contentColor = MidnightBlue
                )
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                RingtoneHelper.stopPreview()
                onDismiss()
            }) {
                Text(strings.cancel)
            }
        }
    )
}
