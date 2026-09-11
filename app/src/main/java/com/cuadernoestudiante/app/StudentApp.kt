package com.cuadernoestudiante.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.ui.navigation.AppDestination
import com.cuadernoestudiante.app.ui.navigation.ExtraRoute
import com.cuadernoestudiante.app.ui.screens.home.HomeScreen
import com.cuadernoestudiante.app.ui.screens.notes.StudentNotesScreen
import com.cuadernoestudiante.app.ui.screens.online.AttendanceScreen
import com.cuadernoestudiante.app.ui.screens.online.CalendarScreen
import com.cuadernoestudiante.app.ui.screens.online.NoticesScreen
import com.cuadernoestudiante.app.ui.screens.online.PendingScreen
import com.cuadernoestudiante.app.ui.screens.online.ProgressScreen
import com.cuadernoestudiante.app.ui.screens.online.ScheduleScreen
import com.cuadernoestudiante.app.ui.screens.profile.StudentSettingsScreen
import com.cuadernoestudiante.app.ui.screens.subjects.StudentResourcesScreen
import com.cuadernoestudiante.app.ui.screens.team.AnonymousTeamReportScreen
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle

@Composable
fun StudentApp(
    viewModel: StudentViewModel,
    backend: CentralBackend,
    currentTheme: AgendaThemeStyle,
    classCode: String,
    onThemeChange: (AgendaThemeStyle) -> Unit,
    onClassCodeChange: (String) -> Unit,
) {
    val navController = rememberNavController()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showNavigation = AppDestination.bottomItems.any { it.route == currentRoute }

    BackHandler(enabled = currentRoute != null && currentRoute != AppDestination.Home.route) {
        if (!navController.popBackStack()) navController.navigate(AppDestination.Home.route) { launchSingleTop = true }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expandedNavigation = maxWidth >= 700.dp
        Row(Modifier.fillMaxSize()) {
            if (expandedNavigation && showNavigation) {
                NavigationRail {
                    Spacer(Modifier.height(8.dp))
                    AppDestination.bottomItems.forEach { destination ->
                        NavigationRailItem(selected = currentRoute == destination.route, onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Home.route) { saveState = true }; launchSingleTop = true; restoreState = true
                            }
                        }, icon = { Icon(destination.icon, contentDescription = destination.label) }, label = { Text(destination.label) })
                    }
                }
            }
            Scaffold(modifier = Modifier.weight(1f), containerColor = Color.Transparent, bottomBar = {
                if (!expandedNavigation && showNavigation) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)) {
                        AppDestination.bottomItems.forEach { destination ->
                            NavigationBarItem(selected = currentRoute == destination.route, onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AppDestination.Home.route) { saveState = true }; launchSingleTop = true; restoreState = true
                                }
                            }, icon = { Icon(destination.icon, contentDescription = destination.label) }, label = { Text(destination.label) })
                        }
                    }
                }
            }) { padding ->
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    NavHost(navController = navController, startDestination = AppDestination.Home.route, modifier = Modifier.fillMaxSize().widthIn(max = 1100.dp)) {
                        composable(AppDestination.Home.route) {
                            HomeScreen(snapshot = snapshot, onNotices = { navController.navigate(ExtraRoute.Notices) }, onPending = { navController.navigate(ExtraRoute.Pending) }, onAttendance = { navController.navigate(ExtraRoute.Attendance) }, onSchedule = { navController.navigate(ExtraRoute.Schedule) }, onResetDemo = viewModel::resetDemoData, onToggleTask = viewModel::markAssessmentCompleted)
                        }
                        composable(AppDestination.Subjects.route) {
                            StudentResourcesScreen(onNotes = { navController.navigate(ExtraRoute.Notes) }, onTeamReport = { navController.navigate(ExtraRoute.TeamReport) })
                        }
                        composable(AppDestination.Calendar.route) { CalendarScreen(snapshot) }
                        composable(AppDestination.Progress.route) { ProgressScreen(snapshot) }
                        composable(AppDestination.Profile.route) { StudentSettingsScreen(currentTheme = currentTheme, classCode = classCode, onThemeChange = onThemeChange, onClassCodeChange = onClassCodeChange) }
                        composable(ExtraRoute.Notices) { NoticesScreen(snapshot) }
                        composable(ExtraRoute.Pending) { PendingScreen(snapshot) }
                        composable(ExtraRoute.Attendance) { AttendanceScreen(snapshot) }
                        composable(ExtraRoute.Schedule) { ScheduleScreen(snapshot) }
                        composable(ExtraRoute.Notes) { StudentNotesScreen() }
                        composable(ExtraRoute.TeamReport) { AnonymousTeamReportScreen(backend = backend) }
                    }
                }
            }
        }
    }
}
