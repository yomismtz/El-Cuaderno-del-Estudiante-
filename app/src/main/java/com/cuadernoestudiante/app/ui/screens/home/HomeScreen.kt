package com.cuadernoestudiante.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.core.model.*
import com.cuadernoestudiante.app.domain.GradeCalculator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
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
    modifier: Modifier = Modifier,
) {
    val evaluatedGrades = snapshot.subjects.map { it.currentGrade }
    val average = GradeCalculator.meanEvaluated(evaluatedGrades)
    val attendance = snapshot.subjects.map { it.attendancePercent }.average().toInt()
    val unreadNotices = snapshot.notices.count { !it.isRead }
    val upcoming = snapshot.assessments
        .filter { it.status == AssessmentStatus.PENDING }
        .sortedBy { it.dueAt }
    val nextActivity = upcoming.firstOrNull { it.type != AssessmentType.EXAM }
    val nextExam = upcoming.firstOrNull { it.type == AssessmentType.EXAM }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Header(
                studentName = snapshot.profile.name.substringBefore(" "),
                school = snapshot.profile.school,
                group = snapshot.profile.group,
            )
        }

        item {
            OverviewCard(
                average = average,
                attendance = attendance,
                unreadNotices = unreadNotices,
            )
        }

        item {
            QuickActions(
                actions = listOf(
                    QuickAction("Avisos", Icons.Rounded.Notifications, onNotices),
                    QuickAction("Pendientes", Icons.Rounded.Assignment, onPending),
                    QuickAction("Asistencia", Icons.Rounded.FactCheck, onAttendance),
                    QuickAction("Horario", Icons.Rounded.Schedule, onSchedule),
                )
            )
        }

        if (nextActivity != null || nextExam != null) {
            item { SectionTitle("Lo próximo") }
            nextActivity?.let { item { UpcomingAssessmentCard(it, snapshot.subjects) } }
            nextExam?.let { item { UpcomingAssessmentCard(it, snapshot.subjects) } }
        }

        item { SectionTitle("Pendientes próximos") }
        items(upcoming.take(4), key = { it.meta.localId }) { assessment ->
            PendingRow(assessment = assessment, subjects = snapshot.subjects)
        }

        item {
            DemoToolsCard(onResetDemo = onResetDemo)
        }
    }
}

@Composable
private fun Header(studentName: String, school: String, group: String) {
    Column {
        Text(
            text = "Hola, $studentName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "$school · $group",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun OverviewCard(average: Double?, attendance: Int, unreadNotices: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Resumen de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatItem("Promedio", average?.let { String.format(Locale.US, "%.1f", it) } ?: "—", Modifier.weight(1f))
                StatItem("Asistencia", "$attendance%", Modifier.weight(1f))
                StatItem("Avisos", unreadNotices.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActions(actions: List<QuickAction>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(actions) { action ->
            AssistChip(
                onClick = action.onClick,
                label = { Text(action.label) },
                leadingIcon = {
                    Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun UpcomingAssessmentCard(assessment: Assessment, subjects: List<Subject>) {
    val subject = subjects.firstOrNull { it.meta.localId == assessment.subjectLocalId }?.name ?: "Materia"
    val icon = if (assessment.type == AssessmentType.EXAM) Icons.Rounded.Quiz else Icons.Rounded.AssignmentTurnedIn
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(assessment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subject, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatDueDate(assessment.dueAt),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PendingRow(assessment: Assessment, subjects: List<Subject>) {
    val subject = subjects.firstOrNull { it.meta.localId == assessment.subjectLocalId }?.name ?: "Materia"
    Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = "Pendiente")
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(assessment.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                shortDueDate(assessment.dueAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DemoToolsCard(onResetDemo: () -> Unit) {
    OutlinedCard(shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Science, contentDescription = null)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Modo demo", fontWeight = FontWeight.SemiBold)
                Text(
                    "Datos ficticios locales. No se envía información a un servidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onResetDemo) { Text("Restablecer") }
        }
    }
}

private fun formatDueDate(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale("es", "MX"))
    return dateTime.format(formatter)
}

private fun shortDueDate(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    return dateTime.format(formatter)
}
