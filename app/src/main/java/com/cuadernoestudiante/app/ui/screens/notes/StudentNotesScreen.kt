package com.cuadernoestudiante.app.ui.screens.notes

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class NotePoint(val x: Float, val y: Float)
private data class StudentNote(val id: String, val title: String, val text: String, val strokes: List<List<NotePoint>>)

@Composable
fun StudentNotesScreen() {
    val context = LocalContext.current
    var notes by remember { mutableStateOf(loadNotes(context)) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    fun persist(updated: List<StudentNote>) { notes = updated; saveNotes(context, updated) }

    if (selectedId == null) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Bloc de notas", style = MaterialTheme.typography.headlineSmall); Text("Notas personales del estudiante. Puedes escribir con teclado o dibujar con dedo, stylus o pen.") }
            item { Button(onClick = { val n=StudentNote(UUID.randomUUID().toString(),"Nueva nota","", emptyList());persist(listOf(n)+notes);selectedId=n.id },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Nueva nota")} }
            if(notes.isEmpty()) item{Text("Aún no hay notas guardadas.")}
            items(notes,key={it.id}){n->ElevatedCard(onClick={selectedId=n.id},modifier=Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(14.dp)){Icon(Icons.Rounded.EditNote,null);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(n.title.ifBlank{"Sin título"},style=MaterialTheme.typography.titleMedium);Text(n.text.lineSequence().firstOrNull()?.take(90).orEmpty().ifBlank{"Nota manuscrita o vacía"},style=MaterialTheme.typography.bodySmall)};IconButton(onClick={persist(notes.filterNot{it.id==n.id})}){Icon(Icons.Rounded.Delete,"Eliminar")}}}}
        }
    } else {
        val n=notes.firstOrNull{it.id==selectedId}; if(n==null){selectedId=null;return}
        Editor(n,{e->persist(notes.map{if(it.id==e.id)e else it})},{persist(notes.filterNot{it.id==n.id});selectedId=null})
    }
}

@Composable
private fun Editor(note:StudentNote,onSave:(StudentNote)->Unit,onDelete:()->Unit){
    var title by remember(note.id){mutableStateOf(note.title)};var text by remember(note.id){mutableStateOf(note.text)};var strokes by remember(note.id){mutableStateOf(note.strokes)};var draw by remember{mutableStateOf(false)};var message by remember{mutableStateOf<String?>(null)}
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{OutlinedTextField(title,{title=it},label={Text("Título")},modifier=Modifier.fillMaxWidth(),singleLine=true)}
        item{OutlinedTextField(text,{text=it},label={Text("Escribe aquí")},modifier=Modifier.fillMaxWidth(),minLines=6)}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=draw,onClick={draw=!draw},label={Text(if(draw)"Pen activo" else "Dibujar")},leadingIcon={Icon(Icons.Rounded.Draw,null)});TextButton(onClick={strokes=emptyList()}){Text("Borrar dibujo")}}}
        item{val ink=MaterialTheme.colorScheme.onSurface;Surface(Modifier.fillMaxWidth().height(340.dp),tonalElevation=1.dp){Canvas(Modifier.fillMaxSize().pointerInput(draw,note.id){if(draw){detectDragGestures(onDragStart={p->val nx=if(size.width==0)0f else p.x/size.width;val ny=if(size.height==0)0f else p.y/size.height;strokes=strokes+listOf(listOf(NotePoint(nx,ny)))},onDrag={c,_->c.consume();val p=c.position;val nx=if(size.width==0)0f else p.x/size.width;val ny=if(size.height==0)0f else p.y/size.height;if(strokes.isNotEmpty())strokes=strokes.dropLast(1)+listOf(strokes.last()+NotePoint(nx,ny))})}}){strokes.forEach{s->if(s.size==1){val p=s.first();drawCircle(ink,2.5f,Offset(p.x*size.width,p.y*size.height))}else if(s.size>1){val path=Path().apply{moveTo(s.first().x*size.width,s.first().y*size.height);s.drop(1).forEach{lineTo(it.x*size.width,it.y*size.height)}};drawPath(path,ink,style=Stroke(4f,cap=StrokeCap.Round))}}}}}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={onSave(note.copy(title=title.trim(),text=text,strokes=strokes));message="Nota guardada."},modifier=Modifier.weight(1f)){Icon(Icons.Rounded.Save,null);Spacer(Modifier.width(6.dp));Text("Guardar")};OutlinedButton(onClick=onDelete){Icon(Icons.Rounded.Delete,null);Spacer(Modifier.width(6.dp));Text("Eliminar")}}}
        message?.let{item{Text(it,style=MaterialTheme.typography.bodySmall)}}
    }
}

private fun loadNotes(c:Context):List<StudentNote> = runCatching{val raw=c.getSharedPreferences("student_notes",0).getString("notes","[]")?:"[]";val a=JSONArray(raw);(0 until a.length()).map{i->val o=a.getJSONObject(i);val sa=o.optJSONArray("strokes")?:JSONArray();val strokes=(0 until sa.length()).map{si->val pa=sa.getJSONArray(si);(0 until pa.length()).map{pi->val p=pa.getJSONObject(pi);NotePoint(p.optDouble("x").toFloat(),p.optDouble("y").toFloat())}};StudentNote(o.getString("id"),o.optString("title"),o.optString("text"),strokes)}}.getOrDefault(emptyList())
private fun saveNotes(c:Context,notes:List<StudentNote>){val a=JSONArray();notes.forEach{n->val o=JSONObject().put("id",n.id).put("title",n.title).put("text",n.text);val sa=JSONArray();n.strokes.forEach{s->val pa=JSONArray();s.forEach{pa.put(JSONObject().put("x",it.x.toDouble()).put("y",it.y.toDouble()))};sa.put(pa)};o.put("strokes",sa);a.put(o)};c.getSharedPreferences("student_notes",0).edit().putString("notes",a.toString()).apply()}
