package com.example.nutrition.nutritionUI.goalsScreen

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import androidx.health.connect.client.PermissionController

class HealthConnectManager(private val context: Context) {
    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        if (!isAvailable) return false
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun getPermissionContract() = PermissionController.createRequestPermissionResultContract()
    suspend fun getTodayHealthStats(): Pair<Int, Int> {
        if (!isAvailable || !hasAllPermissions()) return Pair(0, 0)

        val now = Instant.now()
        val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, now)

        var totalSteps = 0
        var totalKcal = 0

        try {
            val stepsRequest = AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = timeRangeFilter
            )
            val stepsResponse = healthConnectClient.aggregate(stepsRequest)
            totalSteps = (stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()

            val kcalRequest = AggregateRequest(
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = timeRangeFilter
            )
            val kcalResponse = healthConnectClient.aggregate(kcalRequest)
            totalKcal = (kcalResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0).toInt()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(totalSteps, totalKcal)
    }
}