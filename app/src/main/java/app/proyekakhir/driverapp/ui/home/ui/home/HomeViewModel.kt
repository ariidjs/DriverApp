package app.proyekakhir.driverapp.ui.home.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import app.proyekakhir.core.data.source.LocationListener

class HomeViewModel(
    private val locationRepository: LocationListener
) : ViewModel() {
    val location: LiveData<Location> get() = locationRepository.location
    fun requestLocation() {
        locationRepository?.updateLocation(true)
    }

    fun removeLocation() = locationRepository.removeLocationUpdates()
}