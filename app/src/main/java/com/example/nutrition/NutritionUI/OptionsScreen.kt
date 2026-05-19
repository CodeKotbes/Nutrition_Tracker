package com.example.nutrition.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.data.FullBackupManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("NutritionAppPrefs", Context.MODE_PRIVATE) }

    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    var autoBackupEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("auto_backup_enabled", false)) }
    var backupFrequency by remember { mutableStateOf(sharedPrefs.getString("auto_backup_frequency", "Täglich") ?: "Täglich") }
    var folderUri by remember { mutableStateOf(sharedPrefs.getString("auto_backup_folder_uri", null)) }
    var backupHour by remember { mutableIntStateOf(sharedPrefs.getInt("auto_backup_hour", 2)) }
    var backupMinute by remember { mutableIntStateOf(sharedPrefs.getInt("auto_backup_minute", 0)) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = backupHour, initialMinute = backupMinute, is24Hour = true)

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) FullBackupManager.restoreBackup(context, uri)
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            folderUri = it.toString()
            sharedPrefs.edit().putString("auto_backup_folder_uri", folderUri).apply()
            FullBackupManager.updateBackupSchedule(context)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Uhrzeit wählen", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backupHour = timePickerState.hour; backupMinute = timePickerState.minute
                        sharedPrefs.edit().putInt("auto_backup_hour", backupHour).putInt("auto_backup_minute", backupMinute).apply()
                        FullBackupManager.updateBackupSchedule(context)
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Abbrechen", color = grayText) } },
            containerColor = cardColor
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Optionen", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text("ALLGEMEIN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = grayText, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cardColor).padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark Mode", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textColor)
                    Text("Darkmode aktivieren", fontSize = 13.sp, color = grayText)
                }
                Switch(checked = isDark, onCheckedChange = { viewModel.toggleDarkMode() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentBlue))
            }

            Text("DATENSICHERUNG", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = grayText, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cardColor).padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, null, tint = accentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Auto-Backup", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textColor)
                    }
                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = {
                            autoBackupEnabled = it
                            sharedPrefs.edit().putBoolean("auto_backup_enabled", it).apply()
                            FullBackupManager.updateBackupSchedule(context)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentBlue)
                    )
                }

                if (autoBackupEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = grayText.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Intervall", fontSize = 13.sp, color = grayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Täglich", "Wöchentlich").forEach { freq ->
                            val isSelected = backupFrequency == freq
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) accentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) accentBlue else grayText.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).clickable {
                                        backupFrequency = freq; sharedPrefs.edit().putString("auto_backup_frequency", freq).apply()
                                        FullBackupManager.updateBackupSchedule(context)
                                    }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(freq, color = if (isSelected) accentBlue else textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Uhrzeit", fontSize = 13.sp, color = grayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, grayText.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { showTimePicker = true }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = accentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(String.format(Locale.getDefault(), "%02d:%02d Uhr", backupHour, backupMinute), fontWeight = FontWeight.Bold, color = textColor)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Speicherort (Max. 5 Backups)", fontSize = 13.sp, color = grayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, grayText.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { folderPickerLauncher.launch(null) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, null, tint = accentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (folderUri != null) "Ordner ausgewählt" else "Ordner wählen", fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
            }

            Button(
                onClick = { FullBackupManager.createAndShareBackup(context) },
                modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
            ) { Text("Backup manuell exportieren", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

            OutlinedButton(
                onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, accentBlue), colors = ButtonDefaults.outlinedButtonColors(contentColor = accentBlue)
            ) { Text("Backup importieren", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}