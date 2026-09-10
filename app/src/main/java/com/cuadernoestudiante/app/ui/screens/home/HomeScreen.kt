package com.cuadernoestudiante.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.core.model.*
import com.cuadernoestudiante.app.domain.GradeCalculator
import com.cuadernoestudiante.app.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class QuickAction(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
)

@Composable
fun HomeScreen(
    snapshot: StudentDataSnapshot,
    onNotices: () -> Unit,
    onPending: () -> Unit,
    onAttendance: () -> Unit,
    onSchedule: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val average = GradeCalculator.meanEvaluated(snapshot.subjects.map { it.currentGrade })
    val attendance = snapshot.subjects.map { it.attendancePercent }.average().toInt()
    val unreadNotices = snapshot.notices.count { !it.isRead }
    val upcoming = snapshot.assessments
        .filter { it.status == AssessmentStatus.PENDING || it.status == AssessmentStatus.SUBMITTED }
        .sortedBy { it.dueAt }
    val nextActivity = upcoming.firstOrNull { it.type != AssessmentType.EXAM && it.status == AssessmentStatus.PENDING }
    val nextExam = upcoming.firstOrNull { it.type == AssessmentType.EXAM && it.status == AssessmentStatus.PENDING }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FamilyHeader(
                studentName = snapshot.profile.name.substringBefore(" "),
                school = snapshot.profile.school,
                group = snapshot.profile.group,
            )
        }

        item {
            TodaySummary(
                average = average,
                attendance = attendance,
                unreadNotices = unreadNotices,
                nextActivity = nextActivity,
                nextExam = nextExam,
            )
        }

        item {
            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            QuickActionGrid(
                listOf(
                    QuickAction("Avisos", "$unreadNotices nuevos", Icons.Rounded.Notifications, BlushCard, onNotices),
                    QuickAction("Pendientes", "${upcoming.count { it.status == AssessmentStatus.PENDING }} próximos", Icons.Rounded.Assignment, CreamCard, onPending),
                    QuickAction("Asistencia", "$attendance% actual", Icons.Rounded.FactCheck, LavenderCard, onAttendance),
                    QuickAction("Horario", "Consulta tus clases", Icons.Rounded.Schedule, SkyCard, onSchedule),
                )
            )
        }

        if (upcoming.isNotEmpty()) {
            item {
                Text(
                    text = "Tareas y pendientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Puedes marcar como hecha una tarea propia. Esto no modifica la calificación ni los registros del docente.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            items(upcoming.take(6), key = { it.meta.localId }) { assessment ->
                PendingRow(assessment = assessment, subjects = snapshot.subjects, onToggleTask = onToggleTask)
            }
        }

        item {
            DemoToolsCard(onResetDemo = onResetDemo)
        }

        item {
            Text(
                text = "Tu escuela en un solo lugar ♥",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FamilyHeader(
    studentName: String,
    school: String,
    group: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 350.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(color = MintCard, shape = RoundedCornerShape(20.dp)) {
                        Icon(Icons.Rounded.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp).size(36.dp))
                    }
                    Column {
                        Text("Hola, $studentName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("El Cuaderno del Estudiante", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Consulta · Organiza · Avanza", style = MaterialTheme.typography.bodySmall)
                        Text("$school · $group", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MintCard, shape = RoundedCornerShape(22.dp)) {
                        Icon(Icons.Rounded.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(14.dp).size(42.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hola, $studentName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("El Cuaderno del Estudiante", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Consulta · Organiza · Avanza", style = MaterialTheme.typography.bodySmall)
                        Text("$school · $group", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummary(
    average: Double?,
    attendance: Int,
    unreadNotices: Int,
    nextActivity: Assessment?,
    nextExam: Assessment?,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            BoxWithConstraints {
                if (maxWidth < 350.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactStat("Promedio actual", average?.let { String.format(Locale.US, "%.1f", it) } ?: "—")
                        CompactStat("Asistencia", "$attendance%")
                        CompactStat("Avisos nuevos", unreadNotices.toString())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("Promedio", average?.let { String.format(Locale.US, "%.1f", it) } ?: "—", Modifier.weight(1f))
                        MiniStat("Asistencia", "$attendance%", Modifier.weight(1f))
                        MiniStat("Avisos", unreadNotices.toString(), Modifier.weight(1f))
                    }
                }
            }
            HorizontalDivider()
            if (nextActivity == null && nextExam == null) {
                Text("No tienes actividades ni exámenes próximos.", style = MaterialTheme.typography.bodySmall)
            } else {
                nextActivity?.let { NextLine(Icons.Rounded.AssignmentTurnedIn, "Próxima actividad", "${it.title} · ${shortDueDate(it.dueAt)}") }
                nextExam?.let { NextLine(Icons.Rounded.Quiz, "Próximo examen", "${it.title} · ${shortDueDate(it.dueAt)}") }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)) {
        Column(Modifier.padding(10.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CompactStat(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NextLine(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuickActionGrid(actions: List<QuickAction>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth < 340.dp -> 1
            maxWidth < 760.dp -> 2
            else -> 4
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actions.chunked(columns).forEach { rowActions ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowActions.forEach { action ->
                        ElevatedCard(
                            onClick = action.onClick,
                            modifier = Modifier.weight(1f).heightIn(min = if (columns == 1) 104.dp else 126.dp),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().background(action.tint).padding(if (columns == 1) 14.dp else 16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Icon(action.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (columns >= 4) 30.dp else 34.dp))
                                Column {
                                    Text(action.label, fontWeight = FontWeight.Bold)
                                    Text(action.subtitle, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    repeat(columns - rowActions.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PendingRow(
    assessment: Assessment,
    subjects: List<Subject>,
    onToggleTask: (String, Boolean) -> Unit,
) {
    val subject = subjects.firstOrNull { it.meta.localId == assessment.subjectLocalId }?.name ?: "Materia"
    val completed = assessment.status == AssessmentStatus.SUBMITTED

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = if (assessment.type == AssessmentType.EXAM) BlushCard else MintCard) {
                Icon(
                    imageVector = if (assessment.type == AssessmentType.EXAM) Icons.Rounded.Quiz else Icons.Rounded.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp).size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(assessment.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (completed) "Marcada como hecha" else "Pendiente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(shortDueDate(assessment.dueAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (assessment.status != AssessmentStatus.GRADED) {
                    Checkbox(checked = completed, onCheckedChange = { onToggleTask(assessment.meta.localId, it) })
                }
            }
        }
    }
}

@Composable
private fun DemoToolsCard(onResetDemo: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Modo demo", fontWeight = FontWeight.SemiBold)
                Text("Datos ficticios guardados solo en esta versión de prueba.", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onResetDemo) { Text("Restablecer") }
        }
    }
}

private fun shortDueDate(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    return dateTime.format(formatter)
}
