package com.example.vitaescan

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.vitaescan.data.Puesto
import com.example.vitaescan.data.PostulacionDatabase
import com.example.vitaescan.data.PostulacionEntity
import com.example.vitaescan.adapters.FileItem

class PostulacionDialog(
    private val archivo: FileItem,
    private val context: FileActivity
) : DialogFragment() {

    private val puestos = listOf(
        Puesto(1, "Analista de Datos"),
        Puesto(2, "Diseñador UX/UI"),
        Puesto(3, "Desarrollador Móvil"),
        Puesto(4, "Coordinador de RRHH")
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_postulacion, null)
        val spinner: Spinner = view.findViewById(R.id.spinnerPuestos)

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            puestos.map { it.nombre }
        )
        spinner.adapter = adapter

        val builder = AlertDialog.Builder(context)
            .setTitle("Asignar postulación")
            .setView(view)
            .setPositiveButton("Asignar", null)  // <- lo dejamos nulo por ahora
            .setNegativeButton("Cancelar", null)

        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val puestoSeleccionado = puestos[spinner.selectedItemPosition]
                val db = PostulacionDatabase.getInstance(context)

                val yaExiste = db.postulacionDao()
                    .obtenerPorArchivoYPuesto(archivo.documento, puestoSeleccionado.id)
                    .isNotEmpty()

                if (yaExiste) {
                    Toast.makeText(context, "Esta hoja de vida ya fue asignada a ese puesto", Toast.LENGTH_LONG).show()
                } else {
                    db.postulacionDao().insertar(
                        PostulacionEntity(
                            personaNombre = archivo.nombre,
                            archivoId = archivo.documento,
                            puestoId = puestoSeleccionado.id
                        )
                    )
                    dialog.dismiss()
                    context.startActivity(Intent(context, PostuladosActivity::class.java))
                }
            }
        }

        return dialog
    }
}
