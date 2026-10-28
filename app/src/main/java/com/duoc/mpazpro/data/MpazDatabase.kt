package com.duoc.mpazpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, RevisionUtp::class, ConfiguracionDocente::class,
        Actividad::class, RegistroProgreso::class],
    version = 3,
    exportSchema = false
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun revisionUtpDao(): RevisionUtpDao
    abstract fun configuracionDocenteDao(): ConfiguracionDocenteDao
    abstract fun actividadDao(): ActividadDao

    companion object {
        @Volatile
        private var INSTANCE: MpazDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS revisiones_utp (
                        clave TEXT NOT NULL PRIMARY KEY,
                        asignatura TEXT NOT NULL,
                        unidadTitulo TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        observacion TEXT NOT NULL,
                        historial TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS configuraciones_docente (
                        clave TEXT NOT NULL PRIMARY KEY,
                        asignatura TEXT NOT NULL,
                        unidadTitulo TEXT NOT NULL,
                        objetivosAprendizaje TEXT NOT NULL,
                        limiteIntentos INTEGER NOT NULL,
                        tipoActividad TEXT NOT NULL,
                        instrucciones TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun obtenerInstancia(context: Context): MpazDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MpazDatabase::class.java,
                    "mpaz_pro.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
