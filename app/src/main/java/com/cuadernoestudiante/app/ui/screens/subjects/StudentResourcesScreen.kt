package com.cuadernoestudiante.app.ui.screens.subjects

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val supportedDocumentTypes = arrayOf("application/pdf","text/csv","text/comma-separated-values","application/csv","application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","text/plain")

@Composable
fun StudentResourcesScreen(onNotes: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("student_resources", 0) }
    var uriString by remember { mutableStateOf(prefs.getString("last_resource_uri", null)) }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) message = "No se seleccionó ningún archivo." else {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val readable = runCatching { context.contentResolver.openInputStream(uri)?.use { it.read() } != null }.getOrDefault(false)
            if (readable) { uriString = uri.toString(); prefs.edit().putString("last_resource_uri", uri.toString()).apply(); message = "Recurso guardado en este dispositivo." } else message = "No se pudo leer el archivo seleccionado."
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Materias y recursos", style = MaterialTheme.typography.headlineSmall)
        Text("Consulta materiales y guarda recursos personales sin modificar información enviada por el docente.")
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.EditNote,null); Text("Bloc de notas", style=MaterialTheme.typography.titleMedium) }
            Text("Escribe con teclado o dibuja con dedo, stylus o pen.")
            Button(onClick=onNotes, modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text("Abrir bloc de notas")}
        } }
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Description,null); Text("Documentos de estudio", style=MaterialTheme.typography.titleMedium) }
            Text("Formatos admitidos: PDF, CSV, XLS, XLSX, DOC, DOCX y TXT.")
            Button(onClick={picker.launch(supportedDocumentTypes)},modifier=Modifier.heightIn(min=48.dp)){Icon(Icons.Default.UploadFile,null);Spacer(Modifier.width(8.dp));Text(if(uriString==null)"Importar recurso" else "Cambiar recurso")}
            if(uriString!=null){OutlinedButton(onClick={val uri=Uri.parse(uriString);val intent=Intent(Intent.ACTION_VIEW).apply{setDataAndType(uri,context.contentResolver.getType(uri)?:"*/*");addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};runCatching{context.startActivity(intent)}.onFailure{message="No hay una aplicación compatible para abrir este archivo."}}){Icon(Icons.Default.FolderOpen,null);Spacer(Modifier.width(8.dp));Text("Abrir recurso")}}
            message?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
        } }
        Text("Tus notas y archivos personales no cambian calificaciones, asistencia ni datos del docente.", style=MaterialTheme.typography.bodySmall)
    }
}
