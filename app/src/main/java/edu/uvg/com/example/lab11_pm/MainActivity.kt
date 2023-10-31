package edu.uvg.com.example.lab11_pm
import android.Manifest
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
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_ID = 42
    private val REQUIRED_PERMISSIONS_GPS = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se infla la vista de la actividad
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Acción al presionar el botón "Acceder" para solicitar permisos y obtener ubicación
        binding.accederButton.setOnClickListener {
            if (allPermissionsGrantedGPS()) {
                if (isLocationEnabled()) {
                    checkAndRequestPermissions() // Verifica permisos y solicita si no están otorgados
                    leerUbicacionActual() // Obtiene la ubicación actual si está disponible
                } else {
                    // Muestra un mensaje y abre la configuración de ubicación si está desactivada
                    Toast.makeText(this, "Por favor, activa la ubicación para usar esta función", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                // Solicita permisos de ubicación si no están otorgados
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS_GPS,
                    PERMISSION_ID
                )
            }
        }
    }

    // Verifica y solicita los permisos de ubicación
    private fun checkAndRequestPermissions() {
        if (allPermissionsGrantedGPS()) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            leerUbicacionActual()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS_GPS,
                PERMISSION_ID
            )
        }
    }

    // Maneja la respuesta a la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                leerUbicacionActual()
            } else {
                // Muestra un mensaje si los permisos no fueron otorgados
                Toast.makeText(this, "Los permisos de ubicación son necesarios para usar esta función", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Verifica si la ubicación está habilitada en el dispositivo
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Verifica si todos los permisos necesarios para la ubicación están otorgados
    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    // Obtiene y muestra la ubicación actual del dispositivo si los permisos y la ubicación están habilitados
    private fun leerUbicacionActual() {
        if (allPermissionsGrantedGPS()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mFusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData() // Si la ubicación no está disponible, se solicitan nuevos datos
                    } else {
                        // Muestra la ubicación obtenida en la interfaz
                        binding.textViewLatitud.text = "LATITUD = " + location.latitude.toString()
                        binding.textViewLongitud.text = "LONGITUD = " + location.longitude.toString()
                    }
                }
            } else {
                // Si la ubicación está desactivada, se muestra un mensaje y se abre la configuración
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // Si los permisos no están otorgados, se solicitan
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS_GPS,
                PERMISSION_ID
            )
        }
    }

    // Solicita nuevos datos de ubicación si no está disponible la ubicación actual
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }
    }
}