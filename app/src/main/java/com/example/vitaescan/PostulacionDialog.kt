package com.example.vitaescan

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostulacionDialog(
    private val archivo: Map<String, Any>,
    private val context: FileActivity
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_postulacion, null)
        val spinner: Spinner = view.findViewById(R.id.spinnerPuestos)

        val db = Firebase.firestore

        // Cargar puestos desde Firebase
        db.collection("puestos").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(context, "No hay puestos disponibles", Toast.LENGTH_LONG).show()
                    dismiss()
                    return@addOnSuccessListener
                }

                val puestos = snapshot.documents.map { it.id to (it.getString("nombrePuesto") ?: "Sin título") }
                val ids = puestos.map { it.first }
                val nombres = puestos.map { it.second }

                val adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
                )
                spinner.adapter = adapter

                val builder = AlertDialog.Builder(context)
                    .setTitle("Asignar postulación")
                    .setView(view)
                    .setPositiveButton("Asignar", null)
                    .setNegativeButton("Cancelar", null)

                val dialog = builder.create()

                dialog.setOnShowListener {
                    val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    button.setOnClickListener {
                        val puestoId = ids[spinner.selectedItemPosition]
                        val puestoNombre = nombres[spinner.selectedItemPosition]

                        val documento = archivo["documento"] as? String ?: ""
                        val nombre = archivo["nombre"] as? String ?: ""

                        val asignado = mapOf(
                            "documento" to documento,
                            "nombre" to nombre
                        )


                        // Verificar si ya está asignado
                        db.collection("puestos").document(puestoId).get()
                            .addOnSuccessListener { doc ->
                                val asignados = doc.get("asignados") as? List<Map<String, String>> ?: emptyList()
                                val yaExiste = asignados.any { it["documento"] == documento }

                                if (yaExiste) {
                                    Toast.makeText(context, "Esta hoja de vida ya fue asignada a ese puesto", Toast.LENGTH_LONG).show()
                                } else {
                                    db.collection("puestos").document(puestoId)
                                        .update("asignados", FieldValue.arrayUnion(asignado))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "${archivo["nombre"] ?: "Desconocido"} asignado a $puestoNombre"
                                            , Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al asignar", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                }

                dialog.show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar puestos", Toast.LENGTH_LONG).show()
                dismiss()
            }

        // Retorna un Dialog vacío temporalmente, ya que se mostrará más adelante
        return AlertDialog.Builder(context).create()
    }
}
