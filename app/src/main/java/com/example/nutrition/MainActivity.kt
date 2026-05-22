package com.example.nutrition

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.nutrition.data.AppDatabase
import com.example.nutrition.data.FoodRepository
import com.example.nutrition.nutritionUI.analysisUI.AnalysisScreen
import com.example.nutrition.nutritionUI.foodUI.FoodScreen
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import com.example.nutrition.nutritionUI.goalsScreen.GoalsScreen
import com.example.nutrition.nutritionUI.goalsScreen.HealthConnectManager
import com.example.nutrition.nutritionUI.optionsScreen.OptionsScreen
import com.example.nutrition.nutritionUI.recipeScreen.RecipeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nutrition-db"
        ).fallbackToDestructiveMigration().build()

        val sharedPreferences = getSharedPreferences("NutritionAppPrefs", Context.MODE_PRIVATE)

        val healthConnectManager = HealthConnectManager(applicationContext)

        val repository = FoodRepository(
            database.foodItemDao(),
            database.diaryDao(),
            database.recipeDao(),
            database.waterDao(),
            database.weightDao(),
            database.workoutDao(),
            sharedPreferences
        )

        setContent {
            val viewModel: FoodViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FoodViewModel(repository, healthConnectManager) as T
                }
            })

            val isDark by viewModel.isDarkMode.collectAsState()
            val pagerState = rememberPagerState(pageCount = { 5 })
            val coroutineScope = rememberCoroutineScope()
            val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
            val navBarColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
            val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
            val unselectedColor = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
            ) {

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> FoodScreen(viewModel = viewModel)
                        1 -> RecipeScreen(viewModel = viewModel)
                        2 -> AnalysisScreen(viewModel = viewModel)
                        3 -> GoalsScreen(viewModel = viewModel)
                        4 -> OptionsScreen(viewModel = viewModel)
                    }
                }

                NavigationBar(containerColor = navBarColor, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Tagebuch") },
                        label = { Text("Tagebuch") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = accentBlue.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        icon = {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = "Mahlzeit"
                            )
                        },
                        label = { Text("Mahlzeit") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = accentBlue.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 2,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                        icon = { Icon(Icons.Default.BarChart, "Analyse") },
                        label = { Text("Analyse") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = accentBlue.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 3,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } },
                        icon = { Icon(Icons.Default.Flag, contentDescription = "Ziele") },
                        label = { Text("Ziele") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = accentBlue.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 4,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Optionen") },
                        label = { Text("Optionen") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = accentBlue.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    }
}