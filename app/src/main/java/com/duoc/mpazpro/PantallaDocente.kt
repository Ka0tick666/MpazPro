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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import com.duoc.mpazpro.data.ConfiguracionDocente
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.Unidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaDocente(asignaturas: List<AsignaturaEducativa>) {

    val context = LocalContext.current

    val db = remember(context) {
        MpazDatabase.obtenerInstancia(context)
    }

    val asignaturaDao = remember(db) {
        db.asignaturaDao()
    }

    val unidadDao = remember(db) {
        db.unidadDao()
    }

    val configuracionDao = remember(db) {
        db.configuracionDocenteDao()
    }

    val scope = rememberCoroutineScope()

    val asignaturasGuardadas by asignaturaDao
        .obtenerTodas()
        .collectAsState(initial = emptyList())

    var idAsignaturaSeleccionada by remember {
        mutableStateOf<Long?>(null)
    }

    val unidadesGuardadas by remember(idAsignaturaSeleccionada) {
        if (idAsignaturaSeleccionada == null) {
            kotlinx.coroutines.flow.flowOf(emptyList<Unidad>())
        } else {
            unidadDao.obtenerPorAsignatura(
                idAsignaturaSeleccionada!!
            )
        }
    }.collectAsState(initial = emptyList())

    val configuraciones =
        remember { mutableStateMapOf<String, ConfiguracionDocente>() }

    var cargando by remember {
        mutableStateOf(true)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var aviso by remember {
        mutableStateOf("")
    }

    // Seleccionar automáticamente la primera asignatura disponible.
    LaunchedEffect(asignaturasGuardadas) {
        if (
            idAsignaturaSeleccionada == null &&
            asignaturasGuardadas.isNotEmpty()
        ) {
            idAsignaturaSeleccionada =
                asignaturasGuardadas.first().idAsignatura
        }

        // Si se elimina la asignatura seleccionada,
        // seleccionar otra que todavía exista.
        if (
            idAsignaturaSeleccionada != null &&
            asignaturasGuardadas.none {
                it.idAsignatura == idAsignaturaSeleccionada
            }
        ) {
            idAsignaturaSeleccionada =
                asignaturasGuardadas.firstOrNull()?.idAsignatura
        }
    }

    // Recuperar las configuraciones que ya están guardadas.
    LaunchedEffect(configuracionDao) {
        try {
            val guardadas = withContext(Dispatchers.IO) {
                configuracionDao.obtenerTodas()
            }

            guardadas.forEach { configuracion ->
                configuraciones[configuracion.clave] = configuracion
            }
        } catch (e: Exception) {
            aviso = "No se pudieron cargar las configuraciones guardadas."
        } finally {
            cargando = false
        }
    }

    fun obtenerConfiguracion(
        clave: String,
        asignatura: String,
        titulo: String
    ): ConfiguracionDocente {
        return configuraciones[clave]
            ?: ConfiguracionDocente(
                clave = clave,
                asignatura = asignatura,
                unidadTitulo = titulo
            )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Panel Docente",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona una asignatura y configura sus unidades, " +
                    "objetivos de aprendizaje, actividades e intentos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (cargando) {
            CircularProgressIndicator()
            Text("Cargando configuraciones guardadas…")
        }

        if (aviso.isNotBlank()) {
            Text(
                text = aviso,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // SELECCIÓN DE ASIGNATURA
        Text(
            text = "Asignaturas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (asignaturasGuardadas.isEmpty()) {
            Text(
                text = "Todavía no hay asignaturas registradas. " +
                        "Crea una desde el panel UTP.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                asignaturasGuardadas.forEach { asignatura ->
                    FilterChip(
                        selected = idAsignaturaSeleccionada ==
                                asignatura.idAsignatura,
                        onClick = {
                            idAsignaturaSeleccionada =
                                asignatura.idAsignatura
                            aviso = ""
                        },
                        label = {
                            Text(asignatura.nombre)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // DESCRIPCIÓN DE LA ASIGNATURA SELECCIONADA
        val asignaturaSeleccionada =
            asignaturasGuardadas.firstOrNull {
                it.idAsignatura == idAsignaturaSeleccionada
            }

        if (asignaturaSeleccionada != null) {

            Text(
                text = asignaturaSeleccionada.nombre,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (asignaturaSeleccionada.descripcion.isNotBlank()) {
                Text(asignaturaSeleccionada.descripcion)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Unidades de aprendizaje",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (unidadesGuardadas.isEmpty()) {
                Text(
                    text = "Esta asignatura todavía no tiene unidades. " +
                            "Puedes agregarlas desde el panel UTP."
                )
            }

            // CONFIGURACIÓN DE CADA UNIDAD
            unidadesGuardadas.forEach { unidad ->

                val clave =
                    "${asignaturaSeleccionada.nombre}::${unidad.titulo}"

                val config = obtenerConfiguracion(
                    clave = clave,
                    asignatura = asignaturaSeleccionada.nombre,
                    titulo = unidad.titulo
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            text = unidad.titulo,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (unidad.descripcion.isNotBlank()) {
                            Text(unidad.descripcion)
                        }

                        if (unidad.contenido.isNotBlank()) {
                            Text(
                                text = "Contenido registrado por UTP:",
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(unidad.contenido)
                        }

                        OutlinedTextField(
                            value = config.objetivosAprendizaje,
                            onValueChange = { nuevoValor ->
                                configuraciones[clave] = config.copy(
                                    objetivosAprendizaje = nuevoValor
                                )
                            },
                            label = {
                                Text("Objetivos de Aprendizaje (OA)")
                            },
                            placeholder = {
                                Text("Ej.: OA 2, OA 4")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = config.tipoActividad,
                            onValueChange = { nuevoValor ->
                                configuraciones[clave] = config.copy(
                                    tipoActividad = nuevoValor
                                )
                            },
                            label = {
                                Text("Tipo de actividad")
                            },
                            placeholder = {
                                Text("Desafío, práctica, cuestionario…")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = config.instrucciones,
                            onValueChange = { nuevoValor ->
                                configuraciones[clave] = config.copy(
                                    instrucciones = nuevoValor
                                )
                            },
                            label = {
                                Text("Instrucciones para la actividad")
                            },
                            placeholder = {
                                Text("Escribe las instrucciones...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text("Límite de intentos")
                            Text("${config.limiteIntentos}")
                        }

                        Slider(
                            value = config.limiteIntentos.toFloat(),
                            onValueChange = { nuevoValor ->
                                configuraciones[clave] = config.copy(
                                    limiteIntentos = nuevoValor
                                        .toInt()
                                        .coerceIn(1, 10)
                                )
                            },
                            valueRange = 1f..10f,
                            steps = 8
                        )

                        Button(
                            onClick = {
                                val actual =
                                    configuraciones[clave] ?: config

                                scope.launch {
                                    guardando = true

                                    try {
                                        withContext(Dispatchers.IO) {
                                            configuracionDao.guardar(actual)
                                        }

                                        aviso =
                                            "Configuración guardada: " +
                                                    unidad.titulo
                                    } catch (e: Exception) {
                                        aviso =
                                            "No se pudo guardar la " +
                                                    "configuración de ${unidad.titulo}."
                                    } finally {
                                        guardando = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !guardando
                        ) {
                            Text(
                                if (guardando) {
                                    "Guardando..."
                                } else {
                                    "Guardar configuración"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}