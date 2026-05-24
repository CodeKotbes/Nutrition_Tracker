package com.example.nutrition.nutritionUI.goalsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.WeightEntry
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import com.example.nutrition.nutritionUI.foodViewModel.ProjectionMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightDetailContent(
    sortedHistory: List<WeightEntry>, goalOffset: Int?, targetWeight: Double?,
    selectedStartMillis: Long?, onStartSelected: (Long) -> Unit,
    onGoalOffsetChange: (Int) -> Unit, onSetTarget: (String) -> Unit,
    viewModel: FoodViewModel, cardColor: Color, textColor: Color, grayText: Color, accentBlue: Color
) {
    var entryToEdit by remember { mutableStateOf<WeightEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<WeightEntry?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showFullscreenGraph by remember { mutableStateOf(false) }
    var showTrendline by remember { mutableStateOf(false) }
    var projectionMode by remember { mutableStateOf(ProjectionMode.CURRENT) }
    var projectionDays by remember { mutableStateOf(30) }
    var showScenarioInfo by remember { mutableStateOf(false) }
    val latestEntry = sortedHistory.lastOrNull()
    val latestWeight = latestEntry?.weight
    val previousWeight =
        if (sortedHistory.size > 1) sortedHistory[sortedHistory.size - 2].weight else null
    val availableEntries = sortedHistory.map { Pair(it.timestamp, it.weight) }
    val startEntry =
        if (selectedStartMillis != null) sortedHistory.find { it.timestamp == selectedStartMillis }
            ?: sortedHistory.firstOrNull() else sortedHistory.firstOrNull()
    val startWeight = startEntry?.weight

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NutritionProgressDashboardCard(
                title = "Dein Fortschritt",
                unit = "kg",
                startValue = startWeight,
                currentValue = latestWeight,
                previousValue = previousWeight,
                targetValue = targetWeight,
                goalOffset = goalOffset,
                onSetTargetClick = { showTargetDialog = true },
                onAddLogClick = {},
                availableEntries = availableEntries,
                onStartSelected = onStartSelected,
                cardColor = cardColor,
                textColor = textColor,
                accentBlue = accentBlue,
                grayText = grayText
            )
        }

        if (sortedHistory.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Gewichts-Trend",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                        IconButton(onClick = { showFullscreenGraph = true }) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Vollbild",
                                tint = accentBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    val projectedHistory =
                        if (showTrendline) viewModel.getProjectedWeightPath(
                            projectionMode,
                            projectionDays
                        ) else emptyList()

                    AdvancedWeightGraph(
                        historyPoints = sortedHistory,
                        projectedPoints = projectedHistory,
                        targetWeight = targetWeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        accentBlue = accentBlue,
                        textColor = textColor,
                        grayText = grayText,
                        cardColor = cardColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { showTrendline = !showTrendline },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (showTrendline) "Theoretischen Pfad ausblenden" else "Theoretischen Pfad anzeigen",
                            color = accentBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showTrendline) {
                        Spacer(modifier = Modifier.height(16.dp))

                        var modeDropdownExpanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Szenario:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                IconButton(
                                    onClick = { showScenarioInfo = true },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info zu Szenarien",
                                        tint = accentBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Box {
                                OutlinedButton(
                                    onClick = { modeDropdownExpanded = true },
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 0.dp
                                    )
                                ) {
                                    Text(projectionMode.label, color = textColor, fontSize = 12.sp)
                                    Icon(
                                        Icons.Default.ExpandMore,
                                        contentDescription = "Auswählen",
                                        tint = textColor,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(16.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = modeDropdownExpanded,
                                    onDismissRequest = { modeDropdownExpanded = false },
                                    containerColor = cardColor
                                ) {
                                    ProjectionMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.label, color = textColor) },
                                            onClick = {
                                                projectionMode = mode
                                                modeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Zeitraum:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(7 to "1W", 30 to "1M", 90 to "3M").forEach { (days, label) ->
                                    FilterChip(
                                        selected = projectionDays == days,
                                        onClick = { projectionDays = days },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = accentBlue,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Verlauf",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(sortedHistory.reversed()) { entry ->
            var showMenu by remember { mutableStateOf(false) }
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(sdf.format(Date(entry.timestamp)), color = grayText, fontSize = 12.sp)
                    Text(
                        "${entry.weight} kg",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Optionen", tint = grayText)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = cardColor
                    ) {
                        DropdownMenuItem(
                            text = { Text("Bearbeiten", color = textColor) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = accentBlue) },
                            onClick = { showMenu = false; entryToEdit = entry }
                        )
                        HorizontalDivider(color = grayText.copy(alpha = 0.2f))
                        DropdownMenuItem(
                            text = { Text("Löschen", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                            onClick = { showMenu = false; entryToDelete = entry }
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showScenarioInfo) {
        AlertDialog(
            onDismissRequest = { showScenarioInfo = false },
            containerColor = cardColor,
            title = {
                Text(
                    "Szenarien erklärt",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "• Aktuell: Berechnet den Trend basierend auf deiner heutigen Bilanz.\n\n" +
                            "• Dein Ziel: Zeigt den Verlauf, wenn du dich an den im Kalorienrechner eingestellten Wert hältst.\n\n" +
                            "• +/- Werte: Zeigen feste theoretische Pfade, unabhängig von deinem echten Verhalten.",
                    color = grayText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showScenarioInfo = false }) {
                    Text("Verstanden", color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (entryToEdit != null) {
        WeightLogDateDialog(
            entryToEdit!!.weight.toString(),
            entryToEdit!!.timestamp,
            "Eintrag bearbeiten",
            { entryToEdit = null },
            { w, d -> viewModel.updateWeightEntry(entryToEdit!!, w, d); entryToEdit = null },
            cardColor, textColor, accentBlue, grayText
        )
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null }, containerColor = cardColor,
            title = { Text("Eintrag löschen?", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Text(
                    "Möchtest du die ${entryToDelete!!.weight} kg wirklich unwiderruflich löschen?",
                    color = grayText
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteWeightEntry(entryToDelete!!); entryToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Löschen", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            }
        )
    }

    if (showTargetDialog) {
        var input by remember { mutableStateOf(targetWeight?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showTargetDialog = false }, containerColor = cardColor,
            title = { Text("Zielgewicht setzen", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                CustomTextField(
                    input,
                    { input = it },
                    "Ziel (kg)",
                    Modifier.fillMaxWidth(),
                    textColor,
                    grayText,
                    accentBlue,
                    KeyboardType.Decimal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        input.replace(",", ".").toDoubleOrNull()
                            ?.let { onSetTarget(it.toString()) }
                        showTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text("Speichern", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            }
        )
    }

    if (showFullscreenGraph) {
        val projectedHistory =
            if (showTrendline) viewModel.getProjectedWeightPath(
                projectionMode,
                projectionDays
            ) else emptyList()
        FullscreenWeightGraphDialog(
            historyPoints = sortedHistory,
            projectedPoints = projectedHistory,
            targetWeight = targetWeight,
            onClose = { showFullscreenGraph = false },
            cardColor = cardColor,
            textColor = textColor,
            grayText = grayText,
            accentBlue = accentBlue
        )
    }
}