package com.cuadernoestudiante.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle

@Composable
fun StudentSettingsScreen(
    currentTheme: AgendaThemeStyle,
    classCode: String,
    onThemeChange: (AgendaThemeStyle) -> Unit,
    onClassCodeChange: (String) -> Unit,
) {
    var code by remember(classCode) { mutableStateOf(classCode) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Perfil y conexión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("El alumno solo se vincula a sus clases mediante el código entregado por su docente.")
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Código de clase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == '-' }.take(16) },
                        label = { Text("Ej. MAT-2B-7K4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onClassCodeChange(code.trim()) },
                        enabled = code.trim().length >= 4,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Vincular esta clase") }
                    Text(
                        "Cuando exista el servidor común, este código identificará exactamente la clase y permitirá recibir horario, actividades y avisos del docente sin mostrar datos de otros alumnos.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Text("Color de la interfaz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Usa la misma colección de temas que La Carpeta del Docente.")
        }
        items(AgendaThemeStyle.entries) { style ->
            ElevatedCard(
                onClick = { onThemeChange(style) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(style.title, fontWeight = FontWeight.SemiBold)
                        Text(style.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                    RadioButton(selected = style == currentTheme, onClick = { onThemeChange(style) })
                }
            }
        }
    }
}
