package com.duoc.mpazpro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RevisionUtpDao {
    @Query("SELECT * FROM revisiones_utp")
    suspend fun obtenerTodas(): List<RevisionUtp>

    @Query("SELECT * FROM revisiones_utp WHERE clave = :clave LIMIT 1")
    suspend fun buscarPorClave(clave: String): RevisionUtp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(revision: RevisionUtp)
}
