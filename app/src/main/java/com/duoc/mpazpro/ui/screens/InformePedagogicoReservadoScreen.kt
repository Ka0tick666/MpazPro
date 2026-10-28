package com.duoc.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.mpazpro.data.RegistroProgreso

@Composable
fun InformePedagogicoReservadoScreen(
    rolUsuario: String, // "DOCENTE", "UTP" o "ESTUDIANTE"
    idEstudiante: String,
    idOA: String,
    registros: List<RegistroProgreso>
) {
    // Bloqueo estricto para estudiantes
    if (rolUsuario == "ESTUDIANTE") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Acceso denegado: El informe pedagógico es de uso exclusivo del docente.",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    val total = registros.size
    val correctas = registros.count { it.esCorrecta }
    val porcentaje = if (total > 0) (correctas * 100) / total else 0

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Informe Pedagógico Reservado", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Estudiante ID: $idEstudiante", fontWeight = FontWeight.Bold)
                Text("Objetivo: $idOA")
                Text("Respuestas Correctas: $correctas / $total ($porcentaje%)")

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (porcentaje >= 70) {
                    Text("Fortalezas: Comprensión adecuada del objetivo de aprendizaje.", color = Color(0xFF2E7D32))
                } else {
                    Text("Contenidos a Reforzar: Requiere ejercitación guiada en la asignatura.", color = Color(0xFFC62828))
                }
            }
        }
    }
}