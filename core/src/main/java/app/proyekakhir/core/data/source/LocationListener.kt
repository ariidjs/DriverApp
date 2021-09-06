package app.proyekakhir.core.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.proyekakhir.core.util.Constants
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

open class LocationListener(private val context: Context) {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationRequest? = null
    private var _location = MutableLiveData<Location>()
    val location: LiveData<Location> get() = _location
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for (locations in result.locations) {
                _location.value = locations
            }
        }
    }

    fun updateLocation(state: Boolean) {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = Constants.LOCATION_REQUEST_INTERVAL
            fastestInterval = Constants.LOCATION_REQUEST_FASTEST_INTERVAL
            smallestDisplacement = Constants.LOCATION_REQUEST_SMALLEST_DISPLACEMENT
        }

        if (state) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val task = fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest!!,
                    locationCallback,
                    Looper.getMainLooper()
                ) as Task<Void>
                task.addOnSuccessListener { Log.i("TAG", "updateLocation: success") }
                    .addOnFailureListener { Log.i("TAG", "updateLocation: error ${it.message}") }
            }
        }
    }

    fun removeLocationUpdates() {
        val task = fusedLocationProviderClient?.removeLocationUpdates(locationCallback) as Task<Void>
        if (task.isSuccessful) Log.i("TAG", "updateLocation: success") else Log.i(
            "TAG",
            "updateLocation: failure"
        )
    }
}