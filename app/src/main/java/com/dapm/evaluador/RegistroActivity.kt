package com.dapm.evaluador

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        firestore = FirebaseFirestore.getInstance()



        val nombresLayout = findViewById<TextInputLayout>(R.id.nombresLayout)
        val apellidosLayout = findViewById<TextInputLayout>(R.id.apellidosLayout)
        val edadLayout = findViewById<TextInputLayout>(R.id.edadLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val apellidosEditText = findViewById<EditText>(R.id.apellidosEditText)
        val edadEditText = findViewById<EditText>(R.id.edadEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            handleRegister(
                nombresLayout, nombresEditText.text.toString().trim(),
                apellidosLayout, apellidosEditText.text.toString().trim(),
                edadLayout, edadEditText.text.toString().trim(),
                emailLayout, emailEditText.text.toString().trim(),
                passwordLayout, passwordEditText.text.toString().trim()
            )
        }
    }

    private fun handleRegister(
        nombresLayout: TextInputLayout, nombres: String,
        apellidosLayout: TextInputLayout, apellidos: String,
        edadLayout: TextInputLayout, edadStr: String,
        emailLayout: TextInputLayout, email: String,
        passwordLayout: TextInputLayout, password: String
    ) {
        val edad = edadStr.toIntOrNull()

        nombresLayout.error = null
        apellidosLayout.error = null
        edadLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null

        if (nombres.isEmpty()) {
            nombresLayout.error = getString(R.string.error_nombres)
            return
        }

        if (apellidos.isEmpty()) {
            apellidosLayout.error = getString(R.string.error_apellidos)
            return
        }

        if (edad == null || edad <= 0 || edad >= 120) {
            edadLayout.error = getString(R.string.error_edad)
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_email)
            return
        }

        val passwordValidationResult = validatePassword(password)
        if (passwordValidationResult != null) {
            passwordLayout.error = passwordValidationResult
            return
        } else {
            passwordLayout.error = null
        }
        val user = hashMapOf(
            "nombres" to nombres,
            "apellidos" to apellidos,
            "edad" to edad,
            "email" to email,
            "password" to password
        )
        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Usuario registrado con ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar usuario: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) {
            return getString(R.string.error_password_too_short)
        }

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasNumber = password.any { it.isDigit() } // Verifica si hay al menos un número

        return when {
            !hasUpperCase -> getString(R.string.error_password_no_uppercase)
            !hasLowerCase -> getString(R.string.error_password_no_lowercase)
            !hasNumber -> getString(R.string.error_password_no_number)
            else -> null
        }
    }
}
