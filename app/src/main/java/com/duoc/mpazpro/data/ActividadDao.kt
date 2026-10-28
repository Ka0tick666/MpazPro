package com.duoc.mpazpro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividades WHERE idOA = :idOA AND esPruebaFinal = 0")
    fun getActividadesProgresivas(idOA: String): Flow<List<Actividad>>

    @Query("SELECT * FROM actividades WHERE idOA = :idOA AND esPruebaFinal = 1")
    fun getPruebaFinal(idOA: String): Flow<List<Actividad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividad(actividad: Actividad)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarRespuesta(registro: RegistroProgreso)

    @Query("SELECT COUNT(*) FROM registro_progreso WHERE idEstudiante = :idEstudiante AND idOA = :idOA AND esPruebaFinal = 1")
    suspend fun obtenerIntentosRealizados(idEstudiante: String, idOA: String): Int

    @Query("SELECT * FROM registro_progreso WHERE idEstudiante = :idEstudiante AND idOA = :idOA")
    fun obtenerHistorialEstudiante(idEstudiante: String, idOA: String): Flow<List<RegistroProgreso>>
}