package app.proyekakhir.core.domain

import android.location.Location
import androidx.lifecycle.LiveData

interface IMyRepository {
    fun updateLocation(state: Boolean): LiveData<Location>
}