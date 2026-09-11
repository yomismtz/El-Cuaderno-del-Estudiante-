package com.cuadernoestudiante.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuadernoestudiante.app.data.repository.OfflineWriteResult
import com.cuadernoestudiante.app.data.repository.OnlineStudentRepository
import com.cuadernoestudiante.app.network.CentralBackend
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
    private val repository by lazy { OnlineStudentRepository(this, backend) }

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

            DisposableEffect(Unit) {
                val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        repository.refreshFromServer()
                    }
                }
                runCatching { connectivity.registerDefaultNetworkCallback(callback) }
                onDispose { runCatching { connectivity.unregisterNetworkCallback(callback) } }
            }

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
                                backend = backend,
                                currentTheme = familyTheme,
                                classCode = classCode,
                                onThemeChange = { familyTheme = it; prefs.edit().putString("theme", it.key).apply() },
                                onClassCodeChange = { requestedCode ->
                                    val normalizedCode = requestedCode.trim().uppercase()
                                    scope.launch {
                                        runCatching { repository.joinClassOrQueue(normalizedCode) }
                                            .onSuccess { result ->
                                                classCode = normalizedCode
                                                prefs.edit().putString("class_code", normalizedCode).apply()
                                                if (result == OfflineWriteResult.SENT) {
                                                    repository.refreshFromServer()
                                                    Toast.makeText(this@MainActivity, "Clase vinculada correctamente", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Sin conexión: el código quedó guardado y se enviará automáticamente cuando vuelva internet.",
                                                        Toast.LENGTH_LONG,
                                                    ).show()
                                                }
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
