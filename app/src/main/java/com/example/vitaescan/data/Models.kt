
package com.example.vitaescan.data

data class Puesto(val id: Int, val nombre: String)

data class Postulacion(
    val id: Int = 0,
    val personaNombre: String,
    val archivoId: String,
    val puestoId: Int
)