package com.cuadernoestudiante.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cuadernoestudiante.app.ui.navigation.AppDestination
import com.cuadernoestudiante.app.ui.navigation.ExtraRoute
import com.cuadernoestudiante.app.ui.screens.common.PlaceholderScreen
import com.cuadernoestudiante.app.ui.screens.home.HomeScreen
import com.cuadernoestudiante.app.ui.screens.profile.StudentSettingsScreen
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle

@Composable
fun StudentApp(
    viewModel: StudentViewModel,
    currentTheme: AgendaThemeStyle,
    classCode: String,
    onThemeChange: (AgendaThemeStyle) -> Unit,
    onClassCodeChange: (String) -> Unit,
) {
    val navController = rememberNavController()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = AppDestination.bottomItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)) {
                    AppDestination.bottomItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AppDestination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    snapshot = snapshot,
                    onNotices = { navController.navigate(ExtraRoute.Notices) },
                    onPending = { navController.navigate(ExtraRoute.Pending) },
                    onAttendance = { navController.navigate(ExtraRoute.Attendance) },
                    onSchedule = { navController.navigate(ExtraRoute.Schedule) },
                    onResetDemo = viewModel::resetDemoData,
                )
            }
            composable(AppDestination.Subjects.route) {
                PlaceholderScreen("Materias", "Solo muestra las materias vinculadas al código de clase del estudiante.")
            }
            composable(AppDestination.Calendar.route) {
                PlaceholderScreen("Calendario", "Reúne actividades y eventos publicados por los docentes de las clases vinculadas.")
            }
            composable(AppDestination.Progress.route) {
                PlaceholderScreen("Mi progreso", "La lógica distingue Sin evaluar de una calificación real de cero.")
            }
            composable(AppDestination.Profile.route) {
                StudentSettingsScreen(
                    currentTheme = currentTheme,
                    classCode = classCode,
                    onThemeChange = onThemeChange,
                    onClassCodeChange = onClassCodeChange,
                )
            }
            composable(ExtraRoute.Notices) {
                PlaceholderScreen("Avisos", "Aquí aparecerán avisos de tus docentes. Los avisos internos de Dirección dirigidos solo a docentes no son visibles para alumnos.")
            }
            composable(ExtraRoute.Pending) {
                PlaceholderScreen("Pendientes", "Lista de entregas, exámenes y proyectos próximos de tus clases vinculadas.")
            }
            composable(ExtraRoute.Attendance) {
                PlaceholderScreen("Asistencia", "Consulta de asistencia; el estudiante no puede modificar registros.")
            }
            composable(ExtraRoute.Schedule) {
                PlaceholderScreen("Horario", "Horario de solo lectura de las clases vinculadas por código.")
            }
        }
    }
}
