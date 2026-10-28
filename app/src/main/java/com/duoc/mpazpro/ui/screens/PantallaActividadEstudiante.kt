package com.duoc.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.mpazpro.data.Actividad
import com.duoc.mpazpro.ui.components.AudioPlayerButton

@Composable
fun PantallaActividadEstudiante(
    actividad: Actividad,
    intentosRealizados: Int,
    maxIntentosPermitidos: Int,
    onRespuestaSeleccionada: (opcion: Int, esCorrecta: Boolean) -> Unit
) {
    if (actividad.esPruebaFinal &&
        intentosRealizados >= maxIntentosPermitidos
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Has alcanzado el límite máximo de intentos ($maxIntentosPermitidos) configurado por tu docente.",
                fontSize = 20.sp,
                color = Color.Red
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = actividad.titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        // Reproductor de audio
        AudioPlayerButton(
            audioResName = actividad.audioResName
        )

        Text(
            text = actividad.instruccionTexto,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Alternativas de respuesta
        val opciones = listOf(
            actividad.opcionA,
            actividad.opcionB,
            actividad.opcionC
        )

        opciones.forEachIndexed { index, opcionText ->
            Button(
                onClick = {
                    val esCorrecta =
                        index == actividad.respuestaCorrecta

                    onRespuestaSeleccionada(index, esCorrecta)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = opcionText,
                    fontSize = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aviso de funcionalidad pendiente
        Text(
            text = "Para una futura actualización",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}