package com.example.bienestar_emocional

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.example.bienestar_emocional.ui.theme.BienestaremocionalTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import androidx.core.net.toUri

import androidx.health.connect.client.records.metadata.Metadata
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import kotlin.concurrent.write
import kotlin.text.format


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val providerPackageName = "com.google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(this, providerPackageName)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // Optionally redirect to package installer to find a provider, for example:
            val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            this.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = uriString.toUri()  // aqui puede que exista una fallita
                    putExtra("overlay", true)
                    putExtra("callerId", this.`package`) // Aqui puede existir un posible error a futuro
                }
            )
            return
        }
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            Toast.makeText(this, "Health Connect está listo.", Toast.LENGTH_SHORT).show()
        }

        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        
        // Create a set of permissions for required data types
        lifecycleScope.launch {
            checkPermissionsAndRun(healthConnectClient)
        }
        
    }

    private val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getWritePermission(SleepSessionRecord::class),
            HealthPermission.getWritePermission(BloodPressureRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(RestingHeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
        )

    // Issue operations with healthConnectClient
    // Create the permissions launcher
    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(permissions)) {
            // Permissions successfully granted
            Toast.makeText(this, "Health Connect permiso concedido", Toast.LENGTH_SHORT).show()


        } else {
            // Lack of required permissions
        }
    }


    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            // Permissions already granted; proceed with inserting or reading data
            lifecycleScope.launch {
                readData(healthConnectClient)
            }

        } else {
            requestPermissions.launch(permissions)
        }

    }
    

    @SuppressLint("SuspiciousIndentation")
    private suspend fun readData(healthConnectClient: HealthConnectClient) {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val endTime = Instant.now()

        val jsonBienestarEmocional = JSONObject()
        try {
            /*
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getWritePermission(HeartRateRecord::class))) {
                val zoneIdar = ZoneId.systemDefault()
                val startDateTime = LocalDate.now().atStartOfDay(zoneIdar)
                val endDateTime = startDateTime.plusHours(1)
                val heartRateRecord = HeartRateRecord(
                    startTime = startDateTime.toInstant(),
                    endTime = endDateTime.toInstant(),
                    samples = listOf(
                        HeartRateRecord.Sample(time = startTime, beatsPerMinute = 70),
                        HeartRateRecord.Sample(time = endTime, beatsPerMinute = 75)
                    ),
                    startZoneOffset = startDateTime.offset,
                    endZoneOffset = endDateTime.offset,
                    metadata = Metadata.manualEntry()
                )
                healthConnectClient.insertRecords(listOf(heartRateRecord))

            }
            */

            //Pasos
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(StepsRecord::class))) {
                val stepsRequest = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonPasos = JSONObject()
                val stepsResponse = healthConnectClient.readRecords(stepsRequest)
                if (stepsResponse.records.isNotEmpty()){
                    for(record in stepsResponse.records) {
                        // Procesa cada registro de pasos
                        //Toast.makeText(this, "Pasos: ${record.count}, Inicio: ${record.startTime}, Fin: ${record.endTime}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                        Log.i(TAG, "Total de pasos: ${record.count}")
                        jsonPasos.put("pasos", record.count)
                        jsonBienestarEmocional.put("pasosTotales", jsonPasos)
                    }
                }else{
                    Log.i(TAG, "No hay datos pasos en Health Connect")
                    jsonPasos.put("pasos", "")
                    jsonBienestarEmocional.put("pasosTotales", jsonPasos)
                }

            }
            //Frecuencia cardiaca
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(HeartRateRecord::class))) {
                val heartRateRequest = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonFreCardiaca = JSONObject()
                val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)

                if (heartRateResponse.records.isNotEmpty()) {
                    for(latidos in heartRateResponse.records) {
                        val promedioBPM = latidos.samples.map { it.beatsPerMinute }.average()
                        Toast.makeText(this, "Ritmo cardiaco: ${String.format("%.2f", promedioBPM)}, Inicio: ${latidos.startTime}, Fin: ${latidos.endTime}", Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "Ritmo cardiaco: ${promedioBPM}")

                    }
                    val latido = heartRateResponse.records.map { record -> record.samples.map { it.beatsPerMinute }.average() }
                    jsonFreCardiaca.put("latidosPM",latido.average())
                    jsonBienestarEmocional.put("frecuenciaCardiaca", jsonFreCardiaca)

                }else{
                    Log.i(TAG, "No hay datos frecuencia cardiaca en Health Connect")
                    jsonFreCardiaca.put("latidosPM", "")
                    jsonBienestarEmocional.put("frecuenciaCardiaca", jsonFreCardiaca)
                }


            }



            //Presion arterial
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(BloodPressureRecord::class))) {
                val bloodPressureRateRequest = ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonPresionArterial = JSONObject()
                val bloodPressureResponse = healthConnectClient.readRecords(bloodPressureRateRequest)
                if (bloodPressureResponse.records.isNotEmpty()){
                    for (presion in bloodPressureResponse.records) {
                        // Procesa cada registro de sangre
                        //Toast.makeText(this, "Pasos: ${presion.systolic}, Inicio: ${presion.time}, Fin: ${presion.diastolic}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint

                    }
                    val presionSys = bloodPressureResponse.records.map{ record -> record.systolic.inMillimetersOfMercury }
                    val presionDia = bloodPressureResponse.records.map { record -> record.diastolic.inMillimetersOfMercury }
                    jsonPresionArterial.put("presionArterialSystolica", presionSys)
                    jsonPresionArterial.put("presionArterialDiastolica", presionDia)
                    jsonBienestarEmocional.put("presionArterial", jsonPresionArterial)
                }else{
                    Log.i(TAG, "No hay datos presión arterial en Health Connect")
                    jsonPresionArterial.put("presionArterialSystolica", "")
                    jsonPresionArterial.put("presionArterialDiastolica", "")
                    jsonBienestarEmocional.put("presionArterial", jsonPresionArterial)
                }

            }
            //Sueño
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(SleepSessionRecord::class))) {
                val sleepRequest = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonSuenio = JSONObject()
                val sleepResponse = healthConnectClient.readRecords(sleepRequest)
                if (sleepResponse.records.isNotEmpty()){
                    for (dormidito in sleepResponse.records) {
                        // Procesa los niveles de sue;o en este caso vamos a recuperar el rem y el profundo que es el deep
                        val duracionSuenio = Duration.between(dormidito.startTime, dormidito.endTime).toMinutes()
                        jsonSuenio.put("suenioMin", duracionSuenio)
                        jsonBienestarEmocional.put("suenioCont", jsonSuenio)
                        //Toast.makeText(this, "timpo dormido: $duracionSueño , Inicio: ${dormidito.startTime}, Fin: ${dormidito.endTime}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                        Log.i(TAG, "Total de sueño en minutos: $duracionSuenio")
                    }


                }else{
                    Log.i(TAG, "No hay datos sueño en Health Connect")
                    jsonSuenio.put("suenioMin", "")
                    jsonBienestarEmocional.put("suenioCont", jsonSuenio)
                }

            }
            //Ejercicio
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(ExerciseSessionRecord::class))) {
                val exerciseRequest = ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonEjercicio = JSONObject()
                val exerciseResponse = healthConnectClient.readRecords(exerciseRequest)
                if (exerciseResponse.records.isNotEmpty()){
                    for (ejercicio in exerciseResponse.records) {
                        // Procesa cada registro de ejercicio, opcional se puede utilizar el metodo de route para extraer una lista con coordenadas de putos de gps que se recorrieron durante la sesion
                        val duracionEjercicio = Duration.between(ejercicio.startTime, ejercicio.endTime)


                    }
                    val ejercicioTotal = exerciseResponse.records.map { record -> Duration.between(record.startTime, record.endTime).toMinutes() }
                    jsonEjercicio.put("ejercicioMin", ejercicioTotal)
                    jsonBienestarEmocional.put("ejercicioCont", jsonEjercicio)
                }else{
                    Log.i(TAG, "No hay datos ejercicio en Health Connect")
                    jsonEjercicio.put("ejercicioMin", "")
                    jsonBienestarEmocional.put("ejercicioCont", jsonEjercicio)
                }

            }
            //Ritmo cardíaco
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(RestingHeartRateRecord::class))) {
                val restingHeartRateRequest = ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val jsonRitmoCardiaca = JSONObject()
                val restingHeartRateResponse = healthConnectClient.readRecords(restingHeartRateRequest)
                if (restingHeartRateResponse.records.isNotEmpty()){
                    for (restHeart in restingHeartRateResponse.records) {
                        Toast.makeText(this, "Pasos: ${restHeart.beatsPerMinute}, Inicio: ${restHeart.time} , Fin: , Total: ", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                    }
                    val ritmoCardiaca = restingHeartRateResponse.records.map { record -> record.beatsPerMinute }
                    jsonRitmoCardiaca.put("ritmoCardiaca", ritmoCardiaca)
                    jsonBienestarEmocional.put("ritmoCardiacaTotal", jsonRitmoCardiaca)
                }else{
                    Log.i(TAG, "No hay datos ritmo cardiaco en Health Connect")
                    jsonRitmoCardiaca.put("ritmoCardiaca", "")
                    jsonBienestarEmocional.put("ritmoCardiacaTotal", jsonRitmoCardiaca)
                }

            }

            Log.i(TAG, "JSON Bienestar Emocional: $jsonBienestarEmocional")

            Log.i(TAG, "Lectura de datos de Health Connect completada")
        }catch (e: Exception){
            Log.e(TAG, "Error al leer los datos de Health Connect", e)
        }
    }


    fun guardarJsonEnArchivoTxt(context: Context, jsonObject: JSONObject, nombreArchivoBase: String = "datos_salud") {
        try {
            // 1. Convierte el JSONObject a un String formateado (con indentación para legibilidad)
            val jsonStringFormateado: String = jsonObject.toString(4) // 4 espacios de indentación

            // 2. Define el nombre del archivo con una marca de tiempo para evitar sobrescribir

            val nombreArchivo = "${nombreArchivoBase}.txt"

            // 3. Obtén el directorio de archivos internos de tu aplicación
            // Estos archivos son privados para tu aplicación.
            // Otras opciones:
            // - context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS): Almacenamiento externo específico de la app (visible en el explorador de archivos en algunos casos).
            // - Si necesitas que sea accesible públicamente y guardarlo en Downloads (requiere permisos y MediaStore API en Android 10+): es más complejo.
            // Para simplemente "ver la estructura", el directorio interno es el más fácil.
            val directorioArchivosInternos: File = context.filesDir // Directorio: /data/user/0/tu.paquete.de.app/files

            val archivo = File(directorioArchivosInternos, nombreArchivo)

            // 4. Escribe el string JSON en el archivo
            // Opción A: Usando FileOutputStream y OutputStreamWriter (tradicional)
            FileOutputStream(archivo).use { fos ->
                fos.writer().use { writer ->
                    writer.write(jsonStringFormateado)
                }
            }

            // Opción B: Usando la función de extensión de Kotlin 'writeText' (más conciso)
            // archivo.writeText(jsonStringFormateado) // Esto sobrescribe el archivo si ya existe o lo crea

            //Log.i(TAG, "JSON guardado con éxito en: ${archivo.absolutePath}")
            //Toast.makeText(context, "JSON guardado en: ${archivo.name}", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar el JSON en el archivo .txt", e)
            Toast.makeText(context, "Error al guardar JSON", Toast.LENGTH_SHORT).show()
        } catch (e: org.json.JSONException) { // Si hay un error al formatear el JSON
            Log.e(TAG, "Error de JSONException al formatear para guardar", e)
            Toast.makeText(context, "Error de formato JSON", Toast.LENGTH_SHORT).show()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BienestaremocionalTheme {
        Greeting("Android")
    }
}
data class DatosBienestarEmocional(
    val steps: DatosPasos,
    val frecuenciaCardiaca: DatosFrecuenciaCardiaca,
    val presionArterial: Int,
    val sueño: Int,
    val ejercicio: Int,
)
data class DatosPasos(
    val steps: Int,
)

data class DatosFrecuenciaCardiaca(
    val latidosPM: Int,
)

