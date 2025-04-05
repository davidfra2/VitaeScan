package com.example.vitaescan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostulacionDao {

    @Insert
    fun insertar(postulacion: PostulacionEntity)

    @Query("SELECT * FROM postulaciones WHERE archivoId = :archivoId AND puestoId = :puestoId")
    fun obtenerPorArchivoYPuesto(archivoId: String, puestoId: Int): List<PostulacionEntity>

    @Query("SELECT * FROM postulaciones")
    fun obtenerTodas(): List<PostulacionEntity>

    @Query("SELECT * FROM postulaciones WHERE puestoId = :puestoId")
    fun obtenerPorPuesto(puestoId: Int): List<PostulacionEntity>

    @Delete
    fun eliminar(postulacion: PostulacionEntity)
}
