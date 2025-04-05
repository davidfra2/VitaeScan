package com.example.vitaescan

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vitaescan.data.Puesto
import com.example.vitaescan.data.PostulacionDatabase
import com.example.vitaescan.data.PostulacionEntity

class PostuladosActivity : AppCompatActivity() {

    private val puestos = listOf(
        Puesto(1, "Analista de Datos"),
        Puesto(2, "Diseñador UX/UI"),
        Puesto(3, "Desarrollador Móvil"),
        Puesto(4, "Coordinador de RRHH")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postulados)

        val spinner: Spinner = findViewById(R.id.spinnerFiltro)
        val listView: ListView = findViewById(R.id.lvPostulados)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            puestos.map { it.nombre }
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val db = PostulacionDatabase.getInstance(this@PostuladosActivity)
                val postulados = db.postulacionDao().obtenerPorPuesto(puestos[position].id)

                val nombres = postulados.map { it.personaNombre }
                listView.adapter = ArrayAdapter(
                    this@PostuladosActivity,
                    android.R.layout.simple_list_item_1,
                    nombres
                )

                listView.setOnItemClickListener { _, _, index, _ ->
                    val persona = postulados[index]
                    AlertDialog.Builder(this@PostuladosActivity)
                        .setTitle("Eliminar postulación")
                        .setMessage("¿Estás seguro que deseas eliminar esta postulación?")
                        .setPositiveButton("Sí") { _, _ ->
                            db.postulacionDao().eliminar(persona)
                            Toast.makeText(this@PostuladosActivity, "Postulación eliminada", Toast.LENGTH_SHORT).show()
                            recreate()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
