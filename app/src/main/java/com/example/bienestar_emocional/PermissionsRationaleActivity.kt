package com.example.bienestar_emocional


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.text
import com.example.bienestar_emocional.R

// Importa tu R generado

class PermissionsRationaleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_rationale) // Necesitas crear este layout

        val textViewRationale: TextView = findViewById(R.id.textViewRationaleContent)
        val buttonDone: Button = findViewById(R.id.buttonDone)

        // Aquí construirías o establecerías tu texto de justificación.
        // Podrías tenerlo en strings.xml o construirlo dinámicamente si es necesario.
        // Es buena idea hacer que el texto sea "scrolleable" si es largo.
        textViewRationale.text = buildRationaleText()
        textViewRationale.movementMethod = LinkMovementMethod.getInstance() // Para que los enlaces en el texto funcionen (si usas Html.fromHtml)


        buttonDone.setOnClickListener {
            finish() // Simplemente cierra esta actividad de justificación
        }

        // También puedes obtener el nombre del paquete que llamó a esta actividad
        // si necesitas personalizar el texto basado en ello, aunque generalmente no es necesario.
        // val callingPackage = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        // Log.d("RationaleActivity", "Rationale requested by: $callingPackage")
    }

    private fun buildRationaleText(): CharSequence {
        // Ejemplo: Carga desde strings.xml o constrúyelo aquí
        // Es importante que este texto explique CLARAMENTE por qué necesitas los permisos.
        // Puedes usar Html.fromHtml para formateo básico si es necesario.
        return """
            <h1>Por qué ${getString(R.string.app_name)} necesita acceso a tus datos de salud</h1>
            <p>Para ofrecerte una experiencia completa y personalizada, nuestra aplicación necesita acceder a ciertos tipos de datos de Health Connect:</p>
            <ul>
                <li><b>Pasos:</b> Para monitorizar tu actividad diaria, mostrarte tu progreso y ayudarte a alcanzar tus objetivos de movimiento.</li>
                <li><b>Frecuencia Cardíaca:</b> Para analizar tus zonas de entrenamiento durante el ejercicio y ofrecerte información sobre tu salud cardiovascular.</li>
                <li><b>Sueño:</b> Para ayudarte a entender tus patrones de sueño y mejorar tu descanso.</li>
                </ul>
            <p><b>Tu privacidad es importante para nosotros.</b> Tus datos de salud se utilizan únicamente para proporcionarte estas funciones dentro de la aplicación y se manejan de acuerdo con nuestra <a href="https://www.tusitioweb.com/politica-de-privacidad">Política de Privacidad</a>.</p>
            <p>Puedes gestionar estos permisos en cualquier momento desde la configuración de Health Connect o la configuración de permisos de tu dispositivo.</p>
        """.trimIndent().let {
            android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY)
        }
    }
}