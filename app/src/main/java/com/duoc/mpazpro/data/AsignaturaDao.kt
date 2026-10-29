package com.duoc.mpazpro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignaturaDao {

    @Query("SELECT * FROM asignaturas ORDER BY nombre")
    fun obtenerTodas(): Flow<List<Asignatura>>

    @Query("SELECT * FROM asignaturas WHERE publicada = 1 ORDER BY nombre")
    fun obtenerPublicadas(): Flow<List<Asignatura>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(asignatura: Asignatura): Long

    @Update
    suspend fun actualizar(asignatura: Asignatura)

    @Delete
    suspend fun eliminar(asignatura: Asignatura)
}