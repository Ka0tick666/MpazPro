package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades")
data class Actividad (
    @PrimaryKey(autoGenerate = true) val idActividad: Long = 0,
    val idOA: String,
    val asignatura: String,
    val titulo: String,
    val instruccionTexto: String,
    val audioResName: String? = null,
    val imagenResName: String? = null,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val respuestaCorrecta: Int,
    val esPruebaFinal: Boolean = false
)