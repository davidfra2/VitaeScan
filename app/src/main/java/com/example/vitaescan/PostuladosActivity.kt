package com.example.vitaescan

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostuladosActivity : AppCompatActivity() {

    private lateinit var btnToggleFiltros: ImageButton
    private lateinit var layoutFiltros: LinearLayout
    private lateinit var spinnerPuesto: Spinner
    private lateinit var btnFiltroCarrera: Button
    private lateinit var btnFiltroEducacion: Button
    private lateinit var btnFiltroExperiencia: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostuladosAdapter

    private val opcionesCarrera = arrayOf("Ingeniería", "Tecnología", "Salud", "Ciencias Sociales", "Derecho", "Administración y Negocios", "Arte y Diseño", "Educación", "Ciencias Exactas", "Humanidades", "Tecnología Alimentaria", "Otros")
    private val seleccionCarrera = BooleanArray(opcionesCarrera.size)

    private val opcionesEducacion = arrayOf("Técnico", "Pregrado", "Especialización", "Maestría", "Doctorado")
    private val seleccionEducacion = BooleanArray(opcionesEducacion.size)

    private val opcionesExperiencia = arrayOf("Sí", "No")
    private val seleccionExperiencia = BooleanArray(opcionesExperiencia.size)

    private var puestoSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postulados)

        btnToggleFiltros = findViewById(R.id.btnToggleFiltros)
        layoutFiltros = findViewById(R.id.layoutFiltros)
        spinnerPuesto = findViewById(R.id.spinnerPuesto)
        btnFiltroCarrera = findViewById(R.id.btnFiltroCarrera)
        btnFiltroEducacion = findViewById(R.id.btnFiltroEducacion)
        btnFiltroExperiencia = findViewById(R.id.btnFiltroExperiencia)
        recyclerView = findViewById(R.id.recyclerViewPostulados)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostuladosAdapter(emptyList())
        recyclerView.adapter = adapter

        btnToggleFiltros.setOnClickListener {
            layoutFiltros.visibility =
                if (layoutFiltros.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        setupFiltros()
        cargarPuestos()
    }

    private fun setupFiltros() {
        btnFiltroCarrera.setOnClickListener {
            mostrarDialogoMultiple("Seleccionar carrera(s)", opcionesCarrera, seleccionCarrera) {
                btnFiltroCarrera.text = opcionesSeleccionadas(opcionesCarrera, seleccionCarrera, "Seleccionar carrera(s)")
                aplicarFiltros()
            }
        }

        btnFiltroEducacion.setOnClickListener {
            mostrarDialogoMultiple("Seleccionar nivel(es)", opcionesEducacion, seleccionEducacion) {
                btnFiltroEducacion.text = opcionesSeleccionadas(opcionesEducacion, seleccionEducacion, "Seleccionar nivel(es)")
                aplicarFiltros()
            }
        }

        btnFiltroExperiencia.setOnClickListener {
            mostrarDialogoMultiple("Seleccionar experiencia", opcionesExperiencia, seleccionExperiencia) {
                btnFiltroExperiencia.text = opcionesSeleccionadas(opcionesExperiencia, seleccionExperiencia, "Seleccionar experiencia")
                aplicarFiltros()
            }
        }
    }

    private fun mostrarDialogoMultiple(
        titulo: String,
        opciones: Array<String>,
        seleccionados: BooleanArray,
        onAceptar: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMultiChoiceItems(opciones, seleccionados) { _, which, isChecked ->
                seleccionados[which] = isChecked
            }
            .setPositiveButton("Aceptar") { _, _ -> onAceptar() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun opcionesSeleccionadas(opciones: Array<String>, seleccionados: BooleanArray, textoDefault: String): String {
        val seleccion = opciones.filterIndexed { index, _ -> seleccionados[index] }
        return if (seleccion.isEmpty()) textoDefault else seleccion.joinToString(", ")
    }

    private fun cargarPuestos() {
        Firebase.firestore.collection("puestos").get()
            .addOnSuccessListener { result ->
                val nombres = result.documents.mapNotNull { it.getString("nombrePuesto") }
                val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPuesto.adapter = adapterSpinner

                spinnerPuesto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        puestoSeleccionado = nombres[position]
                        aplicarFiltros()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
    }

    private fun aplicarFiltros() {
        Firebase.firestore.collection("puestos")
            .whereEqualTo("nombrePuesto", puestoSeleccionado)
            .get()
            .addOnSuccessListener { snapshot ->
                val personas = snapshot.documents
                    .flatMap { doc ->
                        (doc.get("asignados") as? List<Map<String, Any>>)?.mapNotNull { data ->
                            try {
                                DatosCV(
                                    documento = data["documento"] as? String ?: "",
                                    nombre = data["nombre"] as? String ?: "",
                                    carrera = data["carrera"] as? String ?: "",
                                    nivelEducacion = data["nivelEducacion"] as? String ?: "",
                                    experiencia = data["experiencia"] as? String ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()
                    }
                    .filter { filtrarPorSeleccion(it) }

                adapter.actualizarLista(personas)
            }
    }


    private fun filtrarPorSeleccion(cv: DatosCV): Boolean {
        val carreraNorm = normalizarCarrera(cv.carrera)
        val educacionNorm = normalizarEducacion(cv.nivelEducacion)
        val experiencia = cv.experiencia

        val coincideCarrera = seleccionCarrera.indexOfFirst { it } == -1 ||
                seleccionCarrera.anyIndexed { i, sel -> sel && carreraNorm == opcionesCarrera[i] }

        val coincideEducacion = seleccionEducacion.indexOfFirst { it } == -1 ||
                seleccionEducacion.anyIndexed { i, sel -> sel && educacionNorm == opcionesEducacion[i] }

        val coincideExperiencia = seleccionExperiencia.indexOfFirst { it } == -1 ||
                seleccionExperiencia.anyIndexed { i, sel -> sel && experiencia.equals(opcionesExperiencia[i], true) }

        return coincideCarrera && coincideEducacion && coincideExperiencia
    }

    private fun BooleanArray.anyIndexed(predicate: (index: Int, Boolean) -> Boolean): Boolean {
        for (i in indices) {
            if (predicate(i, this[i])) return true
        }
        return false
    }

    private fun normalizarCarrera(carrera: String): String {
        val c = carrera.lowercase()
        return when {
            c.contains("ingeniería") -> "Ingeniería"
            c.contains("informática") || c.contains("sistemas") || c.contains("tecnología") -> "Tecnología"
            c.contains("medicina") || c.contains("salud") -> "Salud"
            c.contains("psicología") || c.contains("sociología") -> "Ciencias Sociales"
            c.contains("derecho") -> "Derecho"
            c.contains("administración") || c.contains("negocios") -> "Administración y Negocios"
            c.contains("diseño") || c.contains("arte") -> "Arte y Diseño"
            c.contains("educación") || c.contains("pedagogía") -> "Educación"
            c.contains("matemáticas") || c.contains("ciencias") -> "Ciencias Exactas"
            c.contains("historia") -> "Humanidades"
            c.contains("alimento") -> "Tecnología Alimentaria"
            else -> "Otros"
        }
    }

    private fun normalizarEducacion(nivel: String): String {
        val e = nivel.lowercase()
        return when {
            e.contains("doctorado") -> "Doctorado"
            e.contains("maestría") || e.contains("master") -> "Maestría"
            e.contains("especialización") -> "Especialización"
            e.contains("pregrado") || e.contains("licenciatura") -> "Pregrado"
            e.contains("técnico") -> "Técnico"
            else -> "No detectado"
        }
    }
}
