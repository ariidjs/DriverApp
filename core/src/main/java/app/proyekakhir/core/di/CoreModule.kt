package app.proyekakhir.core.di

import android.annotation.SuppressLint
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import app.proyekakhir.core.BuildConfig
import app.proyekakhir.core.BuildConfig.BASE_MAPS_URL
import app.proyekakhir.core.data.source.LocationListener
import app.proyekakhir.core.data.source.MyRepository
import app.proyekakhir.core.data.source.remote.RemoteDataSource
import app.proyekakhir.core.data.source.remote.network.ApiInterface
import app.proyekakhir.core.data.source.remote.network.ApiMapsInterface
import app.proyekakhir.core.domain.repository.IMyRepository
import app.proyekakhir.core.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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


val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(20, TimeUnit.MINUTES)
            .readTimeout(20, TimeUnit.MINUTES)
            .writeTimeout(20, TimeUnit.MINUTES)
            .build()
    }
    factory(named("base")) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiInterface::class.java)
    }

    factory(named("maps")) {
        val retrofit2 = Retrofit.Builder()
            .baseUrl(BASE_MAPS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit2.create(ApiMapsInterface::class.java)
    }

}

val localModule = module {
//    single{
//        getSharedPrefs(androidApplication())
//    }
//
//    single<SharedPreferences.Editor> {
//        getSharedPrefs(androidApplication()).edit()
//    }
//
//    single { LocalProperties(get(), get()) }
}

val repositoryModule = module {
    single { RemoteDataSource(get(named("base")), get(named("maps"))) }
    single<IMyRepository> { MyRepository(get()) }
    single { LocationListener(androidContext()) }
}