package com.cuadernoestudiante.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", "Inicio", Icons.Rounded.Home)
    data object Subjects : AppDestination("subjects", "Materias", Icons.Rounded.School)
    data object Calendar : AppDestination("calendar", "Calendario", Icons.Rounded.CalendarMonth)
    data object Progress : AppDestination("progress", "Progreso", Icons.Rounded.TrendingUp)
    data object Profile : AppDestination("profile", "Perfil", Icons.Rounded.Person)

    companion object { val bottomItems = listOf(Home, Subjects, Calendar, Progress, Profile) }
}

object ExtraRoute {
    const val Notices = "notices"
    const val Pending = "pending"
    const val Attendance = "attendance"
    const val Schedule = "schedule"
    const val Notes = "notes"
}
