package com.dapm.evaluador

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciosesion)

        firestore = FirebaseFirestore.getInstance()

        val imageView = findViewById<ImageView>(R.id.imageView)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registarseButton = findViewById<Button>(R.id.registarseButton)
        imageView.loadCircularImage(R.drawable.perfil)

        loginButton.setOnClickListener {
            handleLogin(
                emailLayout, emailEditText.text.toString().trim(),
                passwordLayout, passwordEditText.text.toString().trim()
            )
        }

        registarseButton.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            val intentRegistro = Intent(this, RegistroActivity::class.java)
            startActivity(intentRegistro)
        }

    }
    fun ImageView.loadCircularImage(resourceId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val roundedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(roundedBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val radius = min(bitmap.width, bitmap.height) / 2.0f
        canvas.drawCircle(bitmap.width / 2.0f, bitmap.height / 2.0f, radius, paint)
        this.setImageBitmap(roundedBitmap)
    }




    private fun handleLogin(
        emailLayout: TextInputLayout, email: String,
        passwordLayout: TextInputLayout, password: String
    ) {
        emailLayout.error = null
        passwordLayout.error = null

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_email)
            return
        }

        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.error_password)
            return
        }

        firestore.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Redirect to the main activity or another appropriate activity
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error during login: $e", Toast.LENGTH_SHORT).show()
            }
    }
}