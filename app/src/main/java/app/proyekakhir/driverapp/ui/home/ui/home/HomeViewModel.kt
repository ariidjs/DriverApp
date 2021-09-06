package app.proyekakhir.driverapp.ui.home.ui.home

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.proyekakhir.core.data.source.LocationListener
import app.proyekakhir.core.data.source.MyRepository

class HomeViewModel(
    private val locationRepository : LocationListener
) : ViewModel() {
//    private var locationRepository : LocationListener? = null
    val location :LiveData<Location> get() = locationRepository.location
//    fun initLocationListener(context: Context) {
//        locationRepository = LocationListener(context)
//    }
    fun requestLocation() {
        locationRepository?.updateLocation(true)
    }
    fun removeLocation() = locationRepository.removeLocationUpdates()
}