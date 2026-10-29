package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unidades",
    foreignKeys = [
        ForeignKey(
            entity = Asignatura::class,
            parentColumns = ["idAsignatura"],
            childColumns = ["idAsignatura"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idAsignatura")]
)
data class Unidad(
    @PrimaryKey(autoGenerate = true)
    val idUnidad: Long = 0,
    val idAsignatura: Long,
    val titulo: String,
    val descripcion: String = "",
    val contenido: String = "",
    val publicada: Boolean = false
)