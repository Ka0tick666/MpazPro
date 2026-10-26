
package com.duoc.mpazpro.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val correo: String,
    val contrasenaHash: String,
    val rol: String
)
