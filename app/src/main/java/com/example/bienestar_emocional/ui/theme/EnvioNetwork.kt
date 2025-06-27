package com.example.bienestar_emocional.ui.theme



import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// Suponiendo que tienes TAG definido en tu clase
// private const val TAG = "NetworkUtils" // o el nombre de tu clase

class EnvioNetwork {

    // Crea una única instancia de OkHttpClient para reutilizarla
    // Es eficiente hacerlo así en lugar de crear una nueva para cada petición
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-f-8".toMediaType()

    suspend fun enviarJsonAPc(
        jsonObject: JSONObject,
        ipAddress: String,
        port: Int,
        endpoint: String = "/" // Opcional: si tu servidor tiene un endpoint específico, ej: "/datos"
    ) {
        val url = "http://$ipAddress:$port$endpoint"
        val jsonString = jsonObject.toString()

        Log.d(TAG, "Enviando JSON a: $url")
        Log.d(TAG, "Contenido JSON: $jsonString")

        // Las operaciones de red deben estar en un hilo de fondo
        withContext(Dispatchers.IO) {
            try {
                val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody) // Usamos POST para enviar datos
                    .build()

                // Opción 1: Ejecución síncrona dentro de la coroutine (más simple para este caso)
                client.newCall(request).execute().use { response -> // .use asegura que la respuesta se cierre
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Error en la petición HTTP: ${response.code} - ${response.message}")
                        Log.e(TAG, "Cuerpo de la respuesta de error: ${response.body?.string()}")
                        // Aquí podrías manejar el error, reintentar, o notificar al usuario
                    } else {
                        Log.i(TAG, "JSON enviado con éxito a $url")
                        Log.i(TAG, "Respuesta del servidor: ${response.body?.string()}")
                        // Aquí puedes procesar la respuesta del servidor si es necesario
                    }
                }

                // Opción 2: Ejecución asíncrona con Callback (más tradicional en OkHttp si no usas coroutines directamente para el execute)
                /*
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "Fallo al enviar JSON a $url", e)
                        // Manejar el error de conexión/IO
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use { // Asegura que la respuesta se cierre
                            if (!response.isSuccessful) {
                                Log.e(TAG, "Error en la petición HTTP: ${response.code} - ${response.message}")
                                Log.e(TAG, "Cuerpo de la respuesta de error: ${response.body?.string()}")
                            } else {
                                Log.i(TAG, "JSON enviado con éxito a $url (Callback)")
                                Log.i(TAG, "Respuesta del servidor (Callback): ${response.body?.string()}")
                            }
                        }
                    }
                })
                */

            } catch (e: IOException) {
                Log.e(TAG, "IOException al enviar JSON a $url", e)
                // Manejar error de red (ej: servidor no disponible, no hay conexión)
            } catch (e: Exception) {
                Log.e(TAG, "Excepción desconocida al enviar JSON a $url", e)
                // Otro tipo de error
            }
        }
    }

    companion object { // Para poder llamar a la función sin instanciar NetworkUtils si lo prefieres
        private const val TAG = "NetworkUtils" // TAG específico para la clase
    }
}