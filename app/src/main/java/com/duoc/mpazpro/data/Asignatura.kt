package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignaturas")
data class Asignatura(
    @PrimaryKey(autoGenerate = true)
    val idAsignatura: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val publicada: Boolean = false
)