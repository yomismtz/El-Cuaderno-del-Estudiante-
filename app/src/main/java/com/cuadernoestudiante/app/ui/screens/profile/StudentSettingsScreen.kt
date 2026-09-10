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
import com.cuadernoestudiante.app.ui.theme.AppFontStyle

@Composable
fun StudentSettingsScreen(
    currentTheme: AgendaThemeStyle,
    classCode: String,
    onThemeChange: (AgendaThemeStyle) -> Unit,
    onClassCodeChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("student_ui", 0) }
    val language = LocalAppLanguage.current
    var code by remember(classCode) { mutableStateOf(classCode) }
    var darkMode by remember { mutableStateOf(prefs.getBoolean("ui_dark", false)) }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
    var fontStyle by remember { mutableStateOf(AppFontStyle.fromKey(prefs.getString("font_style", null))) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(language.text("Perfil y conexión", "Profile and connection"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(language.text("Personaliza la app y administra tu código de clase.", "Customize the app and manage your class code."))
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(language.text("Idioma", "Language"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { item ->
                            FilterChip(selected = language == item, onClick = { if (language != item) { AppLanguagePrefs.save(context, item); (context as? Activity)?.recreate() } }, label = { Text(item.label) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(language.text("Apariencia", "Appearance"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) { Text(language.text("Modo oscuro", "Dark mode")); Text(language.text("Desactívalo para modo claro.", "Turn it off for light mode."), style = MaterialTheme.typography.bodySmall) }
                        Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                    }
                    Text(language.text("Tamaño de letra", "Font size"), fontWeight = FontWeight.SemiBold)
                    Slider(value = fontScale, onValueChange = { fontScale = it }, valueRange = .85f..1.35f, steps = 4)
                    Text(language.text("Estilo de letra", "Font style"), fontWeight = FontWeight.SemiBold)
                    AppFontStyle.entries.forEach { style -> FilterChip(selected = fontStyle == style, onClick = { fontStyle = style }, label = { Text(style.label) }, modifier = Modifier.fillMaxWidth()) }
                    Button(
                        onClick = {
                            prefs.edit().putBoolean("ui_dark", darkMode).putFloat("font_scale", fontScale).putString("font_style", fontStyle.key).apply()
                            (context as? Activity)?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) { Text(language.text("Aplicar apariencia", "Apply appearance")) }
                }
            }
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(language.text("Código de clase", "Class code"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == '-' }.take(16) }, label = { Text(language.text("Ej. MAT-2B-7K4", "e.g. MAT-2B-7K4")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { onClassCodeChange(code.trim()) }, enabled = code.trim().length >= 4, modifier = Modifier.fillMaxWidth()) { Text(language.text("Vincular esta clase", "Join this class")) }
                }
            }
        }
        item {
            Text(language.text("Paleta de color", "Color palette"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(language.text("Las paletas ahora tienen contrastes claramente distintos.", "The palettes now use clearly different color families."))
        }
        items(AgendaThemeStyle.entries) { style ->
            ElevatedCard(onClick = { onThemeChange(style) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(style.title, fontWeight = FontWeight.SemiBold); Text(style.subtitle, style = MaterialTheme.typography.bodySmall) }
                    RadioButton(selected = style == currentTheme, onClick = { onThemeChange(style) })
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
