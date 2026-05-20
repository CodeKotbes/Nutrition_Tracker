package com.example.nutrition.nutritionUI.analysisUI

import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.WaterRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2


fun getWeekDropdownLabel(offset: Int): String {
    val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
    val endCal = Calendar.getInstance(); endCal.add(Calendar.DAY_OF_YEAR, -(offset * 7))
    val startCal = Calendar.getInstance(); startCal.add(Calendar.DAY_OF_YEAR, -(offset * 7) - 6)
    val dateRange = "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"
    return when (offset) {
        0 -> "Diese ($dateRange)"; 1 -> "Letzte ($dateRange)"; else -> "Vor $offset W. ($dateRange)"
    }
}

fun getWeekShortLabel(offset: Int): String {
    val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
    val endCal = Calendar.getInstance(); endCal.add(Calendar.DAY_OF_YEAR, -(offset * 7))
    val startCal = Calendar.getInstance(); startCal.add(Calendar.DAY_OF_YEAR, -(offset * 7) - 6)
    return "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"
}

fun getEntriesForPastWeek(entries: List<DiaryEntry>, weekOffset: Int): List<DiaryEntry> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val resultDates = mutableListOf<String>()
    val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -(weekOffset * 7))
    for (i in 0..6) {
        resultDates.add(sdf.format(cal.time)); cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return entries.filter { it.date in resultDates }
}

fun filterEntriesByDays(entries: List<DiaryEntry>, days: Int): List<DiaryEntry> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
    val startDateStr = sdf.format(cal.time)
    return entries.filter { it.date >= startDateStr }
}

fun filterWaterRecordsByDays(records: List<WaterRecord>, days: Int): List<WaterRecord> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
    val startDateStr = sdf.format(cal.time)
    return records.filter { it.date >= startDateStr }
}

fun prepareDailyKcal(entries: List<DiaryEntry>, days: Int): List<Pair<String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelSdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val dateList = mutableListOf<String>()
    for (i in 0 until days) {
        dateList.add(sdf.format(cal.time)); cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    dateList.reverse()

    return dateList.map { dateStr ->
        val kcal = entries.filter { it.date == dateStr }.sumOf { it.calories }
        val label = if (days <= 7) SimpleDateFormat(
            "E",
            Locale.getDefault()
        ).format(sdf.parse(dateStr)!!) else labelSdf.format(sdf.parse(dateStr)!!)
        label to kcal
    }
}

fun prepareDailyWater(waterRecords: List<WaterRecord>, days: Int): List<Pair<String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelSdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val dateList = mutableListOf<String>()
    for (i in 0 until days) {
        dateList.add(sdf.format(cal.time)); cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    dateList.reverse()

    return dateList.map { dateStr ->
        val amount = waterRecords.filter { it.date == dateStr }.sumOf { it.amount }
        val label = if (days <= 7) SimpleDateFormat(
            "E",
            Locale.getDefault()
        ).format(sdf.parse(dateStr)!!) else labelSdf.format(sdf.parse(dateStr)!!)
        label to amount
    }
}

data class AggregatedFood(
    val name: String, val totalGrams: Double, val calories: Int,
    val protein: Double, val carbs: Double, val fat: Double,
    val fiber: Double, val sugar: Double
)

fun aggregateFood(entries: List<DiaryEntry>, sortByMacro: String): List<AggregatedFood> {
    val grouped = entries.groupBy { it.foodName }.map { (name, list) ->
        AggregatedFood(
            name, list.sumOf { it.amountInGrams }, list.sumOf { it.calories },
            list.sumOf { it.protein }, list.sumOf { it.carbs }, list.sumOf { it.fat },
            list.sumOf { it.fiber }, list.sumOf { it.sugar }
        )
    }
    return when (sortByMacro) {
        "Protein" -> grouped.sortedByDescending { it.protein }
        "Carbs" -> grouped.sortedByDescending { it.carbs }
        "Fett" -> grouped.sortedByDescending { it.fat }
        "Ballaststoffe" -> grouped.sortedByDescending { it.fiber }
        "Zucker" -> grouped.sortedByDescending { it.sugar }
        else -> grouped.sortedByDescending { it.calories }
    }
}