package com.example.vitaescan

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.vitaescan.databinding.ActivityScannerBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

class ScannerActivity : AppCompatActivity() {

    lateinit var binding: ActivityScannerBinding
    private val TAG: String = "MyTag"
    var scanner: GmsDocumentScanner? = null
    var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private fun showSaveDialog(pdfUri: Uri) {
        // Inflar el diseño del Dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_file, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Referencias a los elementos del Dialog
        val etDocument = dialogView.findViewById<EditText>(R.id.et_document)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        btnSave.setOnClickListener {
            val document = etDocument.text.toString().trim()
            val name = etName.text.toString().trim()

            if (document.isNotEmpty() && name.isNotEmpty()) {
                // Crear el nombre del archivo
                val fileName = "$document - $name.pdf"

                // Guardar el archivo con el nuevo nombre
                saveFile(pdfUri, fileName)

                // Cerrar el Dialog
                dialog.dismiss()

            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveFile(pdfUri: Uri, fileName: String) {
        try {
            val inputStream = contentResolver.openInputStream(pdfUri)
            val outputStream = openFileOutput(fileName, MODE_PRIVATE)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(this, "Archivo guardado como $fileName", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar el archivo: ${e.message}")
            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

        scanner = GmsDocumentScanning.getClient(options)
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    scanningResult?.getPdf()?.let { pdf ->
                        val pdfUri = pdf.getUri()
                        showSaveDialog(pdfUri)
                    }
                } else {
                    Toast.makeText(this, "Failed To Fetch Result!", Toast.LENGTH_SHORT).show()
                }
            }

        binding.scanBtn.setOnClickListener {
            scanDocument()
        }
    }

    private fun scanDocument() {
        scanner?.getStartScanIntent(this@ScannerActivity)?.addOnSuccessListener { intentSender ->
            scannerLauncher?.launch(IntentSenderRequest.Builder(intentSender).build())
        }?.addOnFailureListener {
            Log.d(TAG, "scanDocument: ${it.message}")
            Toast.makeText(this, "Failed To Start Scanner!", Toast.LENGTH_SHORT).show()
        }
    }
}