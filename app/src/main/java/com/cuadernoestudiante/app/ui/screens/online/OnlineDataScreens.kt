package com.cuadernoestudiante.app.ui.screens.online

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.core.model.AssessmentStatus
import com.cuadernoestudiante.app.core.model.AttendanceStatus
import com.cuadernoestudiante.app.core.model.EvaluationStatus
import com.cuadernoestudiante.app.core.model.GradeValue
import com.cuadernoestudiante.app.core.model.StudentDataSnapshot
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
private fun ScreenList(title: String, subtitle: String, empty: String, count: Int, content: @Composable () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (count == 0) item { InfoCard(empty) } else item { content() }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(Modifier.fillMaxWidth()) { Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun DataCard(title: String, lines: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.filter { it.isNotBlank() }.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun StudentDataSnapshot.subjectName(id: String?): String =
    id?.let { target -> subjects.firstOrNull { it.meta.localId == target }?.name } ?: "General"

private fun gradeText(value: GradeValue): String = when (value.status) {
    EvaluationStatus.NOT_EVALUATED -> "Sin evaluar"
    EvaluationStatus.GRADED -> value.score?.let { "${"%.1f".format(it)} / 100" } ?: "Sin evaluar"
}

private fun attendanceText(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT -> "Presente"
    AttendanceStatus.ABSENT -> "Ausente"
    AttendanceStatus.LATE -> "Retardo"
    AttendanceStatus.EXCUSED -> "Justificada"
}

private fun assessmentStatusText(status: AssessmentStatus): String = when (status) {
    AssessmentStatus.PENDING -> "Pendiente"
    AssessmentStatus.SUBMITTED -> "Entregada"
    AssessmentStatus.GRADED -> "Calificada"
    AssessmentStatus.OVERDUE -> "Vencida"
}

@Composable
fun NoticesScreen(snapshot: StudentDataSnapshot) {
    ScreenList("Avisos", "Comunicados publicados para tus clases. Los mensajes internos de Dirección a docentes no se muestran aquí.", "No hay avisos publicados.", snapshot.notices.size) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            snapshot.notices.sortedByDescending { it.publishedAt }.forEach { notice ->
                DataCard(notice.title, listOf(snapshot.subjectName(notice.subjectLocalId), notice.body, "${notice.publisherName} · ${notice.publishedAt.format(dateTimeFormatter)}"))
            }
        }
    }
}

@Composable
fun PendingScreen(snapshot: StudentDataSnapshot) {
    val rows = snapshot.assessments.filter { it.status == AssessmentStatus.PENDING || it.status == AssessmentStatus.OVERDUE }.sortedBy { it.dueAt }
    ScreenList("Pendientes", "Actividades, exámenes y proyectos que requieren atención.", "No tienes pendientes.", rows.size) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { item ->
                DataCard(item.title, listOf(snapshot.subjectName(item.subjectLocalId), "${assessmentStatusText(item.status)} · vence ${item.dueAt.format(dateTimeFormatter)}"))
            }
        }
    }
}

@Composable
fun AttendanceScreen(snapshot: StudentDataSnapshot) {
    val rows = snapshot.attendance.sortedByDescending { it.date }
    ScreenList(
        "Asistencia",
        "Consulta de solo lectura. El porcentaje respeta las reglas de retardos y justificantes configuradas por tu docente.",
        "Todavía no hay registros de asistencia.",
        rows.size
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { item ->
                DataCard(
                    snapshot.subjectName(item.subjectLocalId),
                    listOf(item.date.format(dateFormatter), item.sessionTitle.orEmpty(), attendanceText(item.status))
                )
            }
        }
    }
}

@Composable
fun ScheduleScreen(snapshot: StudentDataSnapshot) {
    val dayNames = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val rows = snapshot.schedule.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
    ScreenList("Horario", "Horario institucional de tus clases vinculadas.", "No hay bloques de horario asignados.", rows.size) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { item ->
                val day = dayNames.getOrElse(item.dayOfWeek) { "Día ${item.dayOfWeek}" }
                DataCard(snapshot.subjectName(item.subjectLocalId), listOf("$day · ${item.startTime.format(timeFormatter)}–${item.endTime.format(timeFormatter)}", item.room?.let { "Aula: $it" }.orEmpty()))
            }
        }
    }
}

@Composable
fun CalendarScreen(snapshot: StudentDataSnapshot) {
    val rows = snapshot.calendarEvents.sortedBy { it.startAt }
    ScreenList("Calendario", "Eventos y fechas académicas reunidos desde tus clases.", "No hay eventos próximos.", rows.size) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { item ->
                DataCard(item.title, listOf(snapshot.subjectName(item.subjectLocalId), item.startAt.format(dateTimeFormatter), item.details.orEmpty()))
            }
        }
    }
}

@Composable
fun ProgressScreen(snapshot: StudentDataSnapshot) {
    ScreenList("Mi progreso", "Las calificaciones no evaluadas se mantienen separadas de una calificación real de cero.", "Todavía no hay componentes de progreso.", snapshot.progress.size) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            snapshot.progress.forEach { progress ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(snapshot.subjectName(progress.subjectLocalId), fontWeight = FontWeight.SemiBold)
                        progress.components.forEach { component ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${component.label} (${component.weightPercent}%)", modifier = Modifier.weight(1f))
                                Text(gradeText(component.value), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
