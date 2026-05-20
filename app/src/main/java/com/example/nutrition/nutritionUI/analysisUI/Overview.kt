package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverviewContent(
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    waterBlue: Color,
    onOpenTrend: () -> Unit,
    onOpenMacros: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenWater: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverviewCard(
            "Kalorien-Trend",
            "Verlauf & Historie",
            Icons.Default.BarChart,
            accentBlue,
            cardColor,
            textColor,
            grayText,
            onOpenTrend
        )
        OverviewCard(
            "Wasser-Trend",
            "Getrunkene Menge über Zeit",
            Icons.Default.LocalDrink,
            waterBlue,
            cardColor,
            textColor,
            grayText,
            onOpenWater
        )
        OverviewCard(
            "Nährstoff-Verteilung",
            "Top-Quellen",
            Icons.Default.PieChart,
            Color(0xFFFF9F0A),
            cardColor,
            textColor,
            grayText,
            onOpenMacros
        )
        OverviewCard(
            "Wochen-Vergleich",
            "Detaillierter Bilanz-Abgleich",
            Icons.Default.CompareArrows,
            Color(0xFF30D158),
            cardColor,
            textColor,
            grayText,
            onOpenCompare
        )
    }
}

@Composable
fun OverviewCard(
    title: String, subTitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color, cardColor: Color, textColor: Color, grayText: Color, onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconColor) }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                Text(subTitle, fontSize = 14.sp, color = grayText)
            }
        }
    }
}