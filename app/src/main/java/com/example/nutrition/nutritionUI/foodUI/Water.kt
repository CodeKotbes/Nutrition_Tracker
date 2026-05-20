package com.example.nutrition.nutritionUI.foodUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrition.model.WaterRecord
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterHistorySheetContent(
    records: List<WaterRecord>,
    viewModel: FoodViewModel,
    bgColor: Color,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    waterBlue: Color,
    dividerColor: Color
) {
    var editingRecord by remember { mutableStateOf<WaterRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<WaterRecord?>(null) }
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Heutiger Wasserverlauf",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Noch kein Wasser getrunken.", color = grayText)
            }
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(records.sortedByDescending { it.timestamp }) { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardColor)
                            .clickable { editingRecord = record }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${record.amount} ml",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                            Text(
                                sdf.format(Date(record.timestamp)),
                                color = grayText,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { recordToDelete = record },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, "Löschen", tint = grayText.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }

    if (recordToDelete != null) {
        Dialog(onDismissRequest = { recordToDelete = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Eintrag entfernen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Möchtest du die ${recordToDelete?.amount} ml wirklich löschen?",
                    color = grayText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { recordToDelete = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            recordToDelete?.let { viewModel.deleteWaterRecord(it.id) }
                            recordToDelete = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Löschen", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (editingRecord != null) {
        Dialog(onDismissRequest = { editingRecord = null }) {
            var editAmount by remember { mutableStateOf(editingRecord!!.amount.toString()) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Wassermenge bearbeiten",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { editAmount = it },
                    label = { Text("Menge in ml", color = grayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = waterBlue
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { editingRecord = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            val amount = editAmount.toIntOrNull()
                            if (amount != null && amount > 0) {
                                viewModel.updateWaterRecord(editingRecord!!.id, amount)
                            }
                            editingRecord = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = waterBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun WaterTrackerSection(
    waterIntake: Int,
    waterGoal: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    waterBlue: Color,
    onAddWater: (Int) -> Unit,
    onCustomClick: () -> Unit,
    onCardClick: () -> Unit,
    onEditGoalClick: () -> Unit
) {
    val progress = if (waterGoal > 0) (waterIntake.toFloat() / waterGoal).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable { onCardClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalDrink, contentDescription = "Wasser", tint = waterBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wasser", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$waterIntake / $waterGoal ml",
                    color = grayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditGoalClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Ziel bearbeiten",
                        tint = grayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = waterBlue,
            trackColor = waterBlue.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAddWater(250) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = waterBlue.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("+ 250", color = waterBlue, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { onAddWater(500) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = waterBlue.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("+ 500", color = waterBlue, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { onCustomClick() },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = waterBlue.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("+ Eigene", color = waterBlue, fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tippe auf die Karte, um den Verlauf zu sehen",
            color = grayText.copy(alpha = 0.6f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}