
package com.duoc.mpazpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class],
    version = 1,
    exportSchema = false
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: MpazDatabase? = null

        fun obtenerInstancia(context: Context): MpazDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MpazDatabase::class.java,
                    "mpaz_pro.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
