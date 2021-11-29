package app.proyekakhir.driverapp

import android.app.Application
import android.media.AudioManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import app.proyekakhir.core.di.*
import app.proyekakhir.driverapp.di.useCaseModule
import app.proyekakhir.driverapp.di.viewModule
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                listOf(
                    firebaseModule, locationModule, repositoryModule, viewModule,
                    networkModule, useCaseModule, localModule
                )
            )
        }
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }
}