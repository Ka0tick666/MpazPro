package com.duoc.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.mpazpro.AsignaturaEducativa
import com.duoc.mpazpro.UnidadEducativa
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.RevisionUtp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PantallaActividadEstudiante(
    asignaturas: List<AsignaturaEducativa>
) {
    val context = LocalContext.current
    val db = remember(context) { MpazDatabase.obtenerInstancia(context) }
    val revisionDao = remember(db) { db.revisionUtpDao() }

    val revisionesUtp = remember { mutableStateMapOf<String, RevisionUtp>() }
    var unidadSeleccionada by remember { mutableStateOf<UnidadEducativa?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(revisionDao) {
        try {
            withContext(Dispatchers.IO) {
                revisionDao.obtenerTodas().forEach { revision ->
                    revisionesUtp[revision.clave] = revision
                }
            }
        } catch (_: Exception) {
            // Manejo de error silencioso
        } finally {
            cargando = false
        }
    }

    val unidadActual = unidadSeleccionada
    if (unidadActual != null) {
        VistaDetalleActividad(
            unidad = unidadActual,
            onVolver = { unidadSeleccionada = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mis Asignaturas",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona una asignatura y despliega sus unidades para realizar las actividades.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (cargando) {
            CircularProgressIndicator()
        }

        val asignaturasPublicadas = asignaturas.map { asignatura ->
            val unidadesPublicadas = asignatura.unidades.filter { unidad ->
                val clave = "${asignatura.nombre}::${unidad.titulo}"
                val estado = revisionesUtp[clave]?.estado ?: "En revisión"
                estado == "Publicado"
            }
            asignatura.nombre to unidadesPublicadas
        }.filter { it.second.isNotEmpty() }

        if (asignaturasPublicadas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Aún no hay unidades publicadas por tus docentes.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            asignaturasPublicadas.forEach { (nombreAsignatura, unidades) ->
                var desplegado by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = nombreAsignatura,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${unidades.size} unidad(es) disponible(s)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            TextButton(onClick = { desplegado = !desplegado }) {
                                Text(if (desplegado) "▲ Ocultar" else "▼ Ver unidades")
                            }
                        }

                        if (desplegado) {
                            Spacer(Modifier.height(8.dp))

                            unidades.forEach { unidad ->
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

                                        Spacer(Modifier.height(4.dp))

                                        Text(unidad.descripcion)

                                        Spacer(Modifier.height(8.dp))

                                        Button(
                                            onClick = { unidadSeleccionada = unidad },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Comenzar actividad")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VistaDetalleActividad(
    unidad: UnidadEducativa,
    onVolver: () -> Unit
) {
    var preguntaActual by remember { mutableIntStateOf(0) }
    var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }
    var respondido by remember { mutableStateOf(false) }
    var actividadFinalizada by remember { mutableStateOf(false) }

    val preguntas = unidad.preguntas
    val pregunta = preguntas.getOrNull(preguntaActual)

    // Pantalla de Calificación / Estado Pendiente al terminar la actividad
    if (actividadFinalizada) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.HourglassTop,
                contentDescription = "Nota pendiente",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Actividad Completada",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = unidad.titulo,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calificación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "La nota aún no está disponible",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Tu docente debe revisar y publicar los resultados de esta actividad.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a mis asignaturas")
            }
        }
        return
    }

    // Vista de preguntas durante la actividad
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Volver a asignaturas")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = unidad.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (pregunta != null) {
            Text(
                text = "Pregunta ${preguntaActual + 1} de ${preguntas.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = pregunta.enunciado,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            pregunta.opciones.forEachIndexed { index, opcion ->
                val esCorrecta = index == pregunta.indiceCorrecto
                val colorBoton = when {
                    !respondido -> MaterialTheme.colorScheme.primary
                    index == opcionSeleccionada && esCorrecta -> Color(0xFF4CAF50)
                    index == opcionSeleccionada && !esCorrecta -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }

                Button(
                    onClick = {
                        if (!respondido) {
                            opcionSeleccionada = index
                            respondido = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = opcion, fontSize = 16.sp)
                }
            }

            if (respondido) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = pregunta.explicacion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (preguntaActual < preguntas.size - 1) {
                            preguntaActual++
                            opcionSeleccionada = null
                            respondido = false
                        } else {
                            actividadFinalizada = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (preguntaActual < preguntas.size - 1) "Siguiente pregunta" else "Finalizar evaluación"
                    )
                }
            }
        }
    }
}