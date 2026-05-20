package com.example.nutrition.nutritionUI.goalsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: FoodViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentGoal by viewModel.goalKcal.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    var isMale by rememberSaveable { mutableStateOf(true) }
    var ageInput by rememberSaveable { mutableStateOf("25") }
    var heightInput by rememberSaveable { mutableStateOf("180") }
    var weightInput by rememberSaveable { mutableStateOf("80") }
    var activityLevel by rememberSaveable { mutableStateOf(1.375) }
    var goalOffset by rememberSaveable { mutableStateOf(-500) }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meine Ziele",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = textColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        "Dein Kalorienziel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        "Aktuell: $currentGoal kcal",
                        fontSize = 14.sp,
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectionButton(
                            text = "Männlich",
                            isSelected = isMale,
                            onClick = { isMale = true },
                            modifier = Modifier.weight(1f),
                            accentBlue = accentBlue,
                            textColor = textColor
                        )
                        SelectionButton(
                            text = "Weiblich",
                            isSelected = !isMale,
                            onClick = { isMale = false },
                            modifier = Modifier.weight(1f),
                            accentBlue = accentBlue,
                            textColor = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomTextField(
                            value = ageInput,
                            onValueChange = { ageInput = it },
                            label = "Alter",
                            modifier = Modifier.weight(1f),
                            textColor,
                            grayText,
                            accentBlue
                        )
                        CustomTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = "Größe (cm)",
                            modifier = Modifier.weight(1f),
                            textColor,
                            grayText,
                            accentBlue
                        )
                        CustomTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = "Gewicht (kg)",
                            modifier = Modifier.weight(1f),
                            textColor,
                            grayText,
                            accentBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Alltag / Aktivität",
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectionButton(
                            "Büro / Wenig Bewegung",
                            activityLevel == 1.2,
                            { activityLevel = 1.2 },
                            Modifier.fillMaxWidth(),
                            accentBlue,
                            textColor
                        )
                        SelectionButton(
                            "Leicht aktiv (Viel auf den Beinen)",
                            activityLevel == 1.375,
                            { activityLevel = 1.375 },
                            Modifier.fillMaxWidth(),
                            accentBlue,
                            textColor
                        )
                        SelectionButton(
                            "Mittel (3-5x Sport/Woche)",
                            activityLevel == 1.55,
                            { activityLevel = 1.55 },
                            Modifier.fillMaxWidth(),
                            accentBlue,
                            textColor
                        )
                        SelectionButton(
                            "Sehr aktiv (Schwerstarbeit/Sportler)",
                            activityLevel == 1.725,
                            { activityLevel = 1.725 },
                            Modifier.fillMaxWidth(),
                            accentBlue,
                            textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Dein Ziel",
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectionButton(
                            "Abnehmen",
                            goalOffset == -500,
                            { goalOffset = -500 },
                            Modifier.weight(1f),
                            accentBlue,
                            textColor
                        )
                        SelectionButton(
                            "Halten",
                            goalOffset == 0,
                            { goalOffset = 0 },
                            Modifier.weight(1f),
                            accentBlue,
                            textColor
                        )
                        SelectionButton(
                            "Aufbauen",
                            goalOffset == 300,
                            { goalOffset = 300 },
                            Modifier.weight(1f),
                            accentBlue,
                            textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val weight = weightInput.toDoubleOrNull() ?: 80.0
                            val height = heightInput.toDoubleOrNull() ?: 180.0
                            val age = ageInput.toIntOrNull() ?: 25
                            viewModel.calculateAndSetGoal(
                                isMale,
                                weight,
                                height,
                                age,
                                activityLevel,
                                goalOffset
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                    ) {
                        Text(
                            "Berechnen & als Ziel setzen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}