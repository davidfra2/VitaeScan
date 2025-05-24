package com.example.vitaescan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostuladosAdapter(private var lista: List<DatosCV>) :
    RecyclerView.Adapter<PostuladosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvCarrera: TextView = itemView.findViewById(R.id.tvCarrera)
        val tvEducacion: TextView = itemView.findViewById(R.id.tvEducacion)
        val tvExperiencia: TextView = itemView.findViewById(R.id.tvExperiencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postulado, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postulante = lista[position]

        holder.tvTitulo.text = "${postulante.documento} - ${postulante.nombre}"
        holder.tvCarrera.text = "Carrera: ${postulante.carrera}"
        holder.tvEducacion.text = "Educación: ${postulante.nivelEducacion}"
        holder.tvExperiencia.text = "Experiencia: ${postulante.experiencia}"



        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, postulante)
            true
        }
    }

    private fun showPopupMenu(view: View, postulante: DatosCV) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_postulado)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_asignar -> {
                    mostrarDialogoAsignar(view.context, postulante)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    fun mostrarDialogoAsignar(context: Context, postulante: DatosCV) {
        val db = Firebase.firestore

        db.collection("puestos").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(context, "No hay puestos disponibles", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nombres: List<String> = result.documents.mapNotNull { it.getString("nombrePuesto") }

                AlertDialog.Builder(context)
                    .setTitle("Asignar a un puesto")
                    .setItems(nombres.toTypedArray()) { _, which ->
                        val nombreSeleccionado = nombres[which]
                        val docId = "${postulante.documento} - ${postulante.nombre}"

                        db.collection("hojas_vida").document(docId)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (!snapshot.exists()) {
                                    Toast.makeText(context, "No se encontró hoja de vida en la base de datos", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                }

                                val datos = snapshot.toObject(DatosCV::class.java)
                                if (datos == null) {
                                    Toast.makeText(context, "Error al leer hoja de vida", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                }

                                try {
                                    val asignado = mapOf(
                                        "documento" to datos.documento,
                                        "nombre" to datos.nombre,
                                        "carrera" to datos.carrera,
                                        "nivelEducacion" to datos.nivelEducacion,
                                        "experiencia" to datos.experiencia
                                    )

                                    db.collection("puestos").document(nombreSeleccionado)
                                        .update("asignados", FieldValue.arrayUnion(asignado))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "${datos.nombre} asignado correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al asignar: ${it.message}", Toast.LENGTH_LONG).show()
                                        }

                                } catch (e: Exception) {
                                    Toast.makeText(context, "Excepción al asignar: ${e.message}", Toast.LENGTH_LONG).show()
                                    Log.e("Asignar", "Error crítico", e)
                                }




                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al cargar hoja de vida", Toast.LENGTH_LONG).show()
                            }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar puestos: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }



    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<DatosCV>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
