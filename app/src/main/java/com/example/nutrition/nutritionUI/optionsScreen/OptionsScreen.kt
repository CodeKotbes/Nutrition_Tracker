package com.example.nutrition.nutritionUI.optionsScreen

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nutrition.data.FullBackupManager
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID

enum class OptionsViewState { MAIN, GUIDE_AND_UPDATES }

data class ReminderItem(
    val id: String,
    val title: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sharedPrefs =
        remember { context.getSharedPreferences("NutritionAppPrefs", Context.MODE_PRIVATE) }
    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    var currentView by rememberSaveable { mutableStateOf(OptionsViewState.MAIN) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = currentView != OptionsViewState.MAIN) {
        currentView = OptionsViewState.MAIN
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val requestNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var autoBackupEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean(
                "auto_backup_enabled",
                false
            )
        )
    }
    var backupFrequency by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "auto_backup_frequency",
                "Täglich"
            ) ?: "Täglich"
        )
    }
    var folderUri by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "auto_backup_folder_uri",
                null
            )
        )
    }
    var backupHour by remember { mutableIntStateOf(sharedPrefs.getInt("auto_backup_hour", 2)) }
    var backupMinute by remember { mutableIntStateOf(sharedPrefs.getInt("auto_backup_minute", 0)) }
    var showBackupTimePicker by remember { mutableStateOf(false) }
    val backupTimePickerState = rememberTimePickerState(
        initialHour = backupHour,
        initialMinute = backupMinute,
        is24Hour = true
    )

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) FullBackupManager.restoreBackup(context, uri)
        }
    val folderPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                folderUri = it.toString()
                sharedPrefs.edit().putString("auto_backup_folder_uri", folderUri).apply()
                FullBackupManager.updateBackupSchedule(context)
            }
        }

    var waterReminderEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean(
                "water_reminder_enabled",
                false
            )
        )
    }
    var waterHour by remember { mutableIntStateOf(sharedPrefs.getInt("water_reminder_hour", 18)) }
    var waterMinute by remember {
        mutableIntStateOf(
            sharedPrefs.getInt(
                "water_reminder_minute",
                0
            )
        )
    }
    var waterText by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "water_reminder_text",
                "Hast du heute schon genug getrunken?"
            ) ?: ""
        )
    }
    var showWaterDialog by remember { mutableStateOf(false) }

    var calorieReminderEnabled by remember {
        mutableStateOf(
            sharedPrefs.getBoolean(
                "calorie_reminder_enabled",
                false
            )
        )
    }
    var calorieHour by remember {
        mutableIntStateOf(
            sharedPrefs.getInt(
                "calorie_reminder_hour",
                20
            )
        )
    }
    var calorieMinute by remember {
        mutableIntStateOf(
            sharedPrefs.getInt(
                "calorie_reminder_minute",
                0
            )
        )
    }
    var calorieText by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "calorie_reminder_text",
                "Trag noch deine restlichen Mahlzeiten ein!"
            ) ?: ""
        )
    }
    var showCalorieDialog by remember { mutableStateOf(false) }

    var customReminders by remember {
        val jsonStr = sharedPrefs.getString("custom_reminders", "[]") ?: "[]"
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<ReminderItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ReminderItem(
                    id = obj.getString("id"),
                    title = obj.getString("name"),
                    description = obj.optString("description", ""),
                    hour = obj.getInt("hour"),
                    minute = obj.getInt("minute"),
                    isEnabled = obj.getBoolean("isEnabled")
                )
            )
        }
        mutableStateOf<List<ReminderItem>>(list)
    }

    val saveCustomReminders: (List<ReminderItem>) -> Unit = { list ->
        customReminders = list
        val arr = JSONArray()
        list.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id); obj.put("name", r.title); obj.put("description", r.description)
            obj.put("hour", r.hour); obj.put("minute", r.minute); obj.put("isEnabled", r.isEnabled)
            arr.put(obj)

            if (r.isEnabled) {
                ReminderManager.scheduleReminder(
                    context,
                    r.id.hashCode(),
                    r.hour,
                    r.minute,
                    r.title,
                    r.description
                )
            } else {
                ReminderManager.cancelReminder(context, r.id.hashCode())
            }
        }
        sharedPrefs.edit().putString("custom_reminders", arr.toString()).apply()
    }

    var showCustomReminderDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<ReminderItem?>(null) }
    var reminderToDelete by remember { mutableStateOf<ReminderItem?>(null) }

    if (showBackupTimePicker) {
        AlertDialog(
            onDismissRequest = { showBackupTimePicker = false },
            title = {
                Text(
                    "Backup-Uhrzeit wählen",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { TimeInput(state = backupTimePickerState) }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backupHour = backupTimePickerState.hour; backupMinute =
                        backupTimePickerState.minute
                        sharedPrefs.edit().putInt("auto_backup_hour", backupHour)
                            .putInt("auto_backup_minute", backupMinute).apply()
                        FullBackupManager.updateBackupSchedule(context)
                        showBackupTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupTimePicker = false
                }) { Text("Abbrechen", color = grayText) }
            },
            containerColor = cardColor
        )
    }

    if (showWaterDialog) {
        val waterTimeState = rememberTimePickerState(
            initialHour = waterHour,
            initialMinute = waterMinute,
            is24Hour = true
        )
        var tempText by remember { mutableStateOf(waterText) }
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            title = {
                Text(
                    "Wasser-Check anpassen",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        label = { Text("Benachrichtigungstext", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        "Uhrzeit",
                        color = grayText,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    TimeInput(state = waterTimeState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        waterHour = waterTimeState.hour; waterMinute =
                        waterTimeState.minute; waterText = tempText
                        sharedPrefs.edit().putInt("water_reminder_hour", waterHour)
                            .putInt("water_reminder_minute", waterMinute)
                            .putString("water_reminder_text", waterText).apply()
                        if (waterReminderEnabled) ReminderManager.scheduleReminder(
                            context,
                            1001,
                            waterHour,
                            waterMinute,
                            "Wasser-Check",
                            waterText
                        )
                        showWaterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = { showWaterDialog = false }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            },
            containerColor = cardColor
        )
    }

    if (showCalorieDialog) {
        val calTimeState = rememberTimePickerState(
            initialHour = calorieHour,
            initialMinute = calorieMinute,
            is24Hour = true
        )
        var tempText by remember { mutableStateOf(calorieText) }
        AlertDialog(
            onDismissRequest = { showCalorieDialog = false },
            title = {
                Text(
                    "Kalorien-Check anpassen",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        label = { Text("Benachrichtigungstext", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        "Uhrzeit",
                        color = grayText,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    TimeInput(state = calTimeState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        calorieHour = calTimeState.hour; calorieMinute =
                        calTimeState.minute; calorieText = tempText
                        sharedPrefs.edit().putInt("calorie_reminder_hour", calorieHour)
                            .putInt("calorie_reminder_minute", calorieMinute)
                            .putString("calorie_reminder_text", calorieText).apply()
                        if (calorieReminderEnabled) ReminderManager.scheduleReminder(
                            context,
                            1002,
                            calorieHour,
                            calorieMinute,
                            "Kalorien-Check",
                            calorieText
                        )
                        showCalorieDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCalorieDialog = false
                }) { Text("Abbrechen", color = grayText) }
            },
            containerColor = cardColor
        )
    }

    if (showCustomReminderDialog) {
        var tempTitle by remember { mutableStateOf(reminderToEdit?.title ?: "") }
        var tempDesc by remember { mutableStateOf(reminderToEdit?.description ?: "") }
        val timeState = rememberTimePickerState(
            initialHour = reminderToEdit?.hour ?: 8,
            initialMinute = reminderToEdit?.minute ?: 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showCustomReminderDialog = false },
            title = {
                Text(
                    if (reminderToEdit == null) "Erinnerung erstellen" else "Erinnerung bearbeiten",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Titel", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempDesc,
                        onValueChange = { tempDesc = it },
                        label = { Text("Beschreibung (Optional)", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        "Uhrzeit",
                        color = grayText,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    TimeInput(state = timeState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempTitle.isNotBlank()) {
                            val newItem = ReminderItem(
                                id = reminderToEdit?.id ?: UUID.randomUUID().toString(),
                                title = tempTitle, description = tempDesc,
                                hour = timeState.hour, minute = timeState.minute,
                                isEnabled = reminderToEdit?.isEnabled ?: true
                            )
                            if (reminderToEdit == null) {
                                requestNotificationPermission()
                                saveCustomReminders(customReminders + newItem)
                            } else {
                                saveCustomReminders(customReminders.map { if (it.id == newItem.id) newItem else it })
                            }
                            showCustomReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomReminderDialog = false
                }) { Text("Abbrechen", color = grayText) }
            },
            containerColor = cardColor
        )
    }

    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null }, containerColor = cardColor,
            title = {
                Text(
                    "Erinnerung löschen?",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "Möchtest du die Erinnerung '${reminderToDelete!!.title}' wirklich unwiderruflich löschen?",
                    color = grayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        ReminderManager.cancelReminder(context, reminderToDelete!!.id.hashCode())
                        saveCustomReminders(customReminders.filter { it.id != reminderToDelete!!.id })
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Löschen", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentView == OptionsViewState.MAIN) "Optionen" else "Hilfe & Updates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    if (currentView != OptionsViewState.MAIN) {
                        IconButton(onClick = {
                            currentView = OptionsViewState.MAIN
                        }) { Icon(Icons.Default.ArrowBack, "Zurück", tint = accentBlue) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { paddingValues ->
        Crossfade(targetState = currentView, label = "OptionsTransition") { view ->
            when (view) {
                OptionsViewState.MAIN -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentView = OptionsViewState.GUIDE_AND_UPDATES },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = accentBlue
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        "Anleitung & Neuigkeiten",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Weiter",
                                    tint = grayText
                                )
                            }
                        }

                        Text(
                            "ALLGEMEIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = grayText,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(cardColor)
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Dark Mode",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                                Text(
                                    "App im dunklen Design anzeigen",
                                    fontSize = 13.sp,
                                    color = grayText
                                )
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentBlue
                                )
                            )
                        }

                        Text(
                            "ERINNERUNGEN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = grayText,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(cardColor)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showWaterDialog = true }) {
                                    Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF64D2FF))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Wasser-Check",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                        if (waterReminderEnabled) {
                                            Text(
                                                String.format(
                                                    Locale.getDefault(),
                                                    "Täglich um %02d:%02d Uhr",
                                                    waterHour,
                                                    waterMinute
                                                ),
                                                color = accentBlue,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Switch(
                                    checked = waterReminderEnabled,
                                    onCheckedChange = {
                                        waterReminderEnabled = it; sharedPrefs.edit()
                                        .putBoolean("water_reminder_enabled", it).apply()
                                        if (it) {
                                            requestNotificationPermission()
                                            ReminderManager.scheduleReminder(
                                                context,
                                                1001,
                                                waterHour,
                                                waterMinute,
                                                "Wasser-Check",
                                                waterText
                                            )
                                        } else {
                                            ReminderManager.cancelReminder(context, 1001)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accentBlue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = dividerColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showCalorieDialog = true }) {
                                    Icon(
                                        Icons.Default.RestaurantMenu,
                                        null,
                                        tint = Color(0xFFFF9F0A)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Kalorien-Check",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                        if (calorieReminderEnabled) {
                                            Text(
                                                String.format(
                                                    Locale.getDefault(),
                                                    "Täglich um %02d:%02d Uhr",
                                                    calorieHour,
                                                    calorieMinute
                                                ),
                                                color = accentBlue,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Switch(
                                    checked = calorieReminderEnabled,
                                    onCheckedChange = {
                                        calorieReminderEnabled = it; sharedPrefs.edit()
                                        .putBoolean("calorie_reminder_enabled", it).apply()
                                        if (it) {
                                            requestNotificationPermission()
                                            ReminderManager.scheduleReminder(
                                                context,
                                                1002,
                                                calorieHour,
                                                calorieMinute,
                                                "Kalorien-Check",
                                                calorieText
                                            )
                                        } else {
                                            ReminderManager.cancelReminder(context, 1002)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accentBlue
                                    )
                                )
                            }

                            if (customReminders.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = dividerColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "EIGENE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = grayText
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                customReminders.forEach { reminder ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    reminderToEdit =
                                                        reminder; showCustomReminderDialog = true
                                                }) {
                                            Text(
                                                reminder.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textColor
                                            )
                                            if (reminder.description.isNotBlank()) Text(
                                                reminder.description,
                                                fontSize = 12.sp,
                                                color = grayText,
                                                maxLines = 1
                                            )
                                            Text(
                                                String.format(
                                                    Locale.getDefault(),
                                                    "Täglich um %02d:%02d Uhr",
                                                    reminder.hour,
                                                    reminder.minute
                                                ),
                                                color = accentBlue,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = reminder.isEnabled,
                                                onCheckedChange = { state ->
                                                    saveCustomReminders(customReminders.map {
                                                        if (it.id == reminder.id) it.copy(
                                                            isEnabled = state
                                                        ) else it
                                                    })
                                                    if (state) requestNotificationPermission()
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = accentBlue
                                                )
                                            )
                                            IconButton(onClick = { reminderToDelete = reminder }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    "Löschen",
                                                    tint = grayText.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = {
                                reminderToEdit = null; showCustomReminderDialog = true
                            }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Add, null, tint = accentBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Erinnerung hinzufügen",
                                    color = accentBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            "DATENSICHERUNG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = grayText,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(cardColor)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudUpload, null, tint = accentBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Auto-Backup",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                }
                                Switch(
                                    checked = autoBackupEnabled,
                                    onCheckedChange = {
                                        autoBackupEnabled = it
                                        sharedPrefs.edit().putBoolean("auto_backup_enabled", it)
                                            .apply()
                                        FullBackupManager.updateBackupSchedule(context)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accentBlue
                                    )
                                )
                            }

                            if (autoBackupEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = dividerColor)
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Intervall",
                                    fontSize = 13.sp,
                                    color = grayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Täglich", "Wöchentlich").forEach { freq ->
                                        val isSelected = backupFrequency == freq
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) accentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) accentBlue else grayText.copy(
                                                        alpha = 0.3f
                                                    ),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    backupFrequency = freq; sharedPrefs.edit()
                                                    .putString("auto_backup_frequency", freq)
                                                    .apply()
                                                    FullBackupManager.updateBackupSchedule(context)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                freq,
                                                color = if (isSelected) accentBlue else textColor,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Uhrzeit",
                                    fontSize = 13.sp,
                                    color = grayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            grayText.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { showBackupTimePicker = true }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccessTime, null, tint = accentBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d Uhr",
                                            backupHour,
                                            backupMinute
                                        ), fontWeight = FontWeight.Bold, color = textColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Speicherort (Max. 5 Backups)",
                                    fontSize = 13.sp,
                                    color = grayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            grayText.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { folderPickerLauncher.launch(null) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, null, tint = accentBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        if (folderUri != null) "Ordner ausgewählt" else "Ordner wählen",
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { FullBackupManager.createAndShareBackup(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                        ) {
                            Text(
                                "Backup exportieren",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, accentBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentBlue)
                        ) {
                            Text(
                                "Backup importieren",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                OptionsViewState.GUIDE_AND_UPDATES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        val tabs = listOf("ANLEITUNG", "NEUIGKEITEN")
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = bgColor,
                            contentColor = accentBlue,
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedTab == 0) {
                                GuideSection(
                                    icon = Icons.Default.MenuBook,
                                    title = "1. Tagebuch & Tracking",
                                    cardColor = cardColor,
                                    accentBlue = accentBlue,
                                    textColor = textColor,
                                    grayText = grayText,
                                    text = "Verwalte deine tägliche Ernährung. Nutze den Barcode-Scanner für schnelles Hinzufügen von Produkten oder erstelle eigene Lebensmittel.\n\n" +
                                            "Deine Lebensmittel können mit einem Klick aus der Suche oder dem Verlauf übernommen werden. Passe die Grammzahl einer Zutat direkt im Tagebuch an, ohne in komplizierte Menüs zu wechseln, und exportiere einzelne Zutaten, um sie mit Freunden zu teilen."
                                )
                                GuideSection(
                                    icon = Icons.Default.RestaurantMenu,
                                    title = "2. Mahlzeiten",
                                    cardColor = cardColor,
                                    accentBlue = accentBlue,
                                    textColor = textColor,
                                    grayText = grayText,
                                    text = "Erstelle feste Mahlzeiten aus deinen Produkten, um sie später mit einem Klick in dein Tagebuch zu übernehmen.\n\n" +
                                            "Du hast eine Mahlzeit kreiert, die ein Freund unbedingt ausprobieren will? Nutze die integrierte Export-Funktion (Share-Button), um die Mahlzeit lokal als Datei abzuspeichern. Diese Datei kannst du ihm schicken – er kann sie über 'Importieren' in seiner App direkt abspeichern."
                                )
                                GuideSection(
                                    icon = Icons.Default.BarChart,
                                    title = "3. Analyse & Ziele",
                                    cardColor = cardColor,
                                    accentBlue = accentBlue,
                                    textColor = textColor,
                                    grayText = grayText,
                                    text = "Der Analyse-Bereich wertet deine Essgewohnheiten bis ins kleinste Detail aus. Du erhältst interaktive Graphen für Kalorien, Nährstoffverteilungen (Makros) und deinen Wasserhaushalt.\n\n" +
                                            "Im Tab 'Ziele' definierst du dein Profil (Abnehmen, Halten, Aufbau). Die App errechnet deinen TDEE und integriert automatisch verbrannte Kalorien aus 'Health Connect'. Wenn deine Smartwatch keine aktiven Kalorien sendet, nutzt die App einen intelligenten Fallback-Modus basierend auf deinen Schritten."
                                )
                                GuideSection(
                                    icon = Icons.Default.Settings,
                                    title = "4. Optionen",
                                    cardColor = cardColor,
                                    accentBlue = accentBlue,
                                    textColor = textColor,
                                    grayText = grayText,
                                    text = "Hier hast du die volle Kontrolle. Richte dir smarte Erinnerungen für Wasser, offene Kalorien oder deine eigenen Supplements ein. Du kannst nicht nur die genaue Uhrzeit einstellen, sondern auch den Benachrichtigungstext personalisieren!\n\n" +
                                            "Deine Daten sind zu 100% lokal. Nutze das automatische Backup-System, um deine Historie sicher in deinem Wunsch-Ordner auf dem Smartphone abzulegen."
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFFF453A
                                        ).copy(alpha = 0.15f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.BugReport,
                                            contentDescription = null,
                                            tint = Color(0xFFFF453A)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Fehler gefunden? Bitte melde Bugs oder Wünsche direkt an mich, damit ich sie so schnell wie möglich beheben kann!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }

                            } else {
                                UpdateNote(
                                    version = "Version 1.0",
                                    date = "24.05.2026",
                                    description = "Willkommen beim lokalen Nutrition Tracker!",
                                    features = listOf(
                                        "Datenbank: Lokales Speichern von Produkten, Mahlzeiten und Tagebuch-Einträgen.",
                                        "Scanner: Barcode-Scanner für sofortiges Finden von Nährwerten.",
                                        "Ziele: Dynamischer Rechner für Kalorien, Makros, BMI und TDEE.",
                                        "Health Connect: Synchronisiert Schritte & aktive Kalorien direkt aus Google Fit / Health Connect.",
                                        "Analyse: Detaillierte Wochen-Vergleiche, Makro-Splits und Trends.",
                                        "Backup: Tägliche oder Wöchentliche Datensicherung."
                                    ),
                                    textColor = textColor,
                                    grayText = grayText,
                                    accentBlue = accentBlue,
                                    cardColor = cardColor
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideSection(
    icon: ImageVector,
    title: String,
    text: String,
    cardColor: Color,
    accentBlue: Color,
    textColor: Color,
    grayText: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = text, fontSize = 14.sp, color = textColor, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun UpdateNote(
    version: String,
    date: String,
    description: String,
    features: List<String>,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    cardColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(version, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accentBlue)
            Text(date, fontSize = 12.sp, color = grayText, fontWeight = FontWeight.Bold)
        }
        if (description.isNotEmpty()) {
            Text(
                description,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                color = grayText
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { feature ->
                    Row {
                        Text("•", fontWeight = FontWeight.Bold, color = accentBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feature, fontSize = 14.sp, color = textColor)
                    }
                }
            }
        }
    }
}

object ReminderManager {
    fun scheduleReminder(
        context: Context,
        id: Int,
        hour: Int,
        minute: Int,
        title: String,
        text: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("text", text)
            putExtra("id", id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Erinnerung"
        val text = intent.getStringExtra("text") ?: ""
        val id = intent.getIntExtra("id", 0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "nutrition_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Erinnerungen", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.nutrition.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
        ReminderManager.scheduleReminder(context, id, hour, minute, title, text)
    }
}