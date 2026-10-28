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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.duoc.mpazpro.data.ConfiguracionDocente
import com.duoc.mpazpro.data.MpazDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaDocente(asignaturas: List<AsignaturaEducativa>) {
    val context = LocalContext.current
    val dao = remember(context) {
        MpazDatabase.obtenerInstancia(context).configuracionDocenteDao()
    }
    val scope = rememberCoroutineScope()
    val configuraciones = remember { mutableStateMapOf<String, ConfiguracionDocente>() }
    var cargando by remember { mutableStateOf(true) }
    var aviso by remember { mutableStateOf("") }

    val unidades = asignaturas.flatMap { asignatura ->
        asignatura.unidades.map { unidad ->
            val clave = "${asignatura.nombre}::${unidad.titulo}"
            Triple(asignatura.nombre, unidad, clave)
        }
    }

    LaunchedEffect(dao) {
        try {
            withContext(Dispatchers.IO) {
                dao.obtenerTodas()
            }.forEach { configuraciones[it.clave] = it }
        } catch (e: Exception) {
            aviso = "No se pudieron cargar las configuraciones guardadas."
        } finally {
            cargando = false
        }
    }

    fun configuracionActual(
        clave: String,
        asignatura: String,
        titulo: String
    ): ConfiguracionDocente {
        return configuraciones[clave] ?: ConfiguracionDocente(
            clave = clave,
            asignatura = asignatura,
            unidadTitulo = titulo
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Panel Docente",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Asigna Objetivos de Aprendizaje (OA), configura actividades y define el límite de intentos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (cargando) {
            CircularProgressIndicator()
            Text("Cargando configuraciones guardadas…")
        }

        if (aviso.isNotBlank()) {
            Text(aviso, color = MaterialTheme.colorScheme.error)
        }

        unidades.forEach { (nombreAsignatura, unidad, clave) ->
            val config = configuracionActual(clave, nombreAsignatura, unidad.titulo)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        nombreAsignatura,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(unidad.titulo, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(unidad.descripcion)

                    OutlinedTextField(
                        value = config.objetivosAprendizaje,
                        onValueChange = {
                            configuraciones[clave] = config.copy(objetivosAprendizaje = it)
                        },
                        label = { Text("Objetivos de Aprendizaje (OA)") },
                        placeholder = { Text("Ej.: OA 2, OA 4") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = config.tipoActividad,
                        onValueChange = {
                            configuraciones[clave] = config.copy(tipoActividad = it)
                        },
                        label = { Text("Tipo de actividad") },
                        placeholder = { Text("Desafío, práctica, cuestionario…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = config.instrucciones,
                        onValueChange = {
                            configuraciones[clave] = config.copy(instrucciones = it)
                        },
                        label = { Text("Instrucciones para la actividad") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Límite de intentos")
                        Text("${config.limiteIntentos}")
                    }
                    Slider(
                        value = config.limiteIntentos.toFloat(),
                        onValueChange = {
                            configuraciones[clave] = config.copy(limiteIntentos = it.toInt().coerceIn(1, 10))
                        },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Button(
                        onClick = {
                            val actual = configuracionActual(clave, nombreAsignatura, unidad.titulo)
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { dao.guardar(actual) }
                                    aviso = "Configuración guardada: ${unidad.titulo}"
                                } catch (e: Exception) {
                                    aviso = "No se pudo guardar la configuración de ${unidad.titulo}."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar configuración")
                    }
                }
            }
        }
    }
}
