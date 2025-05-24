// Entrevista guiada paso a paso para crear una hoja de vida por voz
package com.example.vitaescan

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class InterviewActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var answerButton: Button

    private val answers = mutableMapOf<String, String>()
    private var currentQuestionIndex = 0

    private val questions = listOf(
        "¿Cuál es tu nombre completo?",
        "¿Cuál es tu carrera?",
        "¿Cuáles son tus estudios?",
        "¿Tienes experiencia laboral?"
    )

    private val recognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let { processAnswer(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interview)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                123
            )
        }

        questionText = findViewById(R.id.question_text)
        answerButton = findViewById(R.id.answer_button)

        answerButton.setOnClickListener { startVoiceRecognition() }

        showNextQuestion()
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // o "es-CO" para Colombia
        recognizerLauncher.launch(intent)
    }

    private fun processAnswer(answer: String) {
        when (currentQuestionIndex) {
            0 -> answers["nombre"] = answer
            1 -> answers["carrera"] = answer
            2 -> answers["estudios"] = answer
            3 -> {
                answers["experiencia"] = if (answer.lowercase(Locale.ROOT).contains("no")) "No tiene experiencia" else ""
                if (answers["experiencia"].isNullOrEmpty()) {
                    currentQuestionIndex++
                    showNextQuestion()
                    return
                }
            }
            4 -> answers["experiencia"] = answer
        }

        currentQuestionIndex++
        if (currentQuestionIndex >= questions.size ||
            (currentQuestionIndex == 4 && answers["experiencia"] != "")) {
            launchPreview()
        } else {
            showNextQuestion()
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < questions.size) {
            questionText.text = questions[currentQuestionIndex]
        } else {
            launchPreview()
        }
    }

    private fun launchPreview() {
        val intent = Intent(this, PreviewCVActivity::class.java)
        intent.putExtra("nombre", answers["nombre"] ?: "")
        intent.putExtra("carrera", answers["carrera"] ?: "")
        intent.putExtra("estudios", answers["estudios"] ?: "")
        intent.putExtra("experiencia", answers["experiencia"] ?: "")
        startActivity(intent)
        finish()
    }
} 
