package com.example.nutrition.nutritionUI.goalsScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nutrition.model.WeightEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdvancedWeightGraph(
    historyPoints: List<WeightEntry>,
    projectedPoints: List<WeightEntry>,
    targetWeight: Double?,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1f,
    accentBlue: Color,
    textColor: Color,
    grayText: Color,
    cardColor: Color
) {
    if (historyPoints.isEmpty()) return

    val uniqueHistory =
        historyPoints.groupBy { it.date }.map { it.value.last() }.sortedBy { it.timestamp }
    val allPoints = uniqueHistory + projectedPoints
    val minWeight = ((allPoints.minOfOrNull { it.weight } ?: 60.0).coerceAtMost(
        targetWeight ?: Double.MAX_VALUE
    )) - 1.0
    val maxWeight = ((allPoints.maxOfOrNull { it.weight } ?: 100.0).coerceAtLeast(
        targetWeight ?: Double.MIN_VALUE
    )) + 1.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)
    val firstTimestamp = uniqueHistory.first().timestamp
    val msPerDay = 24L * 60 * 60 * 1000
    val lastTimestamp =
        if (projectedPoints.isNotEmpty()) projectedPoints.last().timestamp else uniqueHistory.last().timestamp
    val totalDays = (((lastTimestamp - firstTimestamp) / msPerDay).toInt() + 1).coerceAtLeast(1)

    val dayWidthPx = 150f * zoomScale
    val density = LocalContext.current.resources.displayMetrics.density
    val totalWidthDp = ((totalDays * dayWidthPx) / density).dp

    var selectedPoint by remember { mutableStateOf<WeightEntry?>(null) }
    var tapOffset by remember { mutableStateOf<Offset?>(null) }

    Row(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .width(45.dp)
                .fillMaxHeight()
        ) {
            val height = size.height - 40.dp.toPx()
            val textPaintY = android.graphics.Paint().apply {
                color =
                    if (textColor == Color.White) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
                textSize = 10.sp.toPx()
                isAntiAlias = true
            }
            val stepY = range / 4
            for (i in 0..4) {
                val valueY = minWeight + (i * stepY)
                val y = height - (((valueY - minWeight) / range) * height).toFloat()
                drawContext.canvas.nativeCanvas.drawText(
                    String.format(Locale.US, "%.1f kg", valueY), 5f, y + 4f, textPaintY
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(rememberScrollState())
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
                    .pointerInput(allPoints) {
                        detectTapGestures { tap -> tapOffset = tap }
                    }
            ) {
                val height = size.height - 40.dp.toPx()
                val width = size.width
                val textPaintX = android.graphics.Paint().apply {
                    color =
                        if (textColor == Color.White) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val stepY = range / 4
                for (i in 0..4) {
                    val valueY = minWeight + (i * stepY)
                    val y = height - (((valueY - minWeight) / range) * height).toFloat()
                    drawLine(
                        color = grayText.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (targetWeight != null) {
                    val targetY = height - (((targetWeight - minWeight) / range) * height).toFloat()
                    drawLine(
                        color = Color(0xFF30D158),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                fun getXForTimestamp(ts: Long): Float {
                    val dayIndex = ((ts - firstTimestamp) / msPerDay).toFloat()
                    return dayIndex * dayWidthPx + (dayWidthPx / 2f)
                }

                fun getYForWeight(w: Double): Float {
                    return height - (((w - minWeight) / range) * height).toFloat()
                }

                val historyPath = Path()
                val historyPositions = mutableListOf<Offset>()
                val sdfX = SimpleDateFormat("dd.MM", Locale.getDefault())

                uniqueHistory.forEachIndexed { index, entry ->
                    val x = getXForTimestamp(entry.timestamp)
                    val y = getYForWeight(entry.weight)
                    historyPositions.add(Offset(x, y))

                    if (index == 0) historyPath.moveTo(x, y) else historyPath.lineTo(x, y)
                    drawContext.canvas.nativeCanvas.drawText(
                        sdfX.format(Date(entry.timestamp)), x, height + 20.dp.toPx(), textPaintX
                    )
                }

                if (uniqueHistory.size > 1) {
                    drawPath(
                        path = historyPath,
                        color = accentBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                historyPositions.forEach { offset ->
                    drawCircle(color = cardColor, radius = 5.dp.toPx(), center = offset)
                    drawCircle(color = accentBlue, radius = 3.dp.toPx(), center = offset)
                }

                if (projectedPoints.isNotEmpty() && uniqueHistory.isNotEmpty()) {
                    val projectedPath = Path()
                    val startX = getXForTimestamp(uniqueHistory.last().timestamp)
                    val startY = getYForWeight(uniqueHistory.last().weight)
                    projectedPath.moveTo(startX, startY)

                    projectedPoints.forEach { entry ->
                        val x = getXForTimestamp(entry.timestamp)
                        val y = getYForWeight(entry.weight)
                        projectedPath.lineTo(x, y)

                        drawCircle(color = cardColor, radius = 5.dp.toPx(), center = Offset(x, y))
                        drawCircle(
                            color = Color(0xFFFF9F0A),
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )

                        val dayIndex = ((entry.timestamp - firstTimestamp) / msPerDay).toInt()
                        if (dayIndex % 3 == 0 || entry == projectedPoints.last()) {
                            drawContext.canvas.nativeCanvas.drawText(
                                sdfX.format(Date(entry.timestamp)),
                                x,
                                height + 20.dp.toPx(),
                                textPaintX
                            )
                        }
                    }

                    drawPath(
                        path = projectedPath,
                        color = Color(0xFFFF9F0A),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                        )
                    )
                }

                if (tapOffset != null) {
                    val closestPoint = allPoints.minByOrNull { entry ->
                        val dx = getXForTimestamp(entry.timestamp) - tapOffset!!.x
                        val dy = getYForWeight(entry.weight) - tapOffset!!.y
                        dx * dx + dy * dy
                    }
                    if (closestPoint != null) {
                        val cx = getXForTimestamp(closestPoint.timestamp)
                        val cy = getYForWeight(closestPoint.weight)
                        val distance =
                            kotlin.math.sqrt((cx - tapOffset!!.x) * (cx - tapOffset!!.x) + (cy - tapOffset!!.y) * (cy - tapOffset!!.y))

                        if (distance < 40.dp.toPx()) {
                            selectedPoint = closestPoint
                            drawCircle(
                                color = (if (projectedPoints.contains(closestPoint)) Color(
                                    0xFFFF9F0A
                                ) else accentBlue).copy(alpha = 0.25f),
                                radius = 12.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        } else {
                            selectedPoint = null
                        }
                    }
                }
            }

            if (selectedPoint != null) {
                val sdfTooltip = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val isProjected = projectedPoints.contains(selectedPoint!!)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(textColor.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${selectedPoint!!.weight} kg (${sdfTooltip.format(Date(selectedPoint!!.timestamp))})${if (isProjected) "" else ""}",
                        color = cardColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogDateDialog(
    initialWeight: String,
    initialDate: Long,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long) -> Unit,
    cardColor: Color,
    textColor: Color,
    accentBlue: Color,
    grayText: Color
) {
    var input by remember { mutableStateOf(initialWeight) }
    var selectedDateMillis by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }; showDatePicker = false
                }) { Text("OK", color = accentBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = cardColor)
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardColor,
        title = { Text(title, color = textColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Gewicht in kg", color = grayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentBlue
                    )
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = grayText
                    ); Spacer(modifier = Modifier.width(8.dp))
                    Text("Datum: ${sdf.format(Date(selectedDateMillis))}", color = textColor)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    input.replace(",", ".").toDoubleOrNull()
                        ?.let { onConfirm(it, selectedDateMillis) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = grayText) } }
    )
}

@Composable
fun FullscreenWeightGraphDialog(
    historyPoints: List<WeightEntry>,
    projectedPoints: List<WeightEntry>,
    targetWeight: Double?,
    onClose: () -> Unit,
    cardColor: Color, textColor: Color, grayText: Color, accentBlue: Color
) {
    var zoomScale by remember { mutableStateOf(1f) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = cardColor) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gewichtsverlauf",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Schließen", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(grayText.copy(alpha = 0.05f))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.2f, 4f)
                            }
                        }
                ) {
                    AdvancedWeightGraph(
                        historyPoints = historyPoints,
                        projectedPoints = projectedPoints,
                        targetWeight = targetWeight,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        zoomScale = zoomScale,
                        accentBlue = accentBlue,
                        textColor = textColor,
                        grayText = grayText,
                        cardColor = cardColor
                    )
                }
            }
        }
    }
}