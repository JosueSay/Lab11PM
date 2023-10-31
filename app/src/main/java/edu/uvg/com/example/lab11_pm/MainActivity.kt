package edu.uvg.com.example.lab11_pm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import edu.uvg.com.example.lab11_pm.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest

class MainActivity : AppCompatActivity() {
    // Declaración de variables
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_ID = 42 // Identificador para el permiso
    lateinit var binding: ActivityMainBinding // Variable de binding para el diseño de la actividad
    val permisoLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION // Permiso de ubicación aproximada
    val permisoFine = android.Manifest.permission.ACCESS_FINE_LOCATION // Permiso de ubicación precisa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Comprobación y solicitud de permisos para acceder a la ubicación del dispositivo
        if (allPermissionsGrantedGPS()) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            // Llama a la función para obtener la ubicación actual del dispositivo
            leerUbicacionActual()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permisoLocation, permisoFine),
                PERMISSION_ID
            )
        }
        binding.accederButton.setOnClickListener {
            leerUbicacionActual()
        }
    }

    // Función que verifica si todos los permisos para la ubicación están otorgados
    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    // Función para obtener y mostrar la ubicación actual del dispositivo
    private fun leerUbicacionActual() {
        if (checkLocationPermission()) {
            if (isLocationEnabled()) {
                mFusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        // Muestra la latitud y longitud obtenidas en la interfaz
                        binding.textViewLatitud.text = "LATITUD = " + location.latitude.toString()
                        binding.textViewLongitud.text = "LONGITUD = " + location.longitude.toString()
                    }
                }
            } else {
                // Notifica al usuario para activar la ubicación si está desactivada
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                finish()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permisoLocation, permisoFine),
                PERMISSION_ID
            )
        }
    }

    // Función para verificar si la ubicación está activada en el dispositivo
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Función para verificar si se otorgó el permiso de ubicación
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                permisoLocation
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permisoFine),
                PERMISSION_ID
            )
            return false
        }
        return true
    }

    // Función para solicitar nuevos datos de ubicación
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }
    }

    // Callback para obtener la última ubicación actualizada
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            // Muestra la última latitud y longitud obtenida en la interfaz
            binding.textViewLatitud.text = "LATITUD = " + mLastLocation?.latitude.toString()
            binding.textViewLongitud.text = "LONGITUD = " + mLastLocation?.longitude.toString()
        }
    }

    // Objeto companion para almacenar los permisos necesarios
    companion object {
        private val REQUIRED_PERMISSIONS_GPS = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
