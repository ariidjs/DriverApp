package app.proyekakhir.core.di

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import app.proyekakhir.core.data.source.LocationListener
import app.proyekakhir.core.data.source.MyRepository
import app.proyekakhir.core.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseDatabase.getInstance("https://proyek-akhir-1b6f2-default-rtdb.asia-southeast1.firebasedatabase.app/") }
}

@SuppressLint("VisibleForTests")
val locationModule = module {
    single { FusedLocationProviderClient(androidContext()) }
    single {
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = Constants.LOCATION_REQUEST_INTERVAL
            fastestInterval = Constants.LOCATION_REQUEST_FASTEST_INTERVAL
            smallestDisplacement = Constants.LOCATION_REQUEST_SMALLEST_DISPLACEMENT
        }
    }
    single { LocationServices.getSettingsClient(androidContext()) }
    single {
        LocationSettingsRequest.Builder().apply {
            addLocationRequest(get())
            setAlwaysShow(true)
        }.build()
    }
}

val repositoryModule = module {
    single { MyRepository() }
    single { LocationListener(androidContext()) }
}