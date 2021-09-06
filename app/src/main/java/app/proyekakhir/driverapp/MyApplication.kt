package app.proyekakhir.driverapp

import android.app.Application
import app.proyekakhir.core.di.firebaseModule
import app.proyekakhir.core.di.locationModule
import app.proyekakhir.core.di.repositoryModule
import app.proyekakhir.driverapp.di.viewModule
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(firebaseModule, locationModule, repositoryModule, viewModule))
        }
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }
}