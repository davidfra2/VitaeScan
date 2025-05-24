package com.example.vitaescan

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnDigitalizar: Button
    private lateinit var btnHacercv: Button
    private lateinit var btnVercv: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        tvWelcome = findViewById(R.id.tv_welcome)
        btnDigitalizar = findViewById(R.id.btn_digitalizar)
        btnHacercv = findViewById(R.id.btn_hacercv)
        btnVercv = findViewById(R.id.btn_vercv)

        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"

        tvWelcome.text = "Bienvenido, $nombreUsuario"

        btnDigitalizar.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        btnHacercv.setOnClickListener {
            val intent = Intent(this, InterviewActivity::class.java)
            startActivity(intent)
        }


        btnVercv.setOnClickListener {
            val intent = Intent(this, FileActivity::class.java)
            startActivity(intent)
        }


    }
}