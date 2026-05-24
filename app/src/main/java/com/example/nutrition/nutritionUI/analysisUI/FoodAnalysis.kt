package com.example.nutrition.nutritionUI.analysisUI

import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.WaterRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun generateDateList(
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?
): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateList = mutableListOf<String>()
    val cal = Calendar.getInstance()

    if (timeSpan == -1 && customStartMillis != null && customEndMillis != null) {
        cal.timeInMillis = customStartMillis
        val endCal = Calendar.getInstance().apply { timeInMillis = customEndMillis }
        while (!cal.after(endCal)) {
            dateList.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    } else if (timeSpan == 7) {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -offset)

        for (i in 0..6) {
            dateList.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    } else {
        for (i in 0 until timeSpan) {
            dateList.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        dateList.reverse()
    }
    return dateList
}

fun getFilteredEntries(
    entries: List<DiaryEntry>,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?
): List<DiaryEntry> {
    val dateSet = generateDateList(timeSpan, customStartMillis, customEndMillis).toSet()
    return entries.filter { it.date in dateSet }
}

fun getFilteredWater(
    records: List<WaterRecord>,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?
): List<WaterRecord> {
    val dateSet = generateDateList(timeSpan, customStartMillis, customEndMillis).toSet()
    return records.filter { it.date in dateSet }
}

fun prepareDailyKcal(
    entries: List<DiaryEntry>,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?
): List<Triple<String, String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelSdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val dateList = generateDateList(timeSpan, customStartMillis, customEndMillis)

    return dateList.map { dateStr ->
        val kcal = entries.filter { it.date == dateStr }.sumOf { it.calories }
        val label = if (dateList.size <= 7) SimpleDateFormat(
            "E",
            Locale.GERMAN
        ).format(sdf.parse(dateStr)!!)
        else labelSdf.format(sdf.parse(dateStr)!!)
        Triple(dateStr, label, kcal)
    }
}

fun prepareDailyWater(
    waterRecords: List<WaterRecord>,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?
): List<Triple<String, String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelSdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val dateList = generateDateList(timeSpan, customStartMillis, customEndMillis)

    return dateList.map { dateStr ->
        val amount = waterRecords.filter { it.date == dateStr }.sumOf { it.amount }
        val label = if (dateList.size <= 7) SimpleDateFormat(
            "E",
            Locale.GERMAN
        ).format(sdf.parse(dateStr)!!)
        else labelSdf.format(sdf.parse(dateStr)!!)
        Triple(dateStr, label, amount)
    }
}

fun getWeekRange(dateMillis: Long): Pair<Calendar, Calendar> {
    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -offset)

    val start = cal.clone() as Calendar
    val end = cal.clone() as Calendar
    end.add(Calendar.DAY_OF_YEAR, 6)

    return Pair(start, end)
}

fun getWeekLabel(dateMillis: Long): String {
    val (start, end) = getWeekRange(dateMillis)
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    return "${sdf.format(start.time)} - ${sdf.format(end.time)}"
}

fun getEntriesForWeekOfDate(entries: List<DiaryEntry>, dateMillis: Long): List<DiaryEntry> {
    val (start, _) = getWeekRange(dateMillis)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val resultDates = mutableListOf<String>()

    val current = start.clone() as Calendar
    for (i in 0..6) {
        resultDates.add(sdf.format(current.time))
        current.add(Calendar.DAY_OF_YEAR, 1)
    }
    return entries.filter { it.date in resultDates }
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