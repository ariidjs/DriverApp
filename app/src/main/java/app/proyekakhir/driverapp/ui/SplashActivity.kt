package app.proyekakhir.driverapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.ui.auth.MainActivity
import app.proyekakhir.driverapp.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject  lateinit var localProperties : LocalProperties
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Main).launch {
            delay(500)
            if (!localProperties.apiToken.isNullOrEmpty()) {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                finish()
            }else {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }

        }
    }
}