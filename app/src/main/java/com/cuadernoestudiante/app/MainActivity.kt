package com.cuadernoestudiante.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuadernoestudiante.app.data.demo.DemoStudentRepository
import com.cuadernoestudiante.app.ui.AppLanguagePrefs
import com.cuadernoestudiante.app.ui.LocalAppLanguage
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle
import com.cuadernoestudiante.app.ui.theme.NotebookBackground
import com.cuadernoestudiante.app.ui.theme.StudentTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { DemoStudentRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("student_ui", MODE_PRIVATE)
        setContent {
            val studentViewModel: StudentViewModel = viewModel(factory = StudentViewModel.Factory(repository))
            val appLanguage = remember { AppLanguagePrefs.load(this@MainActivity) }
            var familyTheme by remember {
                mutableStateOf(
                    AgendaThemeStyle.entries.firstOrNull { it.key == prefs.getString("theme", null) }
                        ?: AgendaThemeStyle.MINT_LAVENDER
                )
            }
            var classCode by remember { mutableStateOf(prefs.getString("class_code", "") ?: "") }

            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                StudentTheme(style = familyTheme) {
                    NotebookBackground(style = familyTheme) {
                        StudentApp(
                            viewModel = studentViewModel,
                            currentTheme = familyTheme,
                            classCode = classCode,
                            onThemeChange = {
                                familyTheme = it
                                prefs.edit().putString("theme", it.key).apply()
                            },
                            onClassCodeChange = {
                                classCode = it
                                prefs.edit().putString("class_code", it).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}
