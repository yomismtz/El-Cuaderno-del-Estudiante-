package com.cuadernoestudiante.app.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.network.ParticipationReportRequest
import com.cuadernoestudiante.app.network.StudentTeamActivityDto
import com.cuadernoestudiante.app.network.TeamMemberDto
import kotlinx.coroutines.launch

private data class TeamActivityChoice(
    val className: String,
    val activity: StudentTeamActivityDto,
)

@Composable
fun AnonymousTeamReportScreen(backend: CentralBackend) {
    val scope = rememberCoroutineScope()
    var meId by remember { mutableStateOf<Int?>(null) }
    var choices by remember { mutableStateOf<List<TeamActivityChoice>>(emptyList()) }
    var selectedActivityId by remember { mutableStateOf<Int?>(null) }
    var selectedTargetId by remember { mutableStateOf<Int?>(null) }
    var detail by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("did_not_work") }
    var activityMenu by remember { mutableStateOf(false) }
    var teammateMenu by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val selectedChoice = choices.firstOrNull { it.activity.id == selectedActivityId }
    val teammates = selectedChoice?.activity?.teamMembers.orEmpty().filter { it.id != meId }
    val selectedTeammate = teammates.firstOrNull { it.id == selectedTargetId }

    fun refresh() {
        scope.launch {
            loading = true
            runCatching {
                val me = backend.api.me()
                val classes = backend.api.classes()
                val loaded = buildList {
                    classes.forEach { classroom ->
                        backend.api.teamActivities(classroom.id)
                            .filter { it.team != null }
                            .forEach { add(TeamActivityChoice(classroom.name, it)) }
                    }
                }
                me.id to loaded
            }.onSuccess { (currentId, loaded) ->
                meId = currentId
                choices = loaded
                if (selectedActivityId !in loaded.map { it.activity.id }) {
                    selectedActivityId = loaded.firstOrNull { !it.activity.closed }?.activity?.id
                        ?: loaded.firstOrNull()?.activity?.id
                    selectedTargetId = null
                }
                message = if (loaded.isEmpty()) {
                    "No hay actividades de equipo publicadas para tus clases."
                } else {
                    null
                }
            }.onFailure {
                message = it.message ?: "No se pudieron cargar tus actividades de equipo."
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(selectedActivityId, meId) {
        if (selectedTargetId !in teammates.map(TeamMemberDto::id)) selectedTargetId = null
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Text("Participación de mi equipo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Solo puedes reportar a integrantes de tu propio equipo. Tu docente recibe el reporte sin tu identidad y debe corroborarlo antes de modificar una calificación.")
        }

        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Actividad publicada", fontWeight = FontWeight.Bold)
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { activityMenu = true },
                            enabled = choices.isNotEmpty() && !loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                selectedChoice?.let { "${it.className} · ${it.activity.name}" }
                                    ?: "Selecciona una actividad"
                            )
                        }
                        DropdownMenu(
                            expanded = activityMenu,
                            onDismissRequest = { activityMenu = false },
                        ) {
                            choices.forEach { choice ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${choice.className} · ${choice.activity.name}")
                                            Text(
                                                if (choice.activity.closed) "Coevaluación cerrada" else choice.activity.team?.name.orEmpty(),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedActivityId = choice.activity.id
                                        selectedTargetId = null
                                        activityMenu = false
                                        message = null
                                    },
                                )
                            }
                        }
                    }

                    if (selectedChoice?.activity?.closed == true) {
                        Text("Esta coevaluación ya fue cerrada por el docente y no acepta nuevos reportes.", color = MaterialTheme.colorScheme.error)
                    }

                    Text("Compañero de mi equipo", fontWeight = FontWeight.Bold)
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { teammateMenu = true },
                            enabled = teammates.isNotEmpty() && selectedChoice?.activity?.closed == false && !loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(selectedTeammate?.fullName ?: "Selecciona un compañero")
                        }
                        DropdownMenu(
                            expanded = teammateMenu,
                            onDismissRequest = { teammateMenu = false },
                        ) {
                            teammates.forEach { teammate ->
                                DropdownMenuItem(
                                    text = { Text(teammate.fullName.ifBlank { "Compañero ${teammate.id}" }) },
                                    onClick = {
                                        selectedTargetId = teammate.id
                                        teammateMenu = false
                                        message = null
                                    },
                                )
                            }
                        }
                    }
                    if (selectedChoice != null && teammates.isEmpty()) {
                        Text("No hay otro integrante de tu equipo disponible para reportar.", style = MaterialTheme.typography.bodySmall)
                    }

                    Text("Participación observada", fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = severity == "did_not_work",
                            onClick = { severity = "did_not_work" },
                            label = { Text("No trabajó") },
                        )
                        FilterChip(
                            selected = severity == "partial",
                            onClick = { severity = "partial" },
                            label = { Text("Participación parcial") },
                        )
                    }

                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it.take(500) },
                        label = { Text("Comentario opcional") },
                        supportingText = { Text("${detail.length}/500") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick = {
                            val activityId = selectedActivityId ?: return@Button
                            val targetId = selectedTargetId ?: return@Button
                            scope.launch {
                                loading = true
                                runCatching {
                                    backend.api.reportParticipation(
                                        activityId,
                                        ParticipationReportRequest(
                                            targetStudentId = targetId,
                                            severity = severity,
                                            comment = detail.trim(),
                                        ),
                                    )
                                }.onSuccess {
                                    detail = ""
                                    selectedTargetId = null
                                    message = "Reporte enviado de forma anónima al docente para revisión."
                                }.onFailure {
                                    message = it.message ?: "No se pudo enviar el reporte."
                                }
                                loading = false
                            }
                        },
                        enabled = selectedActivityId != null && selectedTargetId != null && selectedChoice?.activity?.closed == false && !loading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enviar reporte anónimo")
                    }
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        item {
            Text(
                "Privacidad y reglas: la app no permite elegir alumnos fuera de tu propio equipo ni reportarte a ti mismo. Un reporte no cambia notas automáticamente; el docente solo recibe conteos agregados y decide si lo confirma, lo descarta, lo considera parcial o lo marca sin evidencia suficiente.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
