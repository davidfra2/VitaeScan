package com.example.vitaescan

import android.net.Uri
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.vitaescan.databinding.ActivityScannerBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ScannerActivity : AppCompatActivity() {

    private var textoExtraido: String = ""
    private lateinit var binding: ActivityScannerBinding
    private var scanner: GmsDocumentScanner? = null
    private var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        scanner = GmsDocumentScanning.getClient(options)

        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanningResult?.getPdf()?.let { pdf ->
                    val pdfUri = pdf.getUri()

                    scanningResult.getPages()?.firstOrNull()?.let { page ->
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(page.getImageUri()))
                        procesarImagen(bitmap, pdfUri)
                    }
                }
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.scanBtn.setOnClickListener {
            scanDocument()
        }
    }

    private fun scanDocument() {
        scanner?.getStartScanIntent(this)?.addOnSuccessListener { intentSender ->
            scannerLauncher?.launch(IntentSenderRequest.Builder(intentSender).build())
        }?.addOnFailureListener {
            Toast.makeText(this, "Error al iniciar el escáner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarImagen(bitmap: Bitmap, pdfUri: Uri) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                textoExtraido = visionText.text
                showSaveDialog(pdfUri)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error en OCR", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSaveDialog(pdfUri: Uri) {
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
                val fileName = "$documento - $nombre.pdf"
                subirPdfYGuardarEnFirestore(pdfUri, fileName, nombre, documento)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun subirPdfYGuardarEnFirestore(uri: Uri, fileName: String, nombre: String, documento: String) {
        val storageRef = Firebase.storage.reference.child("hojas_vida/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val datos = extraerDatosCV(textoExtraido).copy(
                        documento = documento,
                        nombre = nombre
                    )
                    val hoja = hashMapOf(
                        "documento" to datos.documento,
                        "nombre" to datos.nombre,
                        "carrera" to datos.carrera,
                        "educacion" to datos.nivelEducacion,
                        "experiencia" to datos.experiencia,
                        "urlPdf" to downloadUrl.toString()
                    )

                    Firebase.firestore.collection("hojas_vida")
                        .document("$documento - $nombre")
                        .set(hoja)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Hoja de vida subida con PDF", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MenuActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error guardando en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error subiendo PDF", Toast.LENGTH_SHORT).show()
            }
    }
}
