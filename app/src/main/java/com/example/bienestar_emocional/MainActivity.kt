package com.example.bienestar_emocional

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import java.time.Instant


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

                val stepsResponse = healthConnectClient.readRecords(stepsRequest)
                if (stepsResponse.records.isNotEmpty()){
                    for(record in stepsResponse.records) {
                        // Procesa cada registro de pasos
                        //Toast.makeText(this, "Pasos: ${record.count}, Inicio: ${record.startTime}, Fin: ${record.endTime}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                        Log.i(TAG, "Total de pasos: ${record.count}")
                    }
                }else{
                    Log.i(TAG, "No hay datos pasos en Health Connect")
                }

            }
            //Frecuencia cardiaca
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(HeartRateRecord::class))) {
                val heartRateRequest = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )

                val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)
                if (heartRateResponse.records.isNotEmpty()) {
                    for(latidos in heartRateResponse.records) {
                        val promedioBPM = latidos.samples.map { it.beatsPerMinute }.average()
                        Toast.makeText(this, "Ritmo cardiaco: ${String.format("%.2f", promedioBPM)}, Inicio: ${latidos.startTime}, Fin: ${latidos.endTime}", Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "Ritmo cardiaco: $promedioBPM")
                    }

                }else{
                    Log.i(TAG, "No hay datos frecuencia cardiaca en Health Connect")
                }


            }



            //Presion arterial
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(BloodPressureRecord::class))) {
                val bloodPressureRateRequest = ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )

                val bloodPressureResponse = healthConnectClient.readRecords(bloodPressureRateRequest)
                if (bloodPressureResponse.records.isNotEmpty()){
                    for (presion in bloodPressureResponse.records) {
                        // Procesa cada registro de sangre
                        //Toast.makeText(this, "Pasos: ${presion.systolic}, Inicio: ${presion.time}, Fin: ${presion.diastolic}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint

                    }
                }else{
                    Log.i(TAG, "No hay datos presión arterial en Health Connect")
                }

            }
            //Sueño
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(SleepSessionRecord::class))) {
                val sleepRequest = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )

                val sleepResponse = healthConnectClient.readRecords(sleepRequest)
                if (sleepResponse.records.isNotEmpty()){
                    for (dormidito in sleepResponse.records) {
                        // Procesa los niveles de sue;o en este caso vamos a recuperar el rem y el profundo que es el deep
                        val duracionSueño = Duration.between(dormidito.startTime, dormidito.endTime).toMinutes()

                        //Toast.makeText(this, "timpo dormido: $duracionSueño , Inicio: ${dormidito.startTime}, Fin: ${dormidito.endTime}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                        Log.i(TAG, "Total de sueño en minutos: $duracionSueño")
                    }
                }else{
                    Log.i(TAG, "No hay datos sueño en Health Connect")
                }

            }
            //Ejercicio
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(ExerciseSessionRecord::class))) {
                val exerciseRequest = ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )

                val exerciseResponse = healthConnectClient.readRecords(exerciseRequest)
                if (exerciseResponse.records.isNotEmpty()){
                    for (ejercicio in exerciseResponse.records) {
                        // Procesa cada registro de ejercicio, opcional se puede utilizar el metodo de route para extraer una lista con coordenadas de putos de gps que se recorrieron durante la sesion
                        val duracionEjercicio = Duration.between(ejercicio.startTime, ejercicio.endTime)

                        //Toast.makeText(this, "Pasos: ${ejercicio.exerciseType}, Inicio: ${ejercicio.startTime}, Fin: ${ejercicio.endTime}, Total: ${duracionEjercicio.toHours()}", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                    }
                }else{
                    Log.i(TAG, "No hay datos ejercicio en Health Connect")
                }

            }
            //Ritmo cardíaco
            if (healthConnectClient.permissionController.getGrantedPermissions().contains(HealthPermission.getReadPermission(RestingHeartRateRecord::class))) {
                val restingHeartRateRequest = ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )

                val restingHeartRateResponse = healthConnectClient.readRecords(restingHeartRateRequest)
                if (restingHeartRateResponse.records.isNotEmpty()){
                    for (restHeart in restingHeartRateResponse.records) {
                        Toast.makeText(this, "Pasos: ${restHeart.beatsPerMinute}, Inicio: ${restHeart.time} , Fin: , Total: ", Toast.LENGTH_SHORT).show()
                        // Enviar 'record' a tu endpoint
                    }
                }else{
                    Log.i(TAG, "No hay datos ritmo cardiaco en Health Connect")
                }

            }


            Log.i(TAG, "Lectura de datos de Health Connect completada")
        }catch (e: Exception){
            Log.e(TAG, "Error al leer los datos de Health Connect", e)
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