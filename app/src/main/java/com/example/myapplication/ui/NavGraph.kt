package com.example.myapplication.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.MainActivity
import com.example.myapplication.ViewModelFactory
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.Alert
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel

@Composable
fun NavGraph(
    startDestination: String,
    factory: ViewModelProvider.Factory
) {
    val context = LocalContext.current.applicationContext
    val navFactory = factory
    val sosViewModel: SOSViewModel = viewModel(factory = navFactory)
    val alertViewModel: AlertViewModel = viewModel(factory = navFactory)
    val incidentViewModel: IncidentViewModel = viewModel(factory = navFactory)
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    // Navigate directly to MainActivity after onboarding
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
        composable("main") { MainScreen(sosViewModel, alertViewModel, onNavigateToTimeline = { navController.navigate("timeline") }) }
        composable("alert") { AlertScreen(Alert("", ""), AbilityType.NORMAL) }
        composable("guidance") { GuidanceScreen() }
        composable("sos") { SOSScreen() }
        composable("status") { StatusScreen() }
        composable("report_incident") { ReportIncidentScreen() }
        composable("timeline") { MyIncidentTimelineScreen(incidentViewModel) }
    }
}
