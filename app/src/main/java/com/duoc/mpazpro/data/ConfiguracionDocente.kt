package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuraciones_docente")
data class ConfiguracionDocente(
    @PrimaryKey
    val clave: String,
    val asignatura: String,
    val unidadTitulo: String,
    val objetivosAprendizaje: String = "",
    val limiteIntentos: Int = 3,
    val tipoActividad: String = "Desafío",
    val instrucciones: String = ""
)
