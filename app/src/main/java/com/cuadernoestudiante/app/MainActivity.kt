package com.cuadernoestudiante.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuadernoestudiante.app.data.repository.OnlineStudentRepository
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.network.JoinClassRequest
import com.cuadernoestudiante.app.ui.AppLanguagePrefs
import com.cuadernoestudiante.app.ui.LocalAppLanguage
import com.cuadernoestudiante.app.ui.OnlineAuthScreen
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle
import com.cuadernoestudiante.app.ui.theme.AppFontStyle
import com.cuadernoestudiante.app.ui.theme.NotebookBackground
import com.cuadernoestudiante.app.ui.theme.StudentTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val backend by lazy { CentralBackend(this) }
    private val repository by lazy { OnlineStudentRepository(backend) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("student_ui", MODE_PRIVATE)
        setContent {
            val studentViewModel: StudentViewModel = viewModel(factory = StudentViewModel.Factory(repository))
            val appLanguage = remember { AppLanguagePrefs.load(this@MainActivity) }
            var familyTheme by remember { mutableStateOf(AgendaThemeStyle.entries.firstOrNull { it.key == prefs.getString("theme", null) } ?: AgendaThemeStyle.MINT_LAVENDER) }
            var classCode by remember { mutableStateOf(prefs.getString("class_code", "") ?: "") }
            val darkMode = prefs.getBoolean("ui_dark", false)
            val fontScale = prefs.getFloat("font_scale", 1f)
            val fontStyle = AppFontStyle.fromKey(prefs.getString("font_style", null))
            val scope = rememberCoroutineScope()
            var onlineSession by remember { mutableStateOf(!backend.tokenStore.accessToken.isNullOrBlank()) }

            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                StudentTheme(style = familyTheme, darkMode = darkMode, fontScale = fontScale, fontStyle = fontStyle) {
                    NotebookBackground(style = familyTheme) {
                        if (!onlineSession) {
                            OnlineAuthScreen(
                                backend = backend,
                                onAuthenticated = {
                                    onlineSession = true
                                    repository.refreshFromServer()
                                },
                            )
                        } else {
                            StudentApp(
                                viewModel = studentViewModel,
                                currentTheme = familyTheme,
                                classCode = classCode,
                                onThemeChange = { familyTheme = it; prefs.edit().putString("theme", it.key).apply() },
                                onClassCodeChange = { requestedCode ->
                                    val normalizedCode = requestedCode.trim().uppercase()
                                    scope.launch {
                                        runCatching { backend.api.joinClass(JoinClassRequest(normalizedCode)) }
                                            .onSuccess {
                                                classCode = normalizedCode
                                                prefs.edit().putString("class_code", normalizedCode).apply()
                                                repository.refreshFromServer()
                                                Toast.makeText(this@MainActivity, "Clase vinculada correctamente", Toast.LENGTH_SHORT).show()
                                            }
                                            .onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.message ?: "No se pudo vincular la clase",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
