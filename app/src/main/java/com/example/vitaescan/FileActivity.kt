package com.example.vitaescan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitaescan.adapters.FileAdapter
import com.example.vitaescan.adapters.FileItem
import java.io.File

class FileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter
    private val archivos = mutableListOf<FileItem>()
    private val archivosOriginales = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        toolbarTitle.text = "Mis Documentos"

        // Mostrar campo de búsqueda al tocar el botón
        btnSearch.setOnClickListener {
            if (etSearch.visibility == View.GONE) {
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            } else {
                etSearch.visibility = View.GONE
                etSearch.setText("")
                adapter.updateList(archivosOriginales)
            }
        }

        // Escuchar cambios en el texto para filtrar
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filteredList = archivosOriginales.filter {
                    it.nombre.contains(query, ignoreCase = true)
                }
                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        recyclerView = findViewById(R.id.recyclerView)
        adapter = FileAdapter(archivos) { archivo, action ->
            when (action) {
                "view" -> abrirArchivo(archivo)
                "delete" -> eliminarArchivo(archivo)
                "categorize" -> PostulacionDialog(archivo, this).show(supportFragmentManager, "Dialog")
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cargarArchivos()
    }

    private fun abrirArchivo(archivo: FileItem) {
        val file = File(filesDir, archivo.documento)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } else {
            Toast.makeText(this, "El archivo no existe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarArchivo(archivo: FileItem) {
        val file = File(filesDir, archivo.documento)
        if (file.exists() && file.delete()) {
            archivos.remove(archivo)
            archivosOriginales.remove(archivo)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Archivo eliminado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al eliminar el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarArchivos() {
        val files = filesDir.listFiles()
        archivos.clear()
        archivosOriginales.clear()
        files?.forEach { file ->
            val item = FileItem(file.name, file.nameWithoutExtension)
            archivos.add(item)
            archivosOriginales.add(item)
        }
        adapter.notifyDataSetChanged()
    }
}
