package com.example.nutrition.nutritionUI.goalsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.WorkoutEntry
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun OverviewContent(
    currentGoal: Int,
    steps: Int,
    burnedKcal: Int,
    latestWeight: Double?,
    previousWeight: Double?,
    selectedGoalOffset: Int?,
    ageInput: String,
    heightInput: String,
    isMale: Boolean?,
    selectedActivityLevel: Double?,
    targetWeightInput: String,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onMaleChange: (Boolean) -> Unit,
    onActivityChange: (Double) -> Unit,
    onGoalOffsetChange: (Int) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onOpenWeightDetail: () -> Unit,
    viewModel: FoodViewModel,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(20.dp)
            ) {
                Text(
                    "Dein Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusMiniCard("Ziel", "$currentGoal", "kcal", accentBlue)
                    StatusMiniCard("Aktiv", "+$burnedKcal", "kcal", Color(0xFF30D158))
                    StatusMiniCard("Schritte", "$steps", "heute", Color(0xFFFF9F0A))
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable { onOpenWeightDetail() }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.MonitorWeight, null, tint = accentBlue) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Körpergewicht",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Text("Historie", fontSize = 14.sp, color = grayText)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = grayText)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (latestWeight != null) "$latestWeight" else "--",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            " kg",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = grayText,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    if (latestWeight != null && previousWeight != null) {
                        val diff = latestWeight - previousWeight
                        if (abs(diff) >= 0.1) {
                            val isGoalLoss = (selectedGoalOffset ?: 0) < 0
                            val isGood = if (isGoalLoss) diff < 0 else diff > 0
                            val trendColor = if (isGood) Color(0xFF30D158) else Color(0xFFFF453A)
                            val prefix = if (diff > 0) "+" else ""

                            Text(
                                text = "$prefix${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        diff
                                    )
                                } kg",
                                color = trendColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }

                val heightM = (heightInput.toDoubleOrNull() ?: 0.0) / 100.0
                if (latestWeight != null && heightM > 0) {
                    val bmi = latestWeight / (heightM * heightM)
                    val df = DecimalFormat("0.0")
                    val bmiColor =
                        if (bmi in 18.5..24.9) Color(0xFF30D158) else if (bmi < 18.5) Color(
                            0xFF64D2FF
                        ) else Color(0xFFFF9F0A)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(bmiColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Aktueller BMI: ${df.format(bmi)}",
                            color = grayText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            var isCalculatorExpanded by remember { mutableStateOf(false) }
            val focusManager = LocalFocusManager.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCalculatorExpanded = !isCalculatorExpanded }
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Kalorienrechner",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Icon(
                        imageVector = if (isCalculatorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Aufklappen",
                        tint = grayText
                    )
                }

                AnimatedVisibility(visible = isCalculatorExpanded) {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 20.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectionButton(
                                "Männlich",
                                isMale == true,
                                { onMaleChange(true) },
                                Modifier.weight(1f),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Weiblich",
                                isMale == false,
                                { onMaleChange(false) },
                                Modifier.weight(1f),
                                accentBlue,
                                textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(
                                value = ageInput,
                                onValueChange = onAgeChange,
                                label = "Alter",
                                modifier = Modifier.weight(1f),
                                textColor = textColor,
                                grayText = grayText,
                                accentBlue = accentBlue,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                )
                            )
                            CustomTextField(
                                value = heightInput,
                                onValueChange = onHeightChange,
                                label = "Größe (cm)",
                                modifier = Modifier.weight(1f),
                                textColor = textColor,
                                grayText = grayText,
                                accentBlue = accentBlue,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                            OutlinedTextField(
                                value = if (latestWeight != null) "$latestWeight kg" else "-- kg",
                                onValueChange = {},
                                label = { Text("Gewicht", color = grayText, fontSize = 12.sp) },
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = textColor.copy(alpha = 0.6f),
                                    disabledBorderColor = grayText.copy(alpha = 0.3f),
                                    disabledLabelColor = grayText
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Alltag / Aktivität",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectionButton(
                                "Wenig Bewegung",
                                selectedActivityLevel == 1.2,
                                { onActivityChange(1.2) },
                                Modifier.fillMaxWidth(),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Leicht aktiv",
                                selectedActivityLevel == 1.375,
                                { onActivityChange(1.375) },
                                Modifier.fillMaxWidth(),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Mittel (3-5x Sport/Woche)",
                                selectedActivityLevel == 1.55,
                                { onActivityChange(1.55) },
                                Modifier.fillMaxWidth(),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Sehr aktiv",
                                selectedActivityLevel == 1.725,
                                { onActivityChange(1.725) },
                                Modifier.fillMaxWidth(),
                                accentBlue,
                                textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Dein Ziel",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectionButton(
                                "Abnehmen",
                                selectedGoalOffset == -500,
                                { onGoalOffsetChange(-500) },
                                Modifier.weight(1f),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Halten",
                                selectedGoalOffset == 0,
                                { onGoalOffsetChange(0) },
                                Modifier.weight(1f),
                                accentBlue,
                                textColor
                            )
                            SelectionButton(
                                "Zunehmen",
                                selectedGoalOffset == 300,
                                { onGoalOffsetChange(300) },
                                Modifier.weight(1f),
                                accentBlue,
                                textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onCalculate()
                                isCalculatorExpanded =
                                    false
                                focusManager.clearFocus()
                            },
                            enabled = isMale != null && selectedActivityLevel != null && selectedGoalOffset != null && ageInput.isNotBlank() && heightInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                        ) {
                            Text(
                                "Berechnen & speichern",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            val localWorkouts by viewModel.localWorkouts.collectAsState()
            var showWorkoutDialog by remember { mutableStateOf(false) }
            var workoutToEdit by remember { mutableStateOf<WorkoutEntry?>(null) }
            var workoutToDelete by remember { mutableStateOf<WorkoutEntry?>(null) }

            var isWorkoutsExpanded by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isWorkoutsExpanded = !isWorkoutsExpanded }
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Training",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isWorkoutsExpanded) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(accentBlue.copy(alpha = 0.15f))
                                    .clickable {
                                        workoutToEdit = null
                                        showWorkoutDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Hinzufügen",
                                    tint = accentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Icon(
                            imageVector = if (isWorkoutsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Aufklappen",
                            tint = grayText
                        )
                    }
                }

                AnimatedVisibility(visible = isWorkoutsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    ) {
                        if (localWorkouts.isEmpty()) {
                            Text(
                                "Kein Training für diesen Tag eingetragen.",
                                fontSize = 14.sp,
                                color = grayText
                            )
                        } else {
                            localWorkouts.forEach { workout ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            workoutToEdit = workout
                                            showWorkoutDialog = true
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            workout.name,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            "${workout.durationMinutes} Min",
                                            fontSize = 12.sp,
                                            color = grayText
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "+${workout.calories} kcal",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF30D158)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { workoutToDelete = workout }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Löschen",
                                                tint = grayText
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = grayText.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            if (showWorkoutDialog) {
                AddWorkoutDialog(
                    workoutToEdit = workoutToEdit,
                    onDismiss = { showWorkoutDialog = false },
                    onConfirm = { name, cal, dur ->
                        if (workoutToEdit != null) {
                            viewModel.updateManualWorkout(workoutToEdit!!, name, cal, dur)
                        } else {
                            viewModel.addManualWorkout(name, cal, dur)
                        }
                        showWorkoutDialog = false
                    },
                    cardColor = cardColor,
                    textColor = textColor,
                    accentBlue = accentBlue,
                    grayText = grayText
                )
            }

            if (workoutToDelete != null) {
                AlertDialog(
                    onDismissRequest = { workoutToDelete = null },
                    containerColor = cardColor,
                    title = {
                        Text(
                            "Eintrag löschen?",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    },
                    text = {
                        Text(
                            "Möchtest du '${workoutToDelete!!.name}' wirklich aus deinem Verlauf entfernen?",
                            color = grayText
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteWorkout(workoutToDelete!!)
                                workoutToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Löschen", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { workoutToDelete = null }) {
                            Text(
                                "Abbrechen",
                                color = grayText
                            )
                        }
                    }
                )
            }
        }

        item {
            var hasPermissions by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                hasPermissions = viewModel.healthConnectManager.hasAllPermissions()
            }

            val permissionLauncher =
                androidx.activity.compose.rememberLauncherForActivityResult<Set<String>, Set<String>>(
                    contract = viewModel.healthConnectManager.getPermissionContract()
                ) { grantedPermissions ->
                    coroutineScope.launch {
                        hasPermissions = viewModel.healthConnectManager.hasAllPermissions()
                        if (hasPermissions) {
                            viewModel.syncHealthData()
                        }
                    }
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable {
                        if (hasPermissions) {
                            val intent =
                                android.content.Intent("androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS")
                            intent.putExtra(
                                "android.intent.extra.PACKAGE_NAME",
                                context.packageName
                            )
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallbackIntent =
                                    android.content.Intent("android.health.connect.action.HEALTH_HOME_SETTINGS")
                                try {
                                    context.startActivity(fallbackIntent)
                                } catch (e2: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Einstellungen konnten nicht geöffnet werden.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            try {
                                if (viewModel.healthConnectManager.isAvailable) {
                                    permissionLauncher.launch(viewModel.healthConnectManager.permissions)
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Health Connect nicht verfügbar.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Fehler: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasPermissions) Icons.Default.CheckCircle else Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (hasPermissions) Color(0xFF30D158) else grayText,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Health Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Text(
                            if (hasPermissions) "Verbunden & aktiv" else "Nicht verbunden",
                            fontSize = 13.sp,
                            color = grayText
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = grayText
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}