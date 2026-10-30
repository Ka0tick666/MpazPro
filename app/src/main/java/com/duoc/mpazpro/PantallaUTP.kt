
package com.duoc.mpazpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.duoc.mpazpro.data.Asignatura
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.RevisionUtp
import com.duoc.mpazpro.data.Unidad
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaUTP(asignaturas: List<AsignaturaEducativa> = emptyList()) {
    val context = LocalContext.current

    val database = remember(context) {
        MpazDatabase.obtenerInstancia(context)
    }

    val revisionDao = remember(database) {
        database.revisionUtpDao()
    }

    val asignaturaDao = remember(database) {
        database.asignaturaDao()
    }

    val unidadDao = remember(database) {
        database.unidadDao()
    }

    val scope = rememberCoroutineScope()

    val asignaturasGuardadas by
    asignaturaDao.obtenerTodas().collectAsState(initial = emptyList())

    var asignaturaSeleccionada by remember {
        mutableStateOf<Asignatura?>(null)
    }

    var nombreAsignatura by remember { mutableStateOf("") }
    var descripcionAsignatura by remember { mutableStateOf("") }
    var tituloUnidad by remember { mutableStateOf("") }
    var descripcionUnidad by remember { mutableStateOf("") }
    var contenidoUnidad by remember { mutableStateOf("") }

    var mostrarFormularioAsignatura by remember {
        mutableStateOf(false)
    }

    var mostrarFormularioUnidad by remember {
        mutableStateOf(false)
    }

    var confirmacion by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var filtro by remember { mutableStateOf("Todos") }

    val revisiones = remember {
        mutableStateMapOf<String, RevisionUtp>()
    }

    val unidadesGuardadas by remember(
        asignaturaSeleccionada?.idAsignatura
    ) {
        val seleccionada = asignaturaSeleccionada

        if (seleccionada == null) {
            flowOf(emptyList<Unidad>())
        } else {
            unidadDao.obtenerPorAsignatura(
                seleccionada.idAsignatura
            )
        }
    }.collectAsState(initial = emptyList())

    val filtros = listOf(
        "Todos",
        "En revisión",
        "Autorizado",
        "Publicado",
        "Deshabilitado"
    )

    LaunchedEffect(revisionDao) {
        try {
            revisionDao.obtenerTodas().forEach {
                revisiones[it.clave] = it
            }
        } catch (_: Exception) {
            aviso = "No se pudieron cargar las revisiones anteriores."
        }
    }

    fun revisionActual(
        clave: String,
        asignatura: String,
        titulo: String
    ): RevisionUtp {
        return revisiones[clave] ?: RevisionUtp(
            clave = clave,
            asignatura = asignatura,
            unidadTitulo = titulo
        )
    }

    fun guardarRevision(revision: RevisionUtp) {
        revisiones[revision.clave] = revision

        scope.launch {
            try {
                revisionDao.guardar(revision)
                aviso = "Revisión guardada."
            } catch (_: Exception) {
                aviso = "No se pudo guardar la revisión."
            }
        }
    }

    fun cambiarEstado(
        clave: String,
        asignatura: String,
        titulo: String,
        nuevoEstado: String,
        detalle: String
    ) {
        val anterior = revisionActual(
            clave,
            asignatura,
            titulo
        )

        val fecha = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())

        val entrada = "$fecha — $detalle"

        val historial = if (anterior.historial.isBlank()) {
            entrada
        } else {
            "${anterior.historial}\n$entrada"
        }

        guardarRevision(
            anterior.copy(
                estado = nuevoEstado,
                historial = historial
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = "Panel UTP",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Administra asignaturas, unidades y revisiones educativas.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Resumen
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Resumen",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Asignaturas registradas: ${asignaturasGuardadas.size}"
                )

                Text(
                    "Unidades de la asignatura seleccionada: ${unidadesGuardadas.size}"
                )

                Text(
                    "Asignaturas iniciales del sistema: ${asignaturas?.size ?: 0}"
                )

                Text(
                    "Asignaturas publicadas: ${
                        asignaturasGuardadas.count { it.publicada }
                    }"
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                nombreAsignatura = ""
                descripcionAsignatura = ""
                mostrarFormularioAsignatura = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("＋ Crear asignatura")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Asignaturas guardadas",
            fontWeight = FontWeight.Bold
        )

        if (asignaturasGuardadas.isEmpty()) {
            Text(
                "Todavía no hay asignaturas creadas desde UTP.",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        asignaturasGuardadas.forEach { asignatura ->
            val estaSeleccionada =
                asignaturaSeleccionada?.idAsignatura ==
                        asignatura.idAsignatura

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (estaSeleccionada) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = asignatura.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    if (asignatura.descripcion.isNotBlank()) {
                        Text(asignatura.descripcion)
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (asignatura.publicada) {
                            "Estado: Publicada para estudiantes"
                        } else {
                            "Estado: Borrador"
                        },
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                asignaturaSeleccionada = asignatura
                                aviso = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (estaSeleccionada) {
                                    "Seleccionada"
                                } else {
                                    "Ver unidades"
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                asignaturaSeleccionada = asignatura
                                confirmacion = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Eliminar")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    asignaturaDao.actualizar(
                                        asignatura.copy(
                                            publicada = !asignatura.publicada
                                        )
                                    )

                                    aviso = if (!asignatura.publicada) {
                                        "Asignatura publicada correctamente."
                                    } else {
                                        "Publicación de la asignatura retirada."
                                    }
                                } catch (_: Exception) {
                                    aviso =
                                        "No se pudo actualizar la publicación."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (asignatura.publicada) {
                                "Retirar publicación"
                            } else {
                                "Publicar asignatura"
                            }
                        )
                    }
                }
            }
        }

        // Unidades de la asignatura seleccionada
        if (asignaturaSeleccionada != null) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Unidades de ${asignaturaSeleccionada!!.nombre}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    tituloUnidad = ""
                    descripcionUnidad = ""
                    contenidoUnidad = ""
                    mostrarFormularioUnidad = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("＋ Crear unidad")
            }

            if (unidadesGuardadas.isEmpty()) {
                Text(
                    "Esta asignatura todavía no tiene unidades.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            unidadesGuardadas.forEach { unidad ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = unidad.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        if (unidad.descripcion.isNotBlank()) {
                            Text(unidad.descripcion)
                        }

                        if (unidad.contenido.isNotBlank()) {
                            Text(
                                text = unidad.contenido,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = if (unidad.publicada) {
                                "Estado: Publicada"
                            } else {
                                "Estado: Borrador"
                            },
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        unidadDao.actualizar(
                                            unidad.copy(
                                                publicada = !unidad.publicada
                                            )
                                        )

                                        aviso = if (!unidad.publicada) {
                                            "Unidad publicada correctamente."
                                        } else {
                                            "Publicación de la unidad retirada."
                                        }
                                    } catch (_: Exception) {
                                        aviso =
                                            "No se pudo actualizar la unidad."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (unidad.publicada) {
                                    "Retirar publicación"
                                } else {
                                    "Publicar unidad"
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        unidadDao.eliminar(unidad)
                                        aviso = "Unidad eliminada."
                                    } catch (_: Exception) {
                                        aviso = "No se pudo eliminar la unidad."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Eliminar unidad")
                        }
                    }
                }
            }
        }

        // Revisión del material inicial
        // --- REVISIÓN DEL MATERIAL INICIAL (AGRUPADO Y COLAPSABLE) ---
        Spacer(Modifier.height(20.dp))

        Text(
            "Revisión del material inicial",
            fontWeight = FontWeight.Bold
        )

        Text(
            "Esta sección conserva la revisión de las unidades definidas actualmente en el código."
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            filtros.take(3).forEach { opcion ->
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = { Text(opcion) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            filtros.drop(3).forEach { opcion ->
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = { Text(opcion) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Agrupamos las unidades por nombre de asignatura
        val asignaturasAgrupadas = asignaturas.map { asignatura ->
            val unidadesFiltradas = asignatura.unidades.filter { unidad ->
                val clave = "${asignatura.nombre}::${unidad.titulo}"
                filtro == "Todos" || revisionActual(clave, asignatura.nombre, unidad.titulo).estado == filtro
            }
            asignatura.nombre to unidadesFiltradas
        }.filter { it.second.isNotEmpty() } // Solo mostramos asignaturas que tengan unidades visibles según el filtro

        asignaturasAgrupadas.forEach { (nombreAsignatura, unidades) ->
            var desplegado by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Cabecera desplegable de la asignatura
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = nombreAsignatura,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${unidades.size} unidad(es) disponible(s)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = { desplegado = !desplegado }) {
                            Text(if (desplegado) "▲ Ocultar" else "▼ Ver unidades")
                        }
                    }

                    // Contenido comprimido (solo se muestra al hacer clic en "Ver unidades")
                    if (desplegado) {
                        Spacer(Modifier.height(10.dp))

                        unidades.forEach { unidad ->
                            val clave = "$nombreAsignatura::${unidad.titulo}"
                            val revision = revisionActual(clave, nombreAsignatura, unidad.titulo)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = unidad.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Text(unidad.descripcion)

                                    Spacer(Modifier.height(6.dp))

                                    Text("Preguntas: ${unidad.preguntas.size}")

                                    Text(
                                        text = "Estado: ${revision.estado}",
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = revision.observacion,
                                        onValueChange = { texto ->
                                            guardarRevision(
                                                revisionActual(
                                                    clave,
                                                    nombreAsignatura,
                                                    unidad.titulo
                                                ).copy(observacion = texto)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Observaciones UTP") },
                                        minLines = 2
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            cambiarEstado(
                                                clave,
                                                nombreAsignatura,
                                                unidad.titulo,
                                                "Autorizado",
                                                "Contenido autorizado"
                                            )
                                        },
                                        enabled = revision.estado != "Publicado",
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Autorizar contenido")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val observacion = revisionActual(
                                                clave,
                                                nombreAsignatura,
                                                unidad.titulo
                                            ).observacion.trim()

                                            if (observacion.isBlank()) {
                                                aviso =
                                                    "Escribe una observación antes de solicitar correcciones."
                                            } else {
                                                cambiarEstado(
                                                    clave,
                                                    nombreAsignatura,
                                                    unidad.titulo,
                                                    "Deshabilitado",
                                                    "Corrección solicitada: $observacion"
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Solicitar correcciones")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (revision.estado == "Autorizado") {
                                                cambiarEstado(
                                                    clave,
                                                    nombreAsignatura,
                                                    unidad.titulo,
                                                    "Publicado",
                                                    "Contenido publicado"
                                                )
                                            } else {
                                                aviso =
                                                    "Primero debes autorizar el contenido."
                                            }
                                        },
                                        enabled = revision.estado == "Autorizado",
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Publicar contenido")
                                    }

                                    revision.historial
                                        .split("\n")
                                        .filter { it.isNotBlank() }
                                        .forEach { entrada ->
                                            Text(
                                                text = entrada,
                                                fontSize = 12.sp
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Formulario para crear asignatura
    if (mostrarFormularioAsignatura) {
        AlertDialog(
            onDismissRequest = {
                mostrarFormularioAsignatura = false
            },
            title = {
                Text("Nueva asignatura")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreAsignatura,
                        onValueChange = { nombreAsignatura = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descripcionAsignatura,
                        onValueChange = { descripcionAsignatura = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = nombreAsignatura.isNotBlank() && !cargando,
                    onClick = {
                        scope.launch {
                            cargando = true

                            try {
                                asignaturaDao.insertar(
                                    Asignatura(
                                        nombre = nombreAsignatura.trim(),
                                        descripcion = descripcionAsignatura.trim(),
                                        publicada = false
                                    )
                                )

                                aviso = "Asignatura creada correctamente."
                                mostrarFormularioAsignatura = false
                            } catch (_: Exception) {
                                aviso =
                                    "No se pudo crear la asignatura. Revisa si ya existe o consulta el error."
                            } finally {
                                cargando = false
                            }
                        }
                    }
                ) {
                    Text(if (cargando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarFormularioAsignatura = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Formulario para crear unidad
    if (mostrarFormularioUnidad && asignaturaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarFormularioUnidad = false
            },
            title = {
                Text("Nueva unidad")
            },
            text = {
                Column(
                    modifier = Modifier
                        .height(360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = tituloUnidad,
                        onValueChange = { tituloUnidad = it },
                        label = { Text("Título de la unidad") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descripcionUnidad,
                        onValueChange = { descripcionUnidad = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = contenidoUnidad,
                        onValueChange = { contenidoUnidad = it },
                        label = { Text("Contenido educativo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "La unidad se creará como borrador. Después podrás publicarla desde la lista de unidades."
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = tituloUnidad.isNotBlank() && !cargando,
                    onClick = {
                        val seleccionada = asignaturaSeleccionada
                            ?: return@TextButton

                        scope.launch {
                            cargando = true

                            try {
                                unidadDao.insertar(
                                    Unidad(
                                        idAsignatura = seleccionada.idAsignatura,
                                        titulo = tituloUnidad.trim(),
                                        descripcion = descripcionUnidad.trim(),
                                        contenido = contenidoUnidad.trim(),
                                        publicada = false
                                    )
                                )

                                aviso = "Unidad creada correctamente."
                                mostrarFormularioUnidad = false
                            } catch (_: Exception) {
                                aviso = "No se pudo crear la unidad."
                            } finally {
                                cargando = false
                            }
                        }
                    }
                ) {
                    Text(if (cargando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarFormularioUnidad = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmación para eliminar asignatura
    if (confirmacion && asignaturaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                confirmacion = false
            },
            title = {
                Text("Eliminar asignatura")
            },
            text = {
                Text(
                    "¿Eliminar ${asignaturaSeleccionada!!.nombre} y todas sus unidades? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val seleccionada = asignaturaSeleccionada
                        confirmacion = false

                        if (seleccionada != null) {
                            scope.launch {
                                try {
                                    asignaturaDao.eliminar(seleccionada)
                                    asignaturaSeleccionada = null
                                    aviso = "Asignatura eliminada."
                                } catch (_: Exception) {
                                    aviso = "No se pudo eliminar la asignatura."
                                }
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
