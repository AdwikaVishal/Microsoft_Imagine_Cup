package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel

@Composable
fun MainAppNavGraph(
    sosViewModel: SOSViewModel,
    alertViewModel: AlertViewModel,
    incidentViewModel: IncidentViewModel,
    accessibilityManager: AccessibilityManager? = null
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                sosViewModel = sosViewModel,
                alertViewModel = alertViewModel,
                onNavigateToTimeline = { navController.navigate("timeline") },
                accessibilityManager = accessibilityManager
            )
        }
        composable("timeline") {
            MyIncidentTimelineScreen(viewModel = incidentViewModel)
        }
    }
}
