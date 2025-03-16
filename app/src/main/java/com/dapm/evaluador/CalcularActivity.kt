package com.dapm.evaluador

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import android.view.inputmethod.InputMethodManager
import android.content.Context


class CalcularActivity : AppCompatActivity() {
    private var isSlideUpActive = false
    private var isSlideDownActive = false

    private lateinit var containerInputs: LinearLayout
    private lateinit var containerResult: LinearLayout
    private lateinit var addButton: Button
    private lateinit var resultTextView: TextView
    private val inputViews = mutableListOf<View>()
    private lateinit var fraseAleatoria: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calcular)

        containerInputs = findViewById(R.id.containerInputs)
        containerResult = findViewById(R.id.containerResult)
        addButton = findViewById(R.id.addButton)
        resultTextView = findViewById(R.id.resultTextView)
        val textView: TextView = findViewById(R.id.textView)

        // Aplicar estilo al TextView
        val spannableString = SpannableString("EValuador")
        spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(TypefaceSpan("sans-serif-light"), 2, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString

        val frases = resources.getStringArray(R.array.frases_calculo)
        fraseAleatoria = frases.random()
        val fraseTextView: TextView = findViewById(R.id.fraseTextView)
        fraseTextView.text = fraseAleatoria

        addButton.setOnClickListener {
            addInputField()
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
        val deleteButton = view.findViewById<AppCompatImageButton>(R.id.deleteButton) // Corrected cast

        notaEditText.addTextChangedListener(createTextWatcher())
        porcentajeEditText.addTextChangedListener(createTextWatcher())

        deleteButton.setOnClickListener {
            containerInputs.removeView(view)
            inputViews.remove(view)
            slideDown(containerResult)

        }

        containerInputs.addView(view)
        inputViews.add(view)
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateResult()  // Update result every time a field changes
            }
        }
    }

    private fun updateResult() {
        if (!areAllFieldsFilled()) {
            slideDown(containerResult)
            return
        }

        var totalPorcentaje = 0.0
        var weightedSum = 0.0
        var hasError = false

        for (view in inputViews) {
            val notaEditText = view.findViewById<EditText>(R.id.notaEditText)
            val porcentajeEditText = view.findViewById<EditText>(R.id.porcentajeEditText)

            // Limitar la longitud de la entrada a dos caracteres
            limitEditTextInput(notaEditText, 2)
            limitEditTextInput(porcentajeEditText, 3)  // 3 dígitos para porcentaje, 100 es el máximo

            val nota = notaEditText.text.toString().toDoubleOrNull()
            val porcentaje = porcentajeEditText.text.toString().toDoubleOrNull()

            if (nota == null || porcentaje == null) {
                hasError = true
                break
            } else {
                if (nota > 20) {
                    notaEditText.setText("20")
                    hasError = true
                    break
                }
                if (porcentaje > 100) {
                    porcentajeEditText.setText("100")
                    Toast.makeText(this, "El porcentaje debe ser menor o igual a 100", Toast.LENGTH_SHORT).show()
                    hasError = true
                    break
                }
                totalPorcentaje += porcentaje
                weightedSum += nota * (porcentaje / 100)
            }
        }

        if (hasError) return

        if (totalPorcentaje != 100.0) {
            slideDown(containerResult)
            return
        }


        val result = weightedSum
        resultTextView.text = "El promedio ponderado es: $result"
        slideUp(containerResult)


    }

    // Función para limitar la entrada de los EditText a un número de caracteres
    private fun limitEditTextInput(editText: EditText, maxLength: Int) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLength) {
                    editText.setText(s.substring(0, maxLength))
                    editText.setSelection(maxLength)  // Mover el cursor al final
                }
            }
        })
    }

    private fun slideDown(container: LinearLayout) {
        // Si slideUp está activo, no ejecutar slideDown
        if (isSlideDownActive) return
        hideKeyboard()

        isSlideDownActive = true

        val slideDown = TranslateAnimation(0f, 0f, 0f, container.height.toFloat()) // Mover hacia abajo
        slideDown.duration = 300 // Duración de la animación
        slideDown.fillAfter = true // Mantener el estado final de la animación
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                container.visibility = View.GONE // Ocultar el contenedor después de la animación
                isSlideUpActive= false
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        container.startAnimation(slideDown)
    }

    private fun slideUp(container: LinearLayout) {
        if (isSlideUpActive) return
        hideKeyboard()

        isSlideUpActive = true
        // Asegúrate de que el contenedor esté visible antes de comenzar la animación
        container.visibility = View.VISIBLE

        val slideUp = TranslateAnimation(0f, 0f, container.height.toFloat(), 0f) // Mover hacia arriba
        slideUp.duration = 300 // Duración de la animación
        slideUp.fillAfter = true // Mantener el estado final de la animación
        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                isSlideDownActive = false // Marca slideUp como no activo después de la animación
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        container.startAnimation(slideUp)
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}