package com.cuadernoestudiante.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cuadernoestudiante.app.ui.navigation.AppDestination
import com.cuadernoestudiante.app.ui.navigation.ExtraRoute
import com.cuadernoestudiante.app.ui.screens.common.PlaceholderScreen
import com.cuadernoestudiante.app.ui.screens.home.HomeScreen

@Composable
fun StudentApp(viewModel: StudentViewModel) {
    val navController = rememberNavController()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = AppDestination.bottomItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
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
                PlaceholderScreen("Materias", "La estructura y los datos demo ya están listos; esta será la siguiente pantalla.")
            }
            composable(AppDestination.Calendar.route) {
                PlaceholderScreen("Calendario", "Ruta preparada para Hoy, Esta semana y Mes.")
            }
            composable(AppDestination.Progress.route) {
                PlaceholderScreen("Mi progreso", "La lógica distingue Sin evaluar de una calificación real de cero.")
            }
            composable(AppDestination.Profile.route) {
                PlaceholderScreen("Perfil", "Perfil de solo lectura preparado para datos del estudiante autenticado.")
            }
            composable(ExtraRoute.Notices) {
                PlaceholderScreen("Avisos", "Bandeja académica de profesor y Dirección.")
            }
            composable(ExtraRoute.Pending) {
                PlaceholderScreen("Pendientes", "Lista de entregas, exámenes y proyectos próximos.")
            }
            composable(ExtraRoute.Attendance) {
                PlaceholderScreen("Asistencia", "Consulta de asistencia; el estudiante no puede modificar registros.")
            }
            composable(ExtraRoute.Schedule) {
                PlaceholderScreen("Horario", "Horario de solo lectura; en el futuro lo publicará Dirección.")
            }
        }
    }
}
