package com.dapm.evaluador

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout

class evaluadorActivity : AppCompatActivity() {

    private lateinit var containerInputs: LinearLayout
    private lateinit var addButton: Button
    private lateinit var submitButton: Button
    private val inputViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluador)

        containerInputs = findViewById(R.id.containerInputs)
        addButton = findViewById(R.id.addButton)
        submitButton = findViewById(R.id.submitButton)

        addButton.setOnClickListener {
            addInputField()
        }

        submitButton.setOnClickListener {
            handleSubmit()
        }

        addInputField()

    }

    private fun isTotalPercentage100(): Boolean {
        var totalPorcentaje = 0.0
        for (view in inputViews) {
            val porcentajeEditText = view.findViewById<EditText>(R.id.porcentajeEditText)
            val porcentaje = porcentajeEditText.text.toString().toDoubleOrNull()
            if (porcentaje != null) {
                totalPorcentaje += porcentaje
            }
        }
        return totalPorcentaje >= 100.0
    }

    private fun areAllFieldsFilled(): Boolean {
        for (view in inputViews) {
            val notaEditText = view.findViewById<EditText>(R.id.notaEditText)
            val porcentajeEditText = view.findViewById<EditText>(R.id.porcentajeEditText)

            val nota = notaEditText.text.toString().trim()
            val porcentaje = porcentajeEditText.text.toString().trim()

            if (nota.isEmpty() || porcentaje.isEmpty()) {
                return false
            }
        }
        return true
    }


    private fun addInputField() {

        if (isTotalPercentage100()) {
            Toast.makeText(this, "La suma de los porcentajes ya es 100, no se pueden agregar más notas", Toast.LENGTH_SHORT).show()
            return
        }

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.input_field, containerInputs, false)
        val notaEditText = view.findViewById<EditText>(R.id.notaEditText)
        val porcentajeEditText = view.findViewById<EditText>(R.id.porcentajeEditText)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        notaEditText.addTextChangedListener(createTextWatcher())
        porcentajeEditText.addTextChangedListener(createTextWatcher())

        deleteButton.setOnClickListener {
            containerInputs.removeView(view)
            inputViews.remove(view)
        }

        containerInputs.addView(view)
        inputViews.add(view)
    }


    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun handleSubmit() {
        var totalPorcentaje = 0.0
        var weightedSum = 0.0
        var hasError = false

        for (view in inputViews) {
            val notaLayout = view.findViewById<TextInputLayout>(R.id.notaLayout)
            val porcentajeLayout = view.findViewById<TextInputLayout>(R.id.porcentajeLayout)
            val notaEditText = view.findViewById<EditText>(R.id.notaEditText)
            val porcentajeEditText = view.findViewById<EditText>(R.id.porcentajeEditText)

            val nota = notaEditText.text.toString().toDoubleOrNull()
            val porcentaje = porcentajeEditText.text.toString().toDoubleOrNull()

            notaLayout.error = null
            porcentajeLayout.error = null

            if (nota == null || porcentaje == null) {
                notaLayout.error = getString(R.string.error_required)
                porcentajeLayout.error = getString(R.string.error_required)
                hasError = true
            } else {
                if (nota > 20) {
                    notaLayout.error = getString(R.string.error_note_max)
                    hasError = true
                }
                if (porcentaje > 100) {
                    porcentajeLayout.error = getString(R.string.error_percentage_max)
                    hasError = true
                }
                if (nota <= 20 && porcentaje <= 100) {
                    totalPorcentaje += porcentaje
                    weightedSum += nota * (porcentaje / 100)
                }
            }
        }

        if (totalPorcentaje != 100.0) {
            Toast.makeText(this, "La suma de los porcentajes debe ser 100", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasError) {
            val result = weightedSum
            Toast.makeText(this, "El promedio ponderado es: $result", Toast.LENGTH_SHORT).show()
        }
    }
}
