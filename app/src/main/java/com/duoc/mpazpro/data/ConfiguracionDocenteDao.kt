package com.duoc.mpazpro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConfiguracionDocenteDao {
    @Query("SELECT * FROM configuraciones_docente ORDER BY asignatura, unidadTitulo")
    suspend fun obtenerTodas(): List<ConfiguracionDocente>

    @Query("SELECT * FROM configuraciones_docente WHERE clave = :clave LIMIT 1")
    suspend fun buscarPorClave(clave: String): ConfiguracionDocente?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(configuracion: ConfiguracionDocente)
}
