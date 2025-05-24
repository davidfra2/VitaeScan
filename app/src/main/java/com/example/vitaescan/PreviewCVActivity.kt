// Previsualiza la hoja de vida antes de guardar, con campos editables y botones de lápiz
package com.example.vitaescan

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream

class PreviewCVActivity : AppCompatActivity() {

    private lateinit var nombreText: EditText
    private lateinit var carreraText: EditText
    private lateinit var estudiosText: EditText
    private lateinit var experienciaText: EditText
    private lateinit var btnGuardar: Button
    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button

    private val PICK_IMAGE_REQUEST = 101
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_cv)

        nombreText = findViewById(R.id.edit_nombre)
        carreraText = findViewById(R.id.edit_carrera)
        estudiosText = findViewById(R.id.edit_estudios)
        experienciaText = findViewById(R.id.edit_experiencia)
        btnGuardar = findViewById(R.id.btn_guardar)
        imagePreview = findViewById(R.id.image_preview)
        btnSelectImage = findViewById(R.id.btn_select_image)

        // Rellenar los campos con la información recibida
        nombreText.setText(intent.getStringExtra("nombre"))
        carreraText.setText(intent.getStringExtra("carrera"))
        estudiosText.setText(intent.getStringExtra("estudios"))
        experienciaText.setText(intent.getStringExtra("experiencia"))

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnGuardar.setOnClickListener {
            showSaveDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imagePreview.setImageURI(imageUri)
        }
    }

    private fun showSaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_file, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etDocument = dialogView.findViewById<EditText>(R.id.et_document)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        btnSave.setOnClickListener {
            val documento = etDocument.text.toString().trim()
            val nombre = etName.text.toString().trim()

            if (documento.isNotEmpty() && nombre.isNotEmpty()) {
                val nombreArchivo = "$documento - $nombre"  // Aquí concatenamos documento y nombre
                generarPdfConImagen(nombreArchivo)           // Pasamos el nombre correcto
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun generarPdfConImagen(nombreArchivo: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        paint.textSize = 14f

        var y = 80f
        val margin = 40f

        imageUri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true)
            canvas.drawBitmap(scaledBitmap, margin, y, paint)
            y += 140f
        }

        canvas.drawText("Nombre: ${nombreText.text}", margin, y, paint)
        y += 30
        canvas.drawText("Carrera: ${carreraText.text}", margin, y, paint)
        y += 30
        canvas.drawText("Estudios: ${estudiosText.text}", margin, y, paint)
        y += 30
        canvas.drawText("Experiencia: ${experienciaText.text}", margin, y, paint)

        pdfDocument.finishPage(page)

        val file = File.createTempFile(nombreArchivo, ".pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        subirPdfAFirebase(file, "$nombreArchivo.pdf")
    }

    private fun subirPdfAFirebase(file: File, fileName: String) {
        val storageRef = Firebase.storage.reference.child("hojas_vida/$fileName")

        storageRef.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    guardarDatosConUrl(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir PDF", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarDatosConUrl(urlPdf: String) {
        val documento = "doc-${System.currentTimeMillis()}"
        val hoja = hashMapOf(
            "documento" to documento,
            "nombre" to nombreText.text.toString(),
            "carrera" to carreraText.text.toString(),
            "educacion" to estudiosText.text.toString(),
            "experiencia" to experienciaText.text.toString(),
            "urlPdf" to urlPdf
        )

        Firebase.firestore.collection("hojas_vida")
            .document(documento)
            .set(hoja)
            .addOnSuccessListener {
                Toast.makeText(this, "PDF subido y datos guardados", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error guardando hoja de vida", Toast.LENGTH_SHORT).show()
            }
    }
}
