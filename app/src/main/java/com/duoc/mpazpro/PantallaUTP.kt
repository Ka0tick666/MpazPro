package com.duoc.mpazpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.RevisionUtp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaUTP(asignaturas: List<AsignaturaEducativa>) {
    val context = LocalContext.current
    val dao = remember(context) { MpazDatabase.obtenerInstancia(context).revisionUtpDao() }
    val scope = rememberCoroutineScope()
    val revisiones = remember { mutableStateMapOf<String, RevisionUtp>() }

    var filtro by remember { mutableStateOf("Todos") }
    var aviso by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    val filtros = listOf("Todos", "En revisión", "Autorizado", "Publicado", "Deshabilitado")
    val unidades = asignaturas.flatMap { asignatura ->
        asignatura.unidades.map { unidad ->
            Triple(asignatura.nombre, unidad, "${asignatura.nombre}::${unidad.titulo}")
        }
    }

    LaunchedEffect(dao) {
        try {
            dao.obtenerTodas().forEach { revisiones[it.clave] = it }
        } catch (e: Exception) {
            aviso = "No se pudieron cargar las revisiones guardadas."
        } finally {
            cargando = false
        }
    }

    fun revisionActual(clave: String, asignatura: String, titulo: String): RevisionUtp {
        return revisiones[clave] ?: RevisionUtp(
            clave = clave,
            asignatura = asignatura,
            unidadTitulo = titulo
        )
    }

    fun guardarRevision(revision: RevisionUtp) {
        // Actualiza la pantalla al instante y guarda el cambio en SQLite en segundo plano.
        revisiones[revision.clave] = revision
        scope.launch {
            try {
                dao.guardar(revision)
                aviso = "Cambios guardados correctamente."
            } catch (e: Exception) {
                aviso = "No se pudo guardar el cambio. Inténtalo nuevamente."
            }
        }
    }

    fun cambiarEstado(clave: String, asignatura: String, titulo: String, nuevoEstado: String, detalle: String) {
        val anterior = revisionActual(clave, asignatura, titulo)
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val entrada = "$fecha — $detalle"
        val nuevoHistorial = if (anterior.historial.isBlank()) entrada else anterior.historial + "\n" + entrada
        guardarRevision(anterior.copy(estado = nuevoEstado, historial = nuevoHistorial))
        aviso = "Estado actualizado a: $nuevoEstado"
    }

    val unidadesFiltradas = unidades.filter { (asignatura, unidad, clave) ->
        val estado = revisionActual(clave, asignatura, unidad.titulo).estado
        filtro == "Todos" || filtro == estado
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Panel UTP", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Revisa, autoriza y publica los contenidos educativos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen del material", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Asignaturas: ${asignaturas.size}")
                Text("Unidades: ${unidades.size}")
                Text("En revisión: ${unidades.count { (a, u, k) -> revisionActual(k, a, u.titulo).estado == "En revisión" }}")
                Text("Autorizadas: ${unidades.count { (a, u, k) -> revisionActual(k, a, u.titulo).estado == "Autorizado" }}")
                Text("Publicadas: ${unidades.count { (a, u, k) -> revisionActual(k, a, u.titulo).estado == "Publicado" }}")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Filtrar contenidos", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            filtros.take(3).forEach { opcion ->
                FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion) })
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            filtros.drop(3).forEach { opcion ->
                FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion) })
            }
        }
        Spacer(Modifier.height(12.dp))

        if (cargando) Text("Cargando revisiones guardadas...")
        if (aviso.isNotBlank()) {
            Text(aviso, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }
        if (unidadesFiltradas.isEmpty()) Text("No hay unidades para este filtro.")

        unidadesFiltradas.forEach { (asignatura, unidad, clave) ->
            val revision = revisionActual(clave, asignatura, unidad.titulo)
            val estado = revision.estado
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(asignatura, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(unidad.titulo, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(unidad.descripcion)
                    Spacer(Modifier.height(10.dp))
                    Text("Contenido educativo", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(unidad.contenido)
                    Spacer(Modifier.height(10.dp))
                    Text("Preguntas asociadas: ${unidad.preguntas.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Estado: $estado", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = revision.observacion,
                        onValueChange = { texto ->
                            guardarRevision(revisionActual(clave, asignatura, unidad.titulo).copy(observacion = texto))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observaciones de UTP") },
                        placeholder = { Text("Indica qué se debe corregir...") },
                        minLines = 2
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { cambiarEstado(clave, asignatura, unidad.titulo, "Autorizado", "Contenido autorizado") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = estado != "Publicado"
                    ) { Text("Autorizar contenido") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val observacion = revisionActual(clave, asignatura, unidad.titulo).observacion.trim()
                            if (observacion.isBlank()) {
                                aviso = "Escribe una observación antes de solicitar correcciones."
                            } else {
                                cambiarEstado(clave, asignatura, unidad.titulo, "Deshabilitado", "Corrección solicitada: $observacion")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Solicitar correcciones") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (estado == "Autorizado") {
                                cambiarEstado(clave, asignatura, unidad.titulo, "Publicado", "Contenido publicado")
                            } else {
                                aviso = "Primero debes autorizar el contenido."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = estado == "Autorizado"
                    ) { Text("Publicar contenido") }

                    val registros = revision.historial.split("\n").filter { it.isNotBlank() }
                    if (registros.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Historial de revisión", fontWeight = FontWeight.Bold)
                        registros.forEach { registro -> Text("• $registro", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}
