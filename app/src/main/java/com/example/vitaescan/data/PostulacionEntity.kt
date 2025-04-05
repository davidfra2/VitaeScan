package com.example.vitaescan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "postulaciones")
data class PostulacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personaNombre: String,
    val archivoId: String,
    val puestoId: Int
)

@Database(entities = [PostulacionEntity::class], version = 1)
abstract class PostulacionDatabase : RoomDatabase() {
    abstract fun postulacionDao(): PostulacionDao

    companion object {
        @Volatile
        private var INSTANCE: PostulacionDatabase? = null

        fun getInstance(context: Context): PostulacionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostulacionDatabase::class.java,
                    "postulaciones_db"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
