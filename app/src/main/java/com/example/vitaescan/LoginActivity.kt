package com.example.vitaescan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class Usuario(val correo: String, val contrasena: String, val nombre: String)

class LoginActivity : AppCompatActivity() {

    // Base de datos provisional
    private val usuarios = listOf(
        Usuario("usuario1@gmail.com", "password1", "Juan Pérez"),
        Usuario("usuario2@gmail.com", "password2", "María López"),
        Usuario("usuario3@gmail.com", "password3", "Carlos García")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Obtener las referencias a los campos de texto y el botón
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etContrasena = findViewById<EditText>(R.id.et_contrasena)
        val btnIniciarSesion = findViewById<Button>(R.id.btn_iniciar_sesion)

        // Lógica para manejar el inicio de sesión
        btnIniciarSesion.setOnClickListener {
            val correo = etCorreo.text.toString()
            val contrasena = etContrasena.text.toString()

            // Validar que los campos no estén vacíos
            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
                // Verificar las credenciales
                val usuario = usuarios.find { it.correo == correo && it.contrasena == contrasena }
                if (usuario != null) {
                    // Credenciales correctas, redirigir a la siguiente actividad
                    Toast.makeText(this, "Bienvenido, ${usuario.nombre}!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("nombreUsuario", usuario.nombre) // Pasar el nombre del usuario
                    startActivity(intent)
                }
                else {
                    // Credenciales incorrectas
                    Toast.makeText(this, "Credenciales incorrectas.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}