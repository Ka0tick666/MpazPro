package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registro_progreso")
data class RegistroProgreso(
    @PrimaryKey(autoGenerate = true) val idRegistro: Long = 0,
    val idEstudiante: String,
    val idOA: String,
    val idActividad: Long,
    val respuestaSelecionada: Int,
    val esCorrecta: Boolean,
    val esPruebaFinal: Boolean = false,
    val numeroIntento: Int = 1,
    val fechaHora: Long = System.currentTimeMillis()
)
