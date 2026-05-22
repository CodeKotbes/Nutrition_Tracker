package com.example.nutrition.nutritionUI.goalsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.WorkoutEntry

@Composable
fun SelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentBlue: Color,
    textColor: Color
) {
    val bgColor = if (isSelected) accentBlue.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) accentBlue else Color.Gray.copy(alpha = 0.3f)
    val fontColor = if (isSelected) accentBlue else textColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = fontColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = grayText, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor, unfocusedTextColor = textColor,
            focusedBorderColor = accentBlue, unfocusedBorderColor = grayText.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun StatusMiniCard(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(unit, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun AddWorkoutDialog(
    workoutToEdit: WorkoutEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, calories: Int, duration: Int) -> Unit,
    cardColor: Color,
    textColor: Color,
    accentBlue: Color,
    grayText: Color
) {
    var nameInput by remember { mutableStateOf(workoutToEdit?.name ?: "") }
    var caloriesInput by remember { mutableStateOf(workoutToEdit?.calories?.toString() ?: "") }
    var durationInput by remember {
        mutableStateOf(
            workoutToEdit?.durationMinutes?.toString() ?: ""
        )
    }
    val isEditMode = workoutToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                if (isEditMode) "Training bearbeiten" else "Training hinzufügen",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = "Training",
                    textColor = textColor,
                    grayText = grayText,
                    accentBlue = accentBlue,
                    keyboardType = KeyboardType.Text
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = durationInput,
                        onValueChange = { durationInput = it },
                        label = "Dauer (Min)",
                        modifier = Modifier.weight(1f),
                        textColor = textColor,
                        grayText = grayText,
                        accentBlue = accentBlue,
                        keyboardType = KeyboardType.Number
                    )
                    CustomTextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it },
                        label = "Kalorien",
                        modifier = Modifier.weight(1f),
                        textColor = textColor,
                        grayText = grayText,
                        accentBlue = accentBlue,
                        keyboardType = KeyboardType.Number
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = caloriesInput.toIntOrNull() ?: 0
                    val dur = durationInput.toIntOrNull() ?: 0
                    if (nameInput.isNotBlank() && cal > 0) {
                        onConfirm(nameInput, cal, dur)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen", color = grayText) }
        }
    )
}