package com.cuadernoestudiante.app.ui.screens.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuadernoestudiante.app.ui.AppLanguage
import com.cuadernoestudiante.app.ui.AppLanguagePrefs
import com.cuadernoestudiante.app.ui.LocalAppLanguage
import com.cuadernoestudiante.app.ui.text
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle

@Composable
fun StudentSettingsScreen(
    currentTheme: AgendaThemeStyle,
    classCode: String,
    onThemeChange: (AgendaThemeStyle) -> Unit,
    onClassCodeChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    var code by remember(classCode) { mutableStateOf(classCode) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(language.text("Perfil y conexión", "Profile and connection"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(language.text("El alumno solo se vincula a sus clases mediante el código entregado por su docente.", "Students join classes only with the code provided by their teacher."))
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(language.text("Idioma", "Language"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { item ->
                            FilterChip(
                                selected = language == item,
                                onClick = {
                                    if (language != item) {
                                        AppLanguagePrefs.save(context, item)
                                        (context as? Activity)?.recreate()
                                    }
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(language.text("Código de clase", "Class code"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == '-' }.take(16) },
                        label = { Text(language.text("Ej. MAT-2B-7K4", "e.g. MAT-2B-7K4")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onClassCodeChange(code.trim()) },
                        enabled = code.trim().length >= 4,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(language.text("Vincular esta clase", "Join this class")) }
                    Text(
                        language.text(
                            "Cuando exista el servidor común, este código identificará exactamente la clase y permitirá recibir horario, actividades y avisos del docente sin mostrar datos de otros alumnos.",
                            "When the shared server is available, this code will identify the exact class and allow schedules, activities and teacher notices to be received without exposing other students' data."
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Text(language.text("Color de la interfaz", "Interface color"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(language.text("Usa la misma colección de temas que La Carpeta del Docente.", "Uses the same theme collection as The Teacher Folder."))
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
