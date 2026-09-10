package com.cuadernoestudiante.app.ui.screens.team

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun AnonymousTeamReportScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("anonymous_team_reports", Context.MODE_PRIVATE) }
    var activity by remember { mutableStateOf("") }
    var teammate by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var participation by remember { mutableStateOf("No trabajó") }
    var message by remember { mutableStateOf<String?>(null) }

    fun saveReport() {
        if (activity.isBlank() || teammate.isBlank()) return
        val raw = prefs.getString("pending", "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        array.put(JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("activity", activity.trim())
            .put("teammate", teammate.trim())
            .put("participation", participation)
            .put("detail", detail.trim())
            .put("createdAt", System.currentTimeMillis()))
        prefs.edit().putString("pending", array.toString()).apply()
        activity = ""; teammate = ""; detail = ""
        message = "Reporte guardado de forma anónima y pendiente de sincronización con tu docente."
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Participación de mi equipo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Usa esta opción solo para actividades en equipo. Tu docente verá el reporte sin saber quién lo envió y deberá corroborarlo antes de modificar una calificación.")
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(activity, { activity = it }, label = { Text("Actividad · Ej. Exposición 1") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(teammate, { teammate = it }, label = { Text("Compañero del equipo") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("No trabajó", "Participación parcial").forEach { option ->
                            FilterChip(selected = participation == option, onClick = { participation = option }, label = { Text(option) })
                        }
                    }
                    OutlinedTextField(detail, { detail = it }, label = { Text("Comentario opcional") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Button(onClick = ::saveReport, enabled = activity.isNotBlank() && teammate.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Enviar reporte anónimo") }
                    message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item {
            Text("Reglas: solo se reporta a integrantes del propio equipo; un reporte no cambia notas automáticamente; el docente puede marcarlo como Confirmado, No confirmado, Participación parcial o Sin evidencia suficiente.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
