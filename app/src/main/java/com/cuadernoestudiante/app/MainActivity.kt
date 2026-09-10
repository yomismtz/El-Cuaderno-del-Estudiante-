package com.cuadernoestudiante.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuadernoestudiante.app.data.demo.DemoStudentRepository
import com.cuadernoestudiante.app.ui.theme.AgendaThemeStyle
import com.cuadernoestudiante.app.ui.theme.NotebookBackground
import com.cuadernoestudiante.app.ui.theme.StudentTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { DemoStudentRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val studentViewModel: StudentViewModel = viewModel(
                factory = StudentViewModel.Factory(repository)
            )
            val familyTheme = AgendaThemeStyle.MINT_LAVENDER

            StudentTheme(style = familyTheme) {
                NotebookBackground(style = familyTheme) {
                    StudentApp(viewModel = studentViewModel)
                }
            }
        }
    }
}
