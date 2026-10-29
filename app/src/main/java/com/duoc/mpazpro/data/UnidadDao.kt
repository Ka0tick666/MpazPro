package com.duoc.mpazpro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {

    @Query("""
        SELECT * FROM unidades
        WHERE idAsignatura = :idAsignatura
        ORDER BY idUnidad
    """)
    fun obtenerPorAsignatura(idAsignatura: Long): Flow<List<Unidad>>

    @Query("""
        SELECT * FROM unidades
        WHERE idAsignatura = :idAsignatura
        AND publicada = 1
        ORDER BY idUnidad
    """)
    fun obtenerPublicadas(idAsignatura: Long): Flow<List<Unidad>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(unidad: Unidad): Long

    @Update
    suspend fun actualizar(unidad: Unidad)

    @Delete
    suspend fun eliminar(unidad: Unidad)
}