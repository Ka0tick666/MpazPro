package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revisiones_utp")
data class RevisionUtp(
    @PrimaryKey
    val clave: String,
    val asignatura: String,
    val unidadTitulo: String,
    val estado: String = "En revisión",
    val observacion: String = "",
    // Cada entrada del historial ocupa una línea.
    val historial: String = ""
)
