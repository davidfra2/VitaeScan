package com.example.vitaescan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitaescan.adapters.FileAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter
    private val archivos = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        val layoutFiltros = findViewById<LinearLayout>(R.id.layoutFiltros)




        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(archivos, this) { archivo, action ->
            when (action) {
                "view" -> abrirPdf(archivo)
                "delete" -> eliminarArchivo(archivo)
                "categorize" -> abrirDialogoPostulacion(archivo)
            }
        }

        recyclerView.adapter = adapter

        btnSearch.setOnClickListener {
            etSearch.visibility = if (etSearch.visibility == EditText.GONE) EditText.VISIBLE else EditText.GONE
        }

        val btnIrPostulados = findViewById<ImageButton>(R.id.btnIrPostulados)
        btnIrPostulados.setOnClickListener {
            startActivity(Intent(this, PostuladosActivity::class.java))
        }


        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filtrados = archivos.filter {
                    (it["nombre"] as? String)?.contains(query, ignoreCase = true) == true
                }
                adapter.updateList(filtrados)
            }
        })

        cargarDesdeFirestore()
    }

    private fun cargarDesdeFirestore() {
        Firebase.firestore.collection("hojas_vida").get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { it.data }
                archivos.clear()
                archivos.addAll(lista)
                adapter.updateList(archivos)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar hojas de vida", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirPdf(archivo: Map<String, Any>) {
        val url = archivo["urlPdf"] as? String
        if (!url.isNullOrEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(url), "application/pdf")
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir el PDF", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Este documento no tiene archivo PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarArchivo(archivo: Map<String, Any>) {
        val nombre = archivo["nombre"] as? String ?: return
        val documento = archivo["documento"] as? String ?: return
        val docId = "$documento - $nombre"

        Firebase.firestore.collection("hojas_vida").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Hoja de vida eliminada", Toast.LENGTH_SHORT).show()
                cargarDesdeFirestore()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirDialogoPostulacion(archivo: Map<String, Any>) {
        PostulacionDialog(archivo, this).show(supportFragmentManager, "Dialog")
    }
}
